package com.albertfiati;

import com.albertfiati.Exceptions.InsufficientArguementsException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static String mode = "help";
    public static String hashFunction = "";
    public static String reportFilePath = "";
    public static String verificationFilePath = "";
    public static String monitoringDirectoryPath = "";

    public static Map<String, String> allowedModes;

    static {
        allowedModes = new HashMap<>();
        allowedModes.put("-h", "help");
        allowedModes.put("-i", "init");
        allowedModes.put("-v", "verify");
    }

    public static void main(String[] args) throws IOException {
        /*
        String[] args = new String[9];
        args[0] = "-i";
        args[1] = "-D";
        args[2] = "/Users/optimistic/Downloads/linux-4.16.5";
        args[3] = "-V";
        args[4] = "/Users/optimistic/Downloads/results/verification.json";
        args[5] = "-R";
        args[6] = "/Users/optimistic/Downloads/results/report.json";
        args[7] = "-H";
        args[8] = "sha-1";
        readOptions(args);
        */

        System.out.println("");

        //create an instance of the SIV object
        SIV siv = new SIV();

        try {
            //read arguments submitted and parse them
            readOptions(args);

            System.out.println(String.format("SIV mode: %s", mode));

            switch (mode.toLowerCase()) {
                case "init":
                    if (reportFilePath != "" && verificationFilePath != "" && monitoringDirectoryPath != "" && hashFunction != "")
                        siv.initialize(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction);
                    else
                        printHelp();

                    break;
                case "verify":
                    if (reportFilePath != "" && verificationFilePath != "" && monitoringDirectoryPath != "")
                        siv.verify(verificationFilePath, reportFilePath);
                    else {
                        printHelp();
                    }
                    break;
                default:
                    printHelp();
                    break;
            }
        } catch (Exception exception) {
            System.out.println("ERROR:: " + exception.getMessage());
        }

        System.out.println("");
    }

    //display help
    private static void printHelp() {
        System.out.println("");
        System.out.println("** SIV help **");
        System.out.println("  To get help run");
        System.out.println("    siv -h");
        System.out.println("");
        System.out.println("  To run in initialization mode run");
        System.out.println("    siv -i -D monitoring_directory_path -V verification_file_path -R report_file_path -H hash_function");
        System.out.println("");
        System.out.println("    The SIV supports the following hash functions:");
        System.out.println("      sha-a");
        System.out.println("      md-5");
        System.out.println("");
        System.out.println("  To run in verification mode run");
        System.out.println("    siv -v -D monitoring_directory_path -V verification_file_path -R report_file_path");
    }

    //loading params into the the needed variables for SIV
    private static void readOptions(String[] args) throws InsufficientArguementsException {
        if (args.length == 0 || (args.length == 1 && !args[0].equals("-h")))
            throw new InsufficientArguementsException();

        for (int i = 0; i < args.length; i++) {
            //setting mode
            if (allowedModes.containsKey(args[i])) {
                mode = allowedModes.get(args[i]);
            } else if (args[i].equals("-D")) {
                monitoringDirectoryPath = args[i + 1];
                i++;
            } else if (args[i].equals("-V")) {
                verificationFilePath = args[i + 1];
                i++;
            } else if (args[i].equals("-R")) {
                reportFilePath = args[i + 1];
                i++;
            } else if (args[i].equals("-H")) {
                hashFunction = args[i + 1];
                i++;
            }
        }
    }
}
