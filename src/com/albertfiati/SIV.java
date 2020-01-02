package com.albertfiati;

import com.albertfiati.Exceptions.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SIV {
    private String[] ALLOWED_HASH_FUNCTIONS = {"SHA1", "MD5"};
    private String hashFunction;
    private JSONObject monitoringDirectoryJsonObject = new JSONObject();
    private JSONObject modifiedMonitoringDirectoryJsonObject = new JSONObject();
    private Integer noOfDirectoriesParsed = 0;
    private Integer noOfFilesParsed = 0;
    private Integer noOfWarningsIssued = 0;
    private MetaData fileMetaData = new MetaData();
    private Scanner in = new Scanner(System.in);
    private JSONObject verificationJSONObject;
    private JSONObject reportJSONObject;

    public void initialize(String monitoringDirectoryPath, String verificationFilePath, String reportFilePath, String hashFunction) throws Exception {
        long startTime = System.nanoTime();

        runChecks(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction, true);

        //iteratively go through the monitoring directory and update json
        parseMonitoringDirectory(monitoringDirectoryPath, "init");

        //add hash_function to json object
        monitoringDirectoryJsonObject.put("hash_function", hashFunction);

        //write verification file
        FileManager.write(verificationFilePath, monitoringDirectoryJsonObject);

        //write report file
        writeReportFile(verificationFilePath, reportFilePath, startTime, monitoringDirectoryPath, true);

        print("Done initializing SIV");
    }

    private String getHashFunctionFromFile(String filePath, String key) throws IOException, ParseException {
        String hashFunction;

        if (exists(filePath)) {
            verificationJSONObject = readJSONObjectFromFile(filePath);
            hashFunction = verificationJSONObject.get(key).toString();

            if (hashFunction != null) {
                return hashFunction;
            }
        }

        return null;
    }

    private String getHashFunction(String hashFunction, String verificationFilePath) throws InvalidHashFunctionException, IOException, ParseException {
        String hashFunctionOnFile = getHashFunctionFromFile(verificationFilePath, "hash_function");

        if (hashFunction != null && !hashFunction.equals("")) {
            if (hashFunction.equals(hashFunctionOnFile)) {
                return hashFunction;
            }

            throw new InvalidHashFunctionException("The provided hash function is different from what is on file");
        } else {
            return hashFunctionOnFile;
        }
    }

    public void verify(String monitoringDirectoryPath, String verificationFilePath, String reportFilePath, String hashFunction) throws Exception {
        long startTime = System.nanoTime();

        //set hash function
        hashFunction = getHashFunction(hashFunction, verificationFilePath);

        System.out.println(hashFunction);

        //run check
        runChecks(monitoringDirectoryPath, verificationFilePath, reportFilePath, hashFunction, false);

        //read the verification file into a json object
        verificationJSONObject = readJSONObjectFromFile(verificationFilePath);

        parseMonitoringDirectory(monitoringDirectoryPath, "verify");

        //check for all deleted files
        checkForDeletedFiles();

        //write verification file
        // FileManager.write(verificationFilePath, modifiedMonitoringDirectoryJsonObject);

        //write report file
        writeReportFile(verificationFilePath, reportFilePath, startTime, monitoringDirectoryPath, false);

        print("Done verifying SIV");
    }

    private JSONObject readJSONObjectFromFile(String verificationFilePath) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(new FileReader(verificationFilePath));
    }

    private void runChecks(String monitoringDirectoryPath, String verificationFilePath, String reportFilePath, String hashFunction, boolean isInit) throws Exception {
        //check if the monitoring path is a Directory
        if (!exists(monitoringDirectoryPath))
            throw new DirectoryNotFoundException("The monitoring directory does not exist");

        //check if the monitoring directory exists
        if (!isDirectory(monitoringDirectoryPath))
            throw new NotADirectoryException("The monitoring path provided is not a directory");

        if (isInit) {
            //check if verification file already exists
            if (exists(verificationFilePath)) {
                if (overwriteFile("Verification"))
                    System.exit(-1);
            } else {
                FileManager.createFile(verificationFilePath);
            }
        }

        //check to ensure that the verification file is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, verificationFilePath))
            throw new InvalidFileException("The verification file is in the same monitoring directory");

        //check if report file already exists
        if (exists(reportFilePath)) {
            if (overwriteFile("Report"))
                System.exit(-1);
        } else {
            FileManager.createFile(reportFilePath);
        }

        //check to ensure that the report files is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, reportFilePath))
            throw new InvalidFileException("The report file is in the same monitoring directory");

        if (hashFunction != null) {
            //verify if hash function is allowed
            this.hashFunction = hashFunction.toUpperCase().replace("-", "");

            if (!Arrays.asList(ALLOWED_HASH_FUNCTIONS).contains(this.hashFunction))
                throw new InvalidHashFunctionException();
        } else {
            throw new InsufficientArguementsException();
        }
    }

    private void writeReportFile(String verificationFilePath, String reportFilePath, long startTime,
                                 String monitoringDirectoryPath, boolean isInit) throws Exception {
        JSONObject reportJsonObject = new JSONObject();
        DecimalFormat df = new DecimalFormat("####0.00");

        reportJsonObject.put("hash_function", hashFunction);
        reportJsonObject.put("no_of_files_parsed", noOfFilesParsed);
        reportJsonObject.put("verification_directory", verificationFilePath);
        reportJsonObject.put("monitoring_directory", monitoringDirectoryPath);
        reportJsonObject.put("no_of_directories_parsed", noOfDirectoriesParsed);
        reportJsonObject.put("completion_time", df.format((System.nanoTime() - startTime) / 1000000.0));

        if (!isInit) {
            reportJsonObject.put("no_of_warnings_issued", noOfWarningsIssued);
            reportJsonObject.put("modified_files", modifiedMonitoringDirectoryJsonObject);
        }

        FileManager.write(reportFilePath, reportJsonObject);
        printReport(reportJsonObject);
    }

    private void printReport(JSONObject reportJSONObject) {
        print("");
        print("Printing report to screen");
        print("-------------------------");
        print("");

        Iterable<String> keys = reportJSONObject.keySet();

        for (String key : keys) {
            if (key.equals("completion_time"))
                print(String.format("%s : %s %s", key, reportJSONObject.get(key), " ms"));
            else
                print(String.format("%s : %s", key, reportJSONObject.get(key)));
        }

        print("");
    }

    private boolean overwriteFile(String fileType) throws Exception {
        System.out.print(String.format("%s file already exist. Enter 1 to overwrite and 0 to exit: ", fileType));

        try {
            int overwrite = in.nextInt();

            if (overwrite == 0) {
                print("SIV cancelled");
                return true;
            }
        } catch (Exception e) {
            throw new Exception("Invalid response provided");
        }

        return false;
    }


    /*
     *   check if a file or directory exists
     *
     *   @param string
     *   @return boolean
     * */
    private Boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /*
     *   check if a path is a directory
     *
     *   @param string
     *
     *   @return boolean
     * */
    private Boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    /*
     *   Verify if a file exists in the monitoring directory
     *
     *   @param String monitoringDirectory
     *   @param String file
     *
     *   @return Boolean
     * */
    private Boolean monitoringDirectoryIsParentOf(String monitoringDirectory, String file) {
        return file.replace("\\", "/").matches(String.format("^(%s).*$", monitoringDirectory.replace("\\", "/")));
    }

    /*
     *   parse the monitoring folder
     *
     *   @param String monitoringDirectoryFilePath
     * */
    private void parseMonitoringDirectory(String monitoringDirectoryFilePath, String mode) throws Exception {
        File directory = new File(monitoringDirectoryFilePath);

        for (File file : directory.listFiles()) {

            if (file.isDirectory()) {
                noOfDirectoriesParsed++;

                int hashCode = file.getAbsolutePath().hashCode();
                JSONObject fileMetaData = this.fileMetaData.parse(file, hashFunction, true);

                parseMonitoringDirectory(file.getPath(), mode);

                if (mode == "init") {
                    monitoringDirectoryJsonObject.put(hashCode, fileMetaData);
                } else {
                    verifySystemIntegrity(hashCode, fileMetaData);
                }
            } else {
                noOfFilesParsed++;
                int hashCode = file.getAbsolutePath().hashCode();
                JSONObject fileMetaData = this.fileMetaData.parse(file, hashFunction, false);

                if (mode == "init") {
                    monitoringDirectoryJsonObject.put(hashCode, fileMetaData);
                } else {
                    verifySystemIntegrity(hashCode, fileMetaData);
                }
            }
        }
    }

    /*
     *   run the system verifier on the files for changes and additions
     *
     *   @param  int hashCode
     *   @param  JSONObject fileMetaData
     *
     * */
    private void verifySystemIntegrity(int hashCode, JSONObject fileMetaData) {
        monitoringDirectoryJsonObject.put(hashCode, fileMetaData);
        JSONObject fileDataInVerificationFile = (JSONObject) verificationJSONObject.get(String.format("%d", hashCode));

        if (fileDataInVerificationFile != null) {
            // verifying metadata changes
            Iterable<String> keys = fileMetaData.keySet();
            Map<String, String> updateMap = new HashMap();

            for (String key : keys) {
                if (!fileMetaData.get(key).toString().equals(fileDataInVerificationFile.get(key).toString())) {
                    updateMap.put(String.format("%s_changed", key), "true");
                }
            }

            if (updateMap.size() > 0) {
                noOfWarningsIssued += updateMap.size();
                updateMap.put("status", "modified");
                updateMap.put("alert", "warning");
                fileMetaData.putAll(updateMap);

                modifiedMonitoringDirectoryJsonObject.put(hashCode, fileMetaData);
            }
        } else {
            // recording a new file
            noOfWarningsIssued++;
            fileMetaData.put("status", "new");
            fileMetaData.put("alert", "warning");

            modifiedMonitoringDirectoryJsonObject.put(hashCode, fileMetaData);
        }
    }

    private void checkForDeletedFiles() {
        Iterable<String> hashCodes = verificationJSONObject.keySet();

        JSONObject deletedFileJSON, readData;

        for (String hashCode : hashCodes) {
            if (!hashCode.equals("hash_function")) {
                readData = (JSONObject) monitoringDirectoryJsonObject.get(Integer.parseInt(hashCode));

                if (readData == null) {
                    noOfWarningsIssued++;
                    deletedFileJSON = (JSONObject) verificationJSONObject.get(hashCode);
                    deletedFileJSON.put("status", "deleted");
                    deletedFileJSON.put("alert", "warning");
                    modifiedMonitoringDirectoryJsonObject.put(hashCode, deletedFileJSON);
                }
            }
        }
    }

    private void print(String message) {
        System.out.println(message);
    }
}

