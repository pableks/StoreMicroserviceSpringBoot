package com.example.StoreApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsuariosNotFoundException extends RuntimeException {
    
    public UsuariosNotFoundException(String message){
        super(message);
    }

}
