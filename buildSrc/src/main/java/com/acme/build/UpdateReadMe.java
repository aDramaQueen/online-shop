package com.acme.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UpdateReadMe extends BaseBuildUtility {

    private final static String IDENTIFIER = "### Current version: ";

    public static void updateReadMe(File readMe, String version) throws IOException {
        boolean found = false;
        StringBuilder stringBuilder = new StringBuilder();
        for(String line: Files.readAllLines(readMe.toPath(), charset)) {
            if(line.startsWith(IDENTIFIER)) {
                stringBuilder.append(IDENTIFIER).append(version).append("\n");
                found = true;
            } else {
                stringBuilder.append(line).append("\n");
            }
        }
        if(found) {
            writeToFileByReplace(readMe, stringBuilder);
        } else {
            throw new IllegalStateException("Couldn't find identifier '%s' in README: %s".formatted(IDENTIFIER, readMe));
        }
    }
}
