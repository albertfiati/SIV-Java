package com.albertfiati;

import com.albertfiati.Exceptions.DirectoryNotFoundException;
import com.albertfiati.Exceptions.InvalidFileException;
import com.albertfiati.Exceptions.InvalidHashFunctionException;
import com.albertfiati.Exceptions.NotADirectoryException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SIV {
    private String[] ALLOWED_HASH_FUNCTIONS = {"SHA-1", "MD-5"};
    private String hashFunction;
    private JSONObject monitoringDirectoryJsonObject = new JSONObject();
    private Integer noOfDirectoriesParsed = 0;
    private Integer noOfFilesParsed = 0;
    private Integer noOfWarningsIssued = 0;
    private MetaData fileMetaData = new MetaData();
    private Scanner in = new Scanner(System.in);
    JSONObject verificationJSONObject;
    JSONParser jsonParser = new JSONParser();

    public void initialize(String monitoringDirectoryPath, String verificationFilePath, String reportFilePath, String hashFunction) throws Exception {
        long startTime = System.nanoTime();

        //check if the monitoring path is a Directory
        if (!exists(monitoringDirectoryPath))
            throw new DirectoryNotFoundException("The monitoring directory does not exist");

        //check if the monitoring directory exists
        if (!isDirectory(monitoringDirectoryPath))
            throw new NotADirectoryException("The monitoring path provided is not a directory");

        //check if verification file already exists
        if (exists(verificationFilePath)) {
            if (overwriteFile("Verification")) return;
        } else {
            FileManager.createFile(verificationFilePath);
        }

        //check to ensure that the verification file is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, verificationFilePath))
            throw new InvalidFileException("The verification file is in the same monitoring directory");

        //check if report file already exists
        if (exists(reportFilePath)) {
            if (overwriteFile("Report")) return;
        } else {
            FileManager.createFile(reportFilePath);
        }

        //check to ensure that the report files is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, reportFilePath))
            throw new InvalidFileException("The report file is in the same monitoring directory");

        //verify if hash function is allowed
        this.hashFunction = hashFunction.toUpperCase();

        if (!Arrays.asList(ALLOWED_HASH_FUNCTIONS).contains(this.hashFunction))
            throw new InvalidHashFunctionException();

        //iteratively go through the monitoring directory and update json
        parseMonitoringDirectory(monitoringDirectoryPath, "init");

        //write verification file
        FileManager.write(verificationFilePath, monitoringDirectoryJsonObject);

        //write report file
        JSONObject reportJsonObject = new JSONObject();
        reportJsonObject.put("monitoring_directory", monitoringDirectoryPath);
        reportJsonObject.put("verification_directory", verificationFilePath);
        reportJsonObject.put("no_of_directories_parsed", noOfDirectoriesParsed);
        reportJsonObject.put("no_of_files_parsed", noOfFilesParsed);
        reportJsonObject.put("completion_time", (System.nanoTime() - startTime) / 1000000000.0);
        reportJsonObject.put("hash_function", hashFunction);

        FileManager.write(reportFilePath, reportJsonObject);
        print("Done initializing SIV");
    }

    public void verify(String verificationFilePath, String reportFilePath) throws Exception {
        long startTime = System.nanoTime();

        //check if the verification file exists
        if (!exists(verificationFilePath))
            throw new InvalidFileException("Verification file does not exist");

        //check if the report file exists
        if (!exists(reportFilePath))
            throw new InvalidFileException("Verification file does not exist");

        //read the report file into a json object
        JSONParser jsonParser = new JSONParser();
        JSONObject reportJSONObject = (JSONObject) jsonParser.parse(new FileReader(reportFilePath));

        String monitoringDirectoryPath = reportJSONObject.get("monitoring_directory").toString();

        //ensure that the monitoring directory exists
        if (monitoringDirectoryPath == null && !exists(monitoringDirectoryPath))
            throw new DirectoryNotFoundException("Monitoring directory does not exist");

        //check to ensure that the verification file is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, verificationFilePath))
            throw new InvalidFileException("The verification file is in the same monitoring directory");

        //check to ensure that the report files is not in the monitoring directory
        if (monitoringDirectoryIsParentOf(monitoringDirectoryPath, reportFilePath))
            throw new InvalidFileException("The report file is in the same monitoring directory");

        //ensure that the verification path specified is the same as the one in the report file
        if (!verificationFilePath.equals(reportJSONObject.get("verification_directory").toString()))
            throw new InvalidFileException("Specified verification file path is different from the one specified in the report file");

        //read the verification file into a json object
        verificationJSONObject = (JSONObject) jsonParser.parse(new FileReader(verificationFilePath));

        //parse the monitoring folder for changes
        this.hashFunction = reportJSONObject.get("hash_function").toString().toUpperCase();

        //System.out.println("I am here");
        parseMonitoringDirectory(monitoringDirectoryPath, "verify");

        //check for all deleted files
        checkForDeletedFiles();

        //write verification file
        FileManager.write(verificationFilePath, monitoringDirectoryJsonObject);

        //write report file
        JSONObject reportJsonObject = new JSONObject();
        reportJsonObject.put("monitoring_directory", monitoringDirectoryPath);
        reportJsonObject.put("verification_directory", verificationFilePath);
        reportJsonObject.put("no_of_directories_parsed", noOfDirectoriesParsed);
        reportJsonObject.put("no_of_files_parsed", noOfFilesParsed);
        reportJsonObject.put("no_of_warnings_issued", noOfWarningsIssued);
        reportJsonObject.put("completion_time", (System.nanoTime() - startTime) / 1000000000.0);
        reportJsonObject.put("hash_function", hashFunction);

        FileManager.write(reportFilePath, reportJsonObject);

        print("Done verifying SIV");
    }

    private boolean overwriteFile(String fileType) throws Exception {
        System.out.print(String.format("%s file already exist. Enter 1 to overwrite and 0 to exit: ", fileType));

        try {
            int overwrite = in.nextInt();

            if (overwrite == 0) {
                print("Initialization cancelled");
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
        JSONObject fileDataInVerificationFile = (JSONObject) verificationJSONObject.get(String.format("%d", hashCode));

        if (fileDataInVerificationFile != null) {
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
            }
        } else {
            noOfWarningsIssued++;
            fileMetaData.put("status", "new");
            fileMetaData.put("alert", "warning");
        }

        monitoringDirectoryJsonObject.put(hashCode, fileMetaData);
    }

    private void checkForDeletedFiles() {
        Iterable<String> hashCodes = verificationJSONObject.keySet();

        JSONObject deletedFileJSON,
                readData;

        for (String hashCode : hashCodes) {
            readData = (JSONObject) monitoringDirectoryJsonObject.get(Integer.parseInt(hashCode));

            if (readData == null) {
                noOfWarningsIssued++;
                deletedFileJSON = (JSONObject) verificationJSONObject.get(hashCode);
                deletedFileJSON.put("status", "deleted");
                deletedFileJSON.put("alert", "warning");
                monitoringDirectoryJsonObject.put(hashCode, deletedFileJSON);
            }
        }
    }


    private void print(String message) {
        System.out.println(message);
    }
}
