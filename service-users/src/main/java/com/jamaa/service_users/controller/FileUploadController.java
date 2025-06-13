package com.jamaa.service_users.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jamaa.service_users.service.StorageService;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    StorageService storageService;

    @PostMapping("/cni")
    public ResponseEntity<String> uploadCni(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = storageService.save(file); // Sauvegarde et retourne le chemin
            return ResponseEntity.ok(filePath);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de l’upload");
        }
    }
}
