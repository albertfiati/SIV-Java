package com.albertfiati;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        //String mode = "init";
        String mode = "verify";
        String reportFilePath = "/Users/optimistic/Downloads/results/report.json";
        String verificationFilePath = "/Users/optimistic/Downloads/results/verification.json";
        String monitoringDirectoryPath = "/Users/optimistic/Downloads/virt";

        String hashFunction = "sha-1";

        SIV siv = new SIV();

        try {
            System.out.println(String.format("SIV mode: %s", mode));

            switch (mode.toLowerCase()) {
                case "init":
                    siv.initialize(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction);
                    break;
                case "verify":
                    siv.verify(verificationFilePath, reportFilePath);
                    break;
                default:
                    System.out.println("Invalid mode specified");
                    break;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.print(exception.getMessage());
        }
    }
}
