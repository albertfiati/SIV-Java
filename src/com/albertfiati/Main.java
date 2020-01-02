package com.albertfiati;

import com.albertfiati.Exceptions.InsufficientArguementsException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    enum Modes {
        INIT,
        VERIFY
    }

    private static String mode = "help";
    private static String hashFunction = null;
    private static String reportFilePath = null;
    private static String verificationFilePath = null;
    private static String monitoringDirectoryPath = null;

    // initializing hashmap
    private static Map<String, String> allowedModes;

    static {
        allowedModes = new HashMap<>();
        allowedModes.put("-h", "help");
        allowedModes.put("-i", "init");
        allowedModes.put("-v", "verify");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("");

        /*String[] opts = new String[9];
        opts[0] = "-v";
        opts[1] = "-D";
        opts[2] = "/Users/optimistic/Desktop/sivmon";
        opts[3] = "-V";
        opts[4] = "/Users/optimistic/Desktop/verification.json";
        opts[5] = "-R";
        opts[6] = "/Users/optimistic/Desktop/report.json";
        opts[7] = "-H";
        opts[8] = "md-5";*/

        //create an instance of the SIV object
        SIV siv = new SIV();

        try {
            // read arguments submitted and parse them
            readOptions(args);
            //readOptions(opts);
        } catch (InsufficientArguementsException ex) {
            System.out.println("Reading options Error: Insufficient options\n");
            printHelp();
        }

        try {
            if (reportFilePath != null && verificationFilePath != null && monitoringDirectoryPath != null) {
                if (!reportFilePath.equals("") && !verificationFilePath.equals("") && !monitoringDirectoryPath.equals("")) {
                    Modes selectedMode = Modes.valueOf(mode.toUpperCase());
                    System.out.println(String.format("SIV mode: %s", selectedMode));

                    switch (selectedMode) {
                        case INIT:
                            if (!hashFunction.equals(""))
                                siv.initialize(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction);
                            else
                                printHelp();

                            break;
                        case VERIFY:
                            siv.verify(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction);
                            break;
                        default:
                            printHelp();
                            break;
                    }
                }
            } else {
                printHelp();
            }
        } catch (Exception exception) {
            // exception.printStackTrace();
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
        System.out.println("      sha-1");
        System.out.println("      md-5");
        System.out.println("");
        System.out.println("  To run in verification mode run");
        System.out.println("    siv -v -D monitoring_directory_path -V verification_file_path -R report_file_path -H hash_function");
    }

    //loading params into the the needed variables for SIV
    private static void readOptions(String[] args) throws InsufficientArguementsException {
        System.out.println("Reading options ");

        if (args.length == 0 || (args.length == 1 && !args[0].equals("-h")))
            throw new InsufficientArguementsException();

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
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
}
