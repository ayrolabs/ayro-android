package io.chatz.task.impl;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import io.chatz.model.User;
import io.chatz.service.ChatzService;
import io.chatz.store.Store;
import io.chatz.task.Task;
import io.chatz.exception.TaskException;
import io.chatz.util.Constants;
import io.chatz.util.MessageUtils;
import retrofit2.Response;

public class UpdateUserTask extends Task<User> {

  private static final String TASK_NAME = "user.update";

  private static final String GENERIC_ERROR_STATUS = "999";
  private static final String GENERIC_ERROR_CODE = "user.update.error";
  private static final String GENERIC_ERROR_MESSAGE = "Could not update user";

  private Context context;
  private User user;
  private ChatzService chatzService;

  public UpdateUserTask(Context context, User user) {
    super(TASK_NAME);
    this.context = context;
    this.user = user;
    this.chatzService = ChatzService.getInstance();
  }

  @Override
  protected User executeJob() throws TaskException {
    Log.i(Constants.TAG, String.format("(%s) Updating user...", TASK_NAME));
    String apiToken = Store.getApiToken(context);
    if (apiToken == null) {
      return null;
    }
    try {
      Response<User> response = chatzService.updateUser(apiToken, user).execute();
      if (response.isSuccessful()) {
        Log.i(Constants.TAG, String.format("(%s) User updated with success!", TASK_NAME));
        return response.body();
      } else {
        TaskException exception = new TaskException(response, true);
        Log.e(Constants.TAG, String.format("(%s) Could not update user: %s", TASK_NAME, MessageUtils.get(exception)));
        throw exception;
      }
    } catch (IOException e) {
      TaskException exception = new TaskException(GENERIC_ERROR_STATUS, GENERIC_ERROR_CODE, GENERIC_ERROR_MESSAGE, e, false);
      Log.e(Constants.TAG, String.format("(%s) %s", TASK_NAME, exception.getMessage()));
      throw exception;
    }
  }
}