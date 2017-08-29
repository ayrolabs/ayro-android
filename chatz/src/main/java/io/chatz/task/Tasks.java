package io.chatz.task;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import io.chatz.exception.TaskException;
import io.chatz.util.Constants;

public class Tasks {

  private static final String TASKS_THREAD_NAME = "Chatz.tasks";
  private static final String SCHEDULED_TASKS_THREAD_NAME = "Chatz.tasks.scheduled";
  private static final int SLEEP_TIME = 10000;

  private static Tasks instance;

  private Context context;
  private Handler taskHandler;
  private LinkedBlockingQueue<Task> tasksQueue;
  private LinkedBlockingQueue<Task> failedTasksQueue;
  private boolean runScheduledTasks;
  private int scheduledTaskExecution;

  private Tasks(Context context) {
    this.context = context;
    HandlerThread taskThread = new HandlerThread(TASKS_THREAD_NAME);
    taskThread.start();
    this.taskHandler = new Handler(taskThread.getLooper());
    this.tasksQueue = new LinkedBlockingQueue<>();
    this.failedTasksQueue = new LinkedBlockingQueue<>();
    this.runScheduledTasks = true;
    executeScheduledTasks();
  }

  public static Tasks getInstance(Context context) {
    if (instance == null) {
      instance = new Tasks(context);
    }
    return instance;
  }

  public boolean hasPendingTasks() {
    return !tasksQueue.isEmpty() || !failedTasksQueue.isEmpty();
  }

  private void executeScheduledTasks() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (runScheduledTasks) {
          try {
            Task task = failedTasksQueue.poll();
            if (task == null) {
              if (tasksQueue.isEmpty()) {
                broadcastTasksChanged();
              }
              task = tasksQueue.take();
              scheduledTaskExecution = 0;
            }
            try {
              Object result = task.executeJob();
              emitSuccess(task, result);
            } catch (TaskException exception) {
              emitFail(task, exception);
              if (!exception.shouldCancelTask()) {
                if (scheduledTaskExecution > 16) {
                  scheduledTaskExecution = 0;
                }
                scheduledTaskExecution++;
                failedTasksQueue.put(task);
                long sleepTime = SLEEP_TIME * scheduledTaskExecution;
                Log.i(Constants.TAG, String.format("(%s) Retrying in %d milliseconds", task.getName(), sleepTime));
                Thread.sleep(sleepTime);
              } else {
                failedTasksQueue.clear();
              }
            }
          } catch (Exception e) {
            // Nothing to do...
          }
        }
      }
    }, SCHEDULED_TASKS_THREAD_NAME).start();
  }

  <T> void execute(final Task<T> task) {
    taskHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          T result = task.executeJob();
          emitSuccess(task, result);
        } catch (TaskException exception) {
          emitFail(task, exception);
        }
      }
    });
  }

  void schedule(Task task) {
    try {
      tasksQueue.put(task);
      broadcastTasksChanged();
    } catch (InterruptedException e) {
      // Nothing to do...
    }
  }

  private <T> void emitSuccess(Task<T> task, T result) {
    TaskCallback<T> callback = task.getCallback();
    if (callback != null) {
      callback.onSuccess(result);
    }
  }

  private <T> void emitFail(Task<T> task, TaskException exception) {
    TaskCallback<T> callback = task.getCallback();
    if (callback != null) {
      callback.onFail(exception);
    }
  }

  private void broadcastTasksChanged() {
    context.sendBroadcast(new Intent(Constants.INTENT_ACTION_TASKS_CHANGED));
  }
}