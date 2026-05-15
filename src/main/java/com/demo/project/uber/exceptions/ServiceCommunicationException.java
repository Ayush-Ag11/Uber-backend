package com.demo.project.uber.exceptions;

public class ServiceCommunicationException extends RuntimeException {

    public ServiceCommunicationException(String message) {
        super(message);
    }
}
