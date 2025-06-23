package com.luksosilva.dbcomparator.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FileUtils {

    public static boolean areFilesEqual(File file1, File file2) {

        if (file1 == null || file2 == null) {
            return false;
        }

        return getCanonicalPath(file1).equals(getCanonicalPath(file2));

    }

    public static boolean areFileNamesEqual(File file1, File file2) {

        if (file1 == null || file2 == null) {
            return false;
        }

        return file1.getName().equals(file2.getName());

    }

    public static String getCanonicalPath(File file) {

        if (file == null) {
            throw new IllegalArgumentException("File cannot be null for getCanonicalPath.");
        }

        try{

            return file.getCanonicalPath();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDisplayPath(File file) {
        if (file == null) {
            return "";
        }

        final int MAX_DISPLAY_PATH_LENGTH = 40;

        String fullPath = file.getAbsolutePath();

        if (fullPath.length() <= MAX_DISPLAY_PATH_LENGTH) {
            return fullPath; // No truncation needed
        }

        String fileName = FilenameUtils.getName(fullPath);
        String parentPath = FilenameUtils.getPathNoEndSeparator(fullPath);

        // Find the root (e.g., "C:\", "/")
        String root = FilenameUtils.normalize(file.toPath().getRoot().toString());
        if (root == null) { // For cases like relative paths or non-rooted systems, though less common
            root = "";
        } else if (!root.endsWith(File.separator)) {
            root += File.separator; // Ensure "C:\" has a trailing separator
        }

        // Try to keep the last parent directory if possible
        String lastParentDir = FilenameUtils.getName(parentPath); // "Documents"
        String displayString = "";

        // Strategy 1: root + "..." + lastParentDir + filename
        String proposedPath1 = root + "..." + File.separator + lastParentDir + File.separator + fileName;
        if (proposedPath1.length() <= MAX_DISPLAY_PATH_LENGTH && !lastParentDir.isEmpty()) {
            displayString = proposedPath1;
        } else {
            // Strategy 2: If Strategy 1 is too long or no parent dir, just root + "..." + filename
            String proposedPath2 = root + "..." + File.separator + fileName;
            if (proposedPath2.length() <= MAX_DISPLAY_PATH_LENGTH) {
                displayString = proposedPath2;
            } else {
                // Strategy 3: If even that's too long, truncate from the end
                int startLength = Math.min(root.length(), 5); // Keep a bit of root
                int endLength = MAX_DISPLAY_PATH_LENGTH - startLength - 3; // 3 for "..."
                if (endLength < fileName.length()) { // Ensure we keep at least part of the file name
                    endLength = fileName.length();
                }
                displayString = root.substring(0, Math.min(startLength, root.length())) + "..." +
                        fullPath.substring(fullPath.length() - endLength);
            }
        }
        return displayString;
    }

}
