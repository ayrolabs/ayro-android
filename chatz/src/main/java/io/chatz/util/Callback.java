package io.chatz.util;

public interface Callback<T> {

  void onSuccess(T result);

  void onFail(Exception e);

}