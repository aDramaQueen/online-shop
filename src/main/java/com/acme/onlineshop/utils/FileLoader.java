package com.acme.onlineshop.utils;

import com.acme.onlineshop.Main;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * This class loads files from within the application.
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public class FileLoader {

    private FileLoader() { }

    private static final Set<String> binaryDirectories;

    static {
        binaryDirectories = new HashSet<>();
        binaryDirectories.add("bin"); // Maven, Eclipse & Visual Code
        binaryDirectories.add("out"); // IntelliJ
        binaryDirectories.add("build"); // Gradle
    }

    /**
     * Returns the root directory of this application as absolute path.
     *  <ul>
     *   <li>If it runs in an IDE it will return the root folder of this project</li>
     *   <li>If it runs as JAR it will return the folder where the JAR is located</li>
     * </ul>
     * <p>
     * (See also: <a href="https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/system/ApplicationHome.html">Spring Boot Documentation - ApplicationHome</a>)
     *
     * @return Path to working directory of operating system, which is considered as root
     */
    public static Path getRootDirectory() {
        ApplicationHome home = new ApplicationHome(Main.class);
        Path result = home.getDir().toPath();
        if (home.getSource().isFile()) {
            return result;
        } else {
            while (!binaryDirectories.contains(result.getFileName().toString())) {
                result = result.getParent();
            }
            return result.getParent();
        }
    }

    public static File loadFile(FileLocation file) {
        return new File(getRootDirectory().toString(), file.location);
    }

    public static URL loadEmbeddedFile(FileLocation file) {
        return Main.class.getResource(file.location);
    }

    public static boolean isJar() {
        ApplicationHome home = new ApplicationHome(Main.class);
        if (home.getSource() == null) {
            return false; // For unit tests
        } else {
            return new ApplicationHome(Main.class).getSource().isFile();
        }
    }
}
