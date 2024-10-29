package com.example.StoreApp.dto;

public class LoginResponse {
    private final boolean success;
    private final String message;
    private final UserDTO user;

    public LoginResponse(boolean success, String message, UserDTO user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // Constructor para respuestas sin usuario (error)
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.user = null;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserDTO getUser() {
        return user;
    }
}