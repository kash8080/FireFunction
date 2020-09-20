package com.kashyapmedia.firefunction.responses;

public class ApiSuccessResponse<T> extends ApiResponse<T> {


    private T body;

    public ApiSuccessResponse(T body) {
        this.body = body;
    }

    public T getBody() {
        return body;
    }
}
