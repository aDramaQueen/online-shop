package com.acme.onlineshop.service;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.exception.StorageException;
import com.acme.onlineshop.utils.validators.ResourceProperties;
import liquibase.pro.packaged.M;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@DependsOn("ResourcePropertiesTest")
public class ImageService extends StorageService {

    public ImageService() {
        super(ApplicationConfiguration.getMediaRootDirectory());
    }

    /**
     * List of MIME types for different image files
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#image_types">MDN - Web DOCs</a>
     */
    private final static Set<String> allowedImageFormats = Set.of(
            "image/bmp", "image/jpeg", "image/png", "image/svg+xml"
    );

    /**
     * Validates if given file is a real image file or not.
     *
     * @param file Uploaded file
     */
    @Override
    protected void validateFile(MultipartFile file) {
        super.validateFile(file);
        try {
            Tika tika = new Tika();
            String detectedMimeType = tika.detect(file.getBytes());
            if (!allowedImageFormats.contains(detectedMimeType)) {
                throw new StorageException("Uploaded file (%s) is not a valid image file. Detected format: %s. (Allowed formats: %s)".formatted(file.getOriginalFilename(), detectedMimeType, allowedImageFormats));
            }
        } catch (IOException exc) {
            throw new StorageException("Couldn't detect the media type of uploaded file: %s".formatted(file));
        }
    }

    public String getLogo() {
        //TODO: Make Logo customizable & save location to database
        return "images/svg/default-logo.svg";
    }
}
