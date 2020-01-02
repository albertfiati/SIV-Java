package com.albertfiati.Exceptions;

public class InsufficientArguementsException extends Exception {
    public InsufficientArguementsException() {
        super("Insufficient arguments. use the -h flag to get help");
    }
}
