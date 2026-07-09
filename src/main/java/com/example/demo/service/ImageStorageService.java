package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.image.upload-dir}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        // 1. Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
        }

        // 2. Generate unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 3. Save the file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        System.out.println("Image saved: " + filePath.toAbsolutePath());

        // 4. Return the relative path to store in DB
        return uploadDir + "/" + uniqueFilename;
    }

    public void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
            System.out.println("Image deleted: " + imagePath);
        } catch (IOException e) {
            System.out.println("Could not delete image: " + imagePath);
        }
    }
    // Save image from Base64 string — used when user confirms save
    public String saveImageFromBase64(String base64Data, String mediaType) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extension = mediaType != null && mediaType.contains("png") ? ".png" : ".jpg";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, imageBytes);

        System.out.println("Image saved on final save: " + filePath.toAbsolutePath());

        return uploadDir + "/" + uniqueFilename;
    }
}