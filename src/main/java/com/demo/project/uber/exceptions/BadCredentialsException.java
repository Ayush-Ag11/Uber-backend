package com.demo.project.uber.exceptions;

public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException(){
    }

    public BadCredentialsException(String message) {
        super(message);
    }
}
