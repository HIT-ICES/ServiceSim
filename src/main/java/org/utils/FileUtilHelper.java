package org.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileUtilHelper
 * 
 * @since 2024-11-27
 * @version 1.0
 * @author wutongshufqw
 */
public class FileUtilHelper {
    private static final Logger logger = LoggerFactory.getLogger(FileUtilHelper.class);

    /**
     * Check whether the directory exists
     * 
     * @param path the directory path
     * @return true if the directory exists, otherwise false
     */
    public static boolean isExistDirectory(String path) {
        File directory = new File(path);
        return directory.exists() && directory.isDirectory();
    }

    /**
     * Check whether the file exists
     * 
     * @param path the file path
     * @return true if the file exists, otherwise false
     */
    public static boolean isExistFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Check whether the directory is empty
     * 
     * @param path the directory path
     * @return true if the directory is empty, otherwise false
     */
    public static boolean isEmptyDirectory(String path) {
        File directory = new File(path);
        return directory.exists() && directory.isDirectory() && directory.list().length == 0;
    }

    /**
     * Checks whether specified files exist in the directory
     * 
     * @param path     the directory path
     * @param searchPattern the file name
     * @return true if the directory contains the file, otherwise false
     */
    public static boolean isContainFile(String path, String searchPattern) {
        return isContainFile(path, searchPattern, false);
    }

