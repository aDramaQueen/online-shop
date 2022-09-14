package com.acme.onlineshop.controller;

import com.acme.onlineshop.exception.StorageException;
import com.acme.onlineshop.exception.StorageFileNotFoundException;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.service.ImageService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.stream.Collectors;

/**
 * <p>Controller for uploading all kind of different files</p>
 *
 * @see <a href="https://spring.io/guides/gs/uploading-files/">Spring Guides - Uploading Files</a>
 */
@Controller
@RequestMapping(value = URL.Path.UPLOAD_FILE)
public class FileUploadController {

    private final UserService userService;
    private final ImageService imageService;

    @Autowired
    public FileUploadController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping("/image")
    public String listUploadedUserImages(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("images", imageService.loadAll(currentUser).map(path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,"serveUserFile", currentUser.getUsername(), path.getFileName().toString()).build().toUri().toString()).collect(Collectors.toList()));
        return URL.UPLOAD_FILE.html;
    }

    @GetMapping("/images/{imageName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String imageName) {
        Resource file = imageService.loadAsResource(imageName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/images/{userName}/{imageName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveUserFile(@PathVariable String userName, @PathVariable String imageName) {
        Resource file = imageService.loadAsResource(userName + "/" + imageName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/image")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            imageService.store(file, userService.getCurrentUser());
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        } catch (StorageException exc) {
            redirectAttributes.addFlashAttribute("message", "Error occurred during image uploading process: " + exc.getMessage());
        }
        return "redirect:%s/image".formatted(URL.Path.UPLOAD_FILE);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
