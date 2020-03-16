package com.kashyapmedia.firefunction.Models;

public interface Response<T> {
    void onSuccess(T data);
    void onError(Exception e);
}
