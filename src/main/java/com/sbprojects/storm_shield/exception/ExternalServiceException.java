package com.sbprojects.storm_shield.exception;

//Custom exception designed to cleanly flag external downstream API drops
public class ExternalServiceException extends RuntimeException{
    public ExternalServiceException(String message){
        super(message);
    }
}
