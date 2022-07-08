package com.acme.build;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeLogReader extends BaseBuildUtility {

    private final static Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    private final static String substring = "### Version:";

    public static String getCurrentVersion(File changeLog) throws IOException {
        if(changeLog == null) {
            throw new NullPointerException("There is no \"CHANGELOG.md\" file path defined!");
        } else {
            if (changeLog.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(changeLog, charset))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if(line.startsWith(substring)) {
                            String versionLine = br.readLine();
                            while (versionLine != null && versionLine.isBlank()) {
                                versionLine = br.readLine();
                            }
                            if(versionLine == null) {
                                throw new IllegalStateException("File '%s' does NOT hold a valid version format 'X.Y.Z'".formatted(changeLog.getAbsolutePath()));
                            } else {
                                Matcher match = VERSION_PATTERN.matcher(versionLine);
                                if (match.find()) {
                                    return match.group();
                                } else {
                                    throw new IllegalStateException("File '%s' does NOT hold a valid version format 'X.Y.Z'".formatted(changeLog.getAbsolutePath()));
                                }
                            }
                        }
                    }
                }
            } else {
                throw new FileNotFoundException("File '%s' does NOT exist!".formatted(changeLog.getAbsolutePath()));
            }
            throw new IllegalStateException("File '%s' does NOT hold key line: '%s'".formatted(changeLog.getAbsolutePath(), substring));
        }
    }
}
