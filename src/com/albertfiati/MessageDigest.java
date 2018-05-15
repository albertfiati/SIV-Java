package com.albertfiati;

import com.albertfiati.Exceptions.InvalidHashFunctionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MessageDigest {

    /*
     *   Generate the hash of the file content
     *
     *   @param String filepath
     *   @param String hashFunction
     *
     *   @return String digest
     * */
    public String generateHashOfFile(File file, String hashFunction) throws Exception {
        //hashFunction = hashFunction;

        byte[] digest;
        java.security.MessageDigest messageDigest = null;

        switch (hashFunction.toUpperCase()) {
            case "SHA-1":
                messageDigest = java.security.MessageDigest.getInstance("SHA-1");
                break;
            case "MD-5":
                messageDigest = java.security.MessageDigest.getInstance("MD5");
                break;
            default:
                throw new InvalidHashFunctionException();
        }

        return generateChecksum(messageDigest, file);
    }

    /*
     *   generate the checksum for the file
     *
     *   @param MessageDigest messageDigest
     *   @param File file
     *
     *   @return string checksum
     * */
    private String generateChecksum(java.security.MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fileInputStream = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fileInputStream.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fileInputStream.close();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        //Get the hash's bytes
        byte[] bytes = digest.digest();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }
}
