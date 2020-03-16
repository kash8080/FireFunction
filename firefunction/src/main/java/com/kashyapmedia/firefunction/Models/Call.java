package com.kashyapmedia.firefunction.Models;

public interface Call<T> {
    void execute(Response<T> response);
}
