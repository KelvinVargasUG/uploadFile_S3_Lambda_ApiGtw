package com.kjvargas.uploadtos3.ApiResponse;

public class ApiResponse {
    private String body;
    private int statusCode;

    public ApiResponse(String body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
