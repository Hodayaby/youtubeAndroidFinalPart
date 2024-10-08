package com.example.myyoutube;

public class Resource<T> {
    private T data;
    private String error;

    public Resource(T data, String error) {
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(data, null);
    }

    public static <T> Resource<T> error(String error) {
        return new Resource<>(null, error);
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}

