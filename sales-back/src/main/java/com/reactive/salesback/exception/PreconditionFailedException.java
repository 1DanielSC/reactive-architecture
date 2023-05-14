package com.reactive.salesback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class PreconditionFailedException extends RuntimeException{
    public PreconditionFailedException(){

    }
    public PreconditionFailedException(String message){
        super(message);
    }
}
