package com.reactive.reviewback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class APIConnectionError extends RuntimeException{
    
    public APIConnectionError(){

    }

    public APIConnectionError(String message){
        super(message);
    }

}
