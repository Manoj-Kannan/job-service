package com.omega.jobservice.util;

import java.io.File;

public class FileUtils {
    public static File getConfFile(String filePath) {
        return new File(filePath);
    }
}
