package com.reactive.reviewback.exception;

public class GenericException extends RuntimeException{
    public GenericException(){

    }

    public GenericException(String message){
        super(message);
    }
}
