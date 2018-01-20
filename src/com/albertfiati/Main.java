package com.albertfiati;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String monitoringDirectoryPath = "/Users/optimistic/Downloads/test";
//        String monitoringDirectoryPath = "C:\\Users\\Optimistic\\Downloads";

        String reportFilePath = "/Users/optimistic/Downloads/report.txt";
        String verificationFilePath = "/Users/optimistic/Downloads/verification.txt";

//        String reportFilePath = "C:\\Users\\Optimistic\\Downloads\\monitoringDir\\report.txt";
//        String verificationFilePath = "C:\\Users\\Optimistic\\Downloads\\monitoringDir\\verification.txt";

        String hashFunction = "sha-1";

        SIV siv = new SIV();

        try {
            siv.initialize(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction);
            siv.verify(verificationFilePath, reportFilePath);
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.print(exception.getMessage());
        }
    }
}
