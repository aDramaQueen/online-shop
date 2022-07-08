package com.acme.onlineshop;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Util {

    public enum FileName {
        SPRING_TEST_PROPERTIES("/application.properties"),
        INFLUX_TEST_PROPERTIES("/secrets/influx-test.properties"),
        JWT_ENCRYPTION_KEY("/secrets/jwt.properties");

        private final String fileName;

        FileName(String fileName) {
            this.fileName = fileName;
        }

        public String getPath(){
            return fileName;
        }
    }

    public static URL loadFile(FileName file) throws FileNotFoundException {
        URL result = MainTest.class.getResource(file.getPath());
        if(result != null) {
            return result;
        } else {
            throw new FileNotFoundException("Couldn't find file: "+file.getPath());
        }
    }

    public static String readFile(FileName fileName) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(loadFile(fileName).getPath()))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
