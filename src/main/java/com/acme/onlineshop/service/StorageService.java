package com.acme.onlineshop.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @see  <a href="https://spring.io/guides/gs/uploading-files/">Spring IO - Guide: Uploading Files</a>
 */
@Service
public class StorageService {

    public void init(){

    }

    public void store(MultipartFile file){

    }

    public Stream<Path> loadAll(){
        return null;
    }

    public Path load(String filename){
        return null;
    }

    public Resource loadAsResource(String filename) {
        return null;
    }

    public void deleteAll() {

    }
}
