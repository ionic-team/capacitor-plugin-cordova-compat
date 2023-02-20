package com.getcapacitor;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Common File utilities, such as resolve content URIs and
 * creating portable web paths from low-level files
 */
public class FileUtils {
    public enum Type {
        IMAGE("image");

        private String type;

        Type(String type) {
            this.type = type;
        }
    }

    /**
     * Read a plaintext file.
     *
     * @param assetManager Used to open the file.
     * @param fileName The path of the file to read.
     * @return The contents of the file path.
     * @throws IOException Thrown if any issues reading the provided file path.
     */
    static String readFile(AssetManager assetManager, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(fileName)))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            return buffer.toString();
        }
    }
}
