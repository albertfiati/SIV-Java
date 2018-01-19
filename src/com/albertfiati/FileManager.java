package com.albertfiati;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class FileManager {
    public static void write(String reportFilePath, JSONObject jsonObject) throws Exception {
        try (FileWriter fileWriter = new FileWriter(reportFilePath)) {
            fileWriter.write(jsonObject.toJSONString());
        } catch (IOException e) {
            throw new Exception("File writer failed");
        }
    }

    public static boolean createFile(String filepath) throws IOException {
        File file = new File(filepath);
        return file.createNewFile();
    }
}
