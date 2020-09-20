package com.kashyapmedia.firefunction.responses;

public class ApiErrorResponse<T> extends ApiResponse<T> {
    private String errorMessage;

    public ApiErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
