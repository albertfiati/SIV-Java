package com.albertfiati;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;

public class MetaData {
    private MessageDigest messageDigest = new MessageDigest();

    public JSONObject parse(File file, String hashFunction) throws Exception {
        JSONObject filePropertyObject = new JSONObject();

        Path filePath = Paths.get(file.getPath());
        BasicFileAttributes fileAttributes = null;

        try {
            fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            String path = file.getAbsolutePath();

            filePropertyObject.put("file_name", file.getName());
            filePropertyObject.put("path", path);

            if (!System.getProperty("os.name").toLowerCase().equals("windows 10")) {
                PosixFileAttributes posixFileAttributes = Files.readAttributes(filePath, PosixFileAttributes.class);

                filePropertyObject.put("owner", posixFileAttributes.owner());
                filePropertyObject.put("group", posixFileAttributes.group());
                filePropertyObject.put("permissions", posixFileAttributes.permissions());
            }

            filePropertyObject.put("size", fileAttributes.size());
            filePropertyObject.put("digest", messageDigest.generateHashOfFile(path, hashFunction));
            filePropertyObject.put("last_modification_date", fileAttributes.lastModifiedTime().toString());
            filePropertyObject.put("created_date", fileAttributes.creationTime().toString());

            return filePropertyObject;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