    /**
     * Checks whether specified files exist in the directory
     * 
     * @param path          the directory path
     * @param searchPattern the file name
     * @param isSearchChild whether to search subdirectories
     * @return true if the directory contains the file, otherwise false
     */
    public static boolean isContainFile(String path, String searchPattern, boolean isSearchChild) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        Pattern pattern = Pattern.compile(searchPattern);
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && isSearchChild) {
                if (isContainFile(file.getAbsolutePath(), searchPattern, isSearchChild)) {
                    return true;
                }
            } else if (pattern.matcher(file.getName()).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * create a directory
     * 
     * @param path the directory path
     * @return true if the directory is created successfully, otherwise false
     */
    public static boolean createDirectory(String path) {
        File directory = new File(path);
        return directory.mkdirs();
    }

    /**
     * create a file
     * 
     * @param path the file path
     * @return true if the file is created successfully, otherwise false
     */
    public static boolean createFile(String path) {
        try {
            if (isExistFile(path)) {
                return true;
            }
            File file = new File(path);
            String parentPath = file.getParent();
            if (!isExistDirectory(parentPath)) {
                createDirectory(parentPath);
            }
            return file.createNewFile();
        } catch (IOException e) {
            logger.error("create file failed", e);
            return false;
        }
    }

    /**
     * create a file with data
     * 
     * @param path the file path
     * @param buffer the data
     * @return true if the file is created successfully, otherwise false
     */
    public static boolean createFile(String path, byte[] buffer) {
        if (!createFile(path)) {
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(buffer);
            return true;
        } catch (IOException e) {
            logger.error("write file failed", e);
            return false;
        }
    }

    /**
     * get the line count of the file
     *
     * @param path the file path
     * @return the line count of the file
     */
    public static int getLineCount(String path) {
        if (!isExistFile(path)) {
            return -1;
        }
        File file = new File(path);

        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            logger.error("read file failed", e);
            return -1;
        }
        return lineCount;
    }

    /**
     * get the size of the file
     *
     * @param path the file path
     * @return the size of the file
     */
    public static long getFileSize(String path) {
        if (!isExistFile(path)) {
            return -1;
        }
        File file = new File(path);
        return file.length();
    }

    /** 
     * get file name list of the directory
     * 
     * @param path the directory path
     * @return the file list of the directory
     */
    public static List<String> getFileNameList(String path) {
        return getFileNameList(path, ".*");
    }

    /**
     * get file name list of the directory
     * 
     * @param path the directory path
     * @param searchPattern the file name
     * @return the file list of the directory
     */
    public static List<String> getFileNameList(String path, String searchPattern) {
        return getFileNameList(path, searchPattern, false);
    }

    /**
     * get file name list of the directory
     * 
     * @param path          the directory path
     * @param searchPattern the file name
     * @param isSearchChild whether to search subdirectories
     * @return the file list of the directory
     */
    public static List<String> getFileNameList(String path, String searchPattern, boolean isSearchChild) {
        if (!isExistDirectory(path)) {
            return null;
        }
        File directory = new File(path);
        Pattern pattern = Pattern.compile(searchPattern);
        List<String> fileNameList = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && isSearchChild) {
                fileNameList.addAll(getFileNameList(file.getAbsolutePath(), searchPattern, isSearchChild).stream()
                        .map(fileName -> file.getName() + File.separator + fileName).toList());
            } else if (pattern.matcher(file.getName()).find()) {
                fileNameList.add(file.getName());
            }
        }
        return fileNameList;
    }
    
    /**
     * write string to file
     * 
     * @param path the directory path
     * @param content the content
     * @return true if the content is written successfully, otherwise false
     */
    public static boolean WriteText(String path, String content) {
        return WriteText(path, content, false);
    }

    /**
     * write string to file
     * 
     * @param path the directory path
     * @param content the content
     * @param isAppend whether to append the content
     * @return true if the content is written successfully, otherwise false
     */
    public static boolean WriteText(String path, String content, boolean isAppend) {
        if (!createFile(path)) {
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(path, isAppend)) {
            fos.write(content.getBytes());
            return true;
        } catch (IOException e) {
            logger.error("write file failed", e);
            return false;
        }
    }

    /**
     * copy file
     * 
     * @param res the source file path
     * @param dest the destination file path
     * @return true if the file is copied successfully, otherwise false
     */
    public static boolean copyFile(String res, String dest) {
        if (!isExistFile(res)) {
            return false;
        }
        if (!createFile(dest)) {
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            try (FileInputStream fis = new FileInputStream(res)) {
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("copy file failed", e);
            return false;
        }
    }

    /**
     * move file
     * 
     * @param res  the source file path
     * @param dest the destination file path
     * @return true if the file is moved successfully, otherwise false
     */
    public static boolean moveFile(String res, String dest) {
        if (!copyFile(res, dest)) {
            return false;
        }
        File file = new File(res);
        return file.delete();
    }

    /**
     * get the name of the file from the path
     * 
     * @param path the file path
     * @return the name of the file
     */
    public static String getFileName(String path) {
        return getFileName(path, true);
    }

    /**
     * get the name of the file from the path
     * 
     * @param path the file path
     * @param extension whether to get the extension
     * @return the name of the file
     */
    public static String getFileName(String path, boolean extension) {
        if (!isExistFile(path)) {
            return null;
        }
        File file = new File(path);
        String fileName = file.getName();
        if (!extension) {
            int index = fileName.lastIndexOf(".");
            if (index != -1) {
                fileName = fileName.substring(0, index);
            }
        }
        return fileName;
    }

    /**
     * get the extension of the file from the path
     * 
     * @param path the file path
     * @return the extension of the file
     */
    public static String getFileExtension(String path) {
        if (!isExistFile(path)) {
            return null;
        }
        File file = new File(path);
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1);
        }
        return null;
    }

    /**
     * read file into byte array
     * 
     * @param path the file path
     * @return the byte array of the file
     */
    public static byte[] readFileBytes(String path) {
        if (!isExistFile(path)) {
            return null;
        }
        File file = new File(path);
        byte[] buffer = new byte[(int) getFileSize(path)];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
            return buffer;
        } catch (IOException e) {
            logger.error("read file failed", e);
            return null;
        }
    }

    /**
     * read file into String
     * 
     * @param path the file path
     * @return the byte array of the file
     */
    public static String readFileText(String path) {
        if (!isExistFile(path)) {
            return null;
        }
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("read file failed", e);
            return null;
        }
    }

    /**
     * delete directory (only the files in the directory)
     * 
     * @param path the directory path
     * @return true if the directory is deleted successfully, otherwise false
     */
    public static boolean deleteDirectory(String path) {
        if (!isExistDirectory(path)) {
            return false;
        }
        File directory = new File(path);
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file.getAbsolutePath());
            } else {
                deleteFile(file.getAbsolutePath());
            }
        }
        return true;
    }

    /**
     * delete file content
     * 
     * @param path the file path
     * @return true if the file is deleted successfully, otherwise false
     */
    public static boolean deleteFileContent(String path) {
        if (!isExistFile(path)) {
            return false;
        }
        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[0]);
            return true;
        } catch (IOException e) {
            logger.error("delete file content failed", e);
            return false;
        }
    }

    /**
     * delete file
     * 
     * @param path the file path
     * @return true if the file is deleted successfully, otherwise false
     */
    public static boolean deleteFile(String path) {
        if (!isExistFile(path)) {
            return false;
        }
        File file = new File(path);
        return file.delete();
    }

    /**
     * delete file or directory
     * 
     * @param path the file or directory path
     * @return true if the file or directory is deleted successfully, otherwise false
     */
    public static boolean delete(String path) {
        if (isExistDirectory(path)) {
            return deleteDirectory(path);
        } else if (isExistFile(path)) {
            return deleteFile(path);
        }
        return false;
    }

    /**
     * rename file
     * 
     * @param path the file path
     * @param newName the new name
     * @return true if the file is renamed successfully, otherwise false
     */
    public static boolean renameFile(String path, String newName) {
        if (!isExistFile(path)) {
            return false;
        }
        File file = new File(path);
        String parentPath = file.getParent();
        return file.renameTo(new File(parentPath + File.separator + newName));
    }

    /**
     * rename directory
     * 
     * @param path the directory path
     * @param newName the new name
     * @return true if the directory is renamed successfully, otherwise false
     */
    public static boolean renameDirectory(String path, String newName) {
        if (!isExistDirectory(path)) {
            return false;
        }
        File directory = new File(path);
        String parentPath = directory.getParent();
        return directory.renameTo(new File(parentPath + File.separator + newName));
    }

    /**
     * rename file or directory
     * 
     * @param path the file or directory path
     * @param newName the new name
     * @return true if the file or directory is renamed successfully, otherwise false
     */
    public static boolean rename(String path, String newName) {
        if (isExistDirectory(path)) {
            return renameDirectory(path, newName);
        } else if (isExistFile(path)) {
            return renameFile(path, newName);
        }
        return false;
    }

    /**
     * convert Byte to KB, MB, GB, TB
     * 
     * @param size the size in Byte
     * @return the size in KB, MB, GB, TB
     */
    public static String convertSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / 1024.0 / 1024.0);
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2fGB", size / 1024.0 / 1024.0 / 1024.0);
        } else {
            return String.format("%.2fTB", size / 1024.0 / 1024.0 / 1024.0 / 1024.0);
        }
    }

    /** 
     * convert KB, MB, GB, TB to Byte
     * 
     * @param size the size in KB, MB, GB, TB
     * @return the size in Byte
     */
    public static long convertSize(String size) {
        if (size.endsWith("KB")) {
            return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024);
        } else if (size.endsWith("MB")) {
            return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024 * 1024);
        } else if (size.endsWith("GB")) {
            return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024);
        } else if (size.endsWith("TB")) {
            return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024 * 1024);
        } else {
            return Long.parseLong(size.substring(0, size.length() - 1));
        }
    }

    /**
     * replace illegal characters in the file name
     * 
     * @param fileName the file name
     * @return the file name without illegal characters
     */
    public static String replaceIllegalCharacter(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * get the resource path
     * 
     * @return the resource path
     */
    public static String getResourcePath() {
        return FileUtilHelper.class.getClassLoader().getResource("").getPath();
    }

    /**
     * get the resource path
     * 
     * @param path the resource path
     * @return the resource path
     */
    public static String getResourcePath(String path) {
        return FileUtilHelper.class.getClassLoader().getResource(path).getPath();
    }

    /**
     * add the path separator
     * 
     * @param paths the path
     * @return the path with separator
     */
    public static String append(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            sb.append(paths[i]);
            if (i != paths.length - 1) {
                sb.append(File.separator);
            }
        }
        return sb.toString();
    }
}