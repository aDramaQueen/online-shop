package com.acme.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UpdateBanner extends BaseBuildUtility {

    private final static String text = """
            Version:
                %s
            Copyright:
                %s
            """;

    public static void updateBanner(File sourceBanner, File targetBanner, String version, String license) throws IOException {
        if(sourceBanner == null || targetBanner == null) {
            throw new NullPointerException("There is no banner file path defined!");
        } else {
            StringBuilder plainBanner = readBanner(sourceBanner);
            plainBanner.append("\n").append(text.formatted(version, license));
            if(!targetBanner.exists()) {
                targetBanner.createNewFile();
            }
            writeToFileByReplace(targetBanner, plainBanner);
        }
    }

    private static StringBuilder readBanner(File banner) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(banner, charset));
        reader.lines().forEach(line -> stringBuilder.append(line).append("\n"));
        return stringBuilder;
    }
}
