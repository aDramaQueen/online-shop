package com.acme.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseBuildUtility {

    protected final static Charset charset = StandardCharsets.UTF_8;

    protected static void writeToFileByAppend(File targetFile, String content) throws IOException {
        FileWriter fw = new FileWriter(targetFile, charset, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }

    protected static void writeToFileByAppend(File targetFile, StringBuilder content) throws IOException {
        writeToFileByAppend(targetFile, content.toString());
    }

    protected static void writeToFileByReplace(File targetFile, String content) throws IOException {
        FileWriter fw = new FileWriter(targetFile, charset, false);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }

    protected static void writeToFileByReplace(File targetFile, StringBuilder content) throws IOException {
        writeToFileByReplace(targetFile, content.toString());
    }
}
