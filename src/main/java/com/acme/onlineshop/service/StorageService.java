package com.acme.onlineshop.service;

import com.acme.onlineshop.exception.StorageException;
import com.acme.onlineshop.exception.StorageFileNotFoundException;
import com.acme.onlineshop.persistence.user.User;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Abstract parent class for uploading arbitrary files
 */
public abstract class StorageService {

    protected Path rootLocation;

    protected StorageService(Path rootLocation) {
        this.rootLocation = rootLocation;
    }

    protected void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Won't store empty files.");
        }
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    public void store(MultipartFile file, User user) throws StorageException {
        validateUser(user, false);
        validateFile(file);
        try {
            if (file.getOriginalFilename() != null) {
                Path destinationFile = this.rootLocation.resolve(Paths.get(user.getUsername(), file.getOriginalFilename())).normalize().toAbsolutePath();
                if (!destinationFile.startsWith(this.rootLocation)) {
                    // This is a security check
                    throw new StorageException("Cannot store file outside pre defined directory: " + rootLocation);
                }
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                throw new StorageException("File has no original file name: " + file);
            }
        } catch (IOException exc) {
            throw new StorageException("Failed to store file.", exc);
        }
    }

    private void validateUser(User user, boolean download) {
        if(user == null) {
            throw new StorageException("Anonymous users are NOT allowed to upload/download any files.");
        }
        //TODO: check if user has enough space for additional pictures. Lets say..., 1-2 MB ?!?
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 2).filter(path -> path.toFile().isFile()).map(this.rootLocation::relativize);
        } catch (IOException exc) {
            throw new StorageException("Failed to read stored files", exc);
        }
    }

    public Stream<Path> loadAll(User user) {
        if(user == null) {
            return Stream.empty();
        } else {
            try {
                return Files.walk(Paths.get(this.rootLocation.toString(), user.getUsername()), 1).filter(path -> path.toFile().isFile()).map(this.rootLocation::relativize);
            } catch (IOException exc) {
                throw new StorageException("Failed to read stored files", exc);
            }
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public Resource loadAsResource(String filename) {
        Path file = load(filename);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException exc) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, exc);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    public void deleteAll(User user) {
        validateUser(user, true);
        FileSystemUtils.deleteRecursively(Paths.get(rootLocation.toString(), user.getUsername()).toFile());
    }
}
