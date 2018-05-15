package com.albertfiati;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

public class MetaData {
    private MessageDigest messageDigest = new MessageDigest();

    public JSONObject parse(File file, String hashFunction, boolean isDirectory) throws Exception {
        JSONObject filePropertyObject = new JSONObject();

        Path filePath = Paths.get(file.getPath());
        BasicFileAttributes fileAttributes = null;

        //try {
        fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
        String path = file.getAbsolutePath();

        filePropertyObject.put("file_name", file.getName());
        filePropertyObject.put("path", path);

        if (!System.getProperty("os.name").toLowerCase().equals("windows 10")) {
            PosixFileAttributes posixFileAttributes = Files.readAttributes(filePath, PosixFileAttributes.class);

            filePropertyObject.put("owner", posixFileAttributes.owner().toString());
            filePropertyObject.put("group", posixFileAttributes.group().toString());
            filePropertyObject.put("access_rights", PosixFilePermissions.toString(posixFileAttributes.permissions()));
        }

        filePropertyObject.put("size", fileAttributes.size());
        filePropertyObject.put("last_modification_date", fileAttributes.lastModifiedTime().toString());
        filePropertyObject.put("created_date", fileAttributes.creationTime().toString());

        if (isDirectory) {
            filePropertyObject.put("no_of_files", file.listFiles().length);
            filePropertyObject.put("file_type", "dir");
        } else {
            filePropertyObject.put("digest", messageDigest.generateHashOfFile(file, hashFunction));
            filePropertyObject.put("file_type", "file");
        }

        return filePropertyObject;
        /*} catch (IOException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }*/
    }
}
