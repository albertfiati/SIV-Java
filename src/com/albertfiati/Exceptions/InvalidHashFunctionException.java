package com.albertfiati.Exceptions;

public class InvalidHashFunctionException extends Exception {
    public InvalidHashFunctionException() {
        super("Hash function provided is invalid. Use either SHA-1 or MD-5");
    }

    public InvalidHashFunctionException(String message) {
        super(message);
    }
}
