package com.urjcservice.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;

    @Value("${MINIO_BUCKET:studyspace-images}")
    private String bucketName;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /*
     * @PostConstruct
     * public void init() {
     * try {
     * this.rootLocation = Paths.get(storageLocation);
     * Files.createDirectories(this.rootLocation);
     * } catch (IOException e) {
     * throw new
     * IllegalStateException("The storage folder could not be initialized", e);
     * }
     * }
     */

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Error: empty file.");
            }
            // unique name to avoid conflicts
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return filename;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save file.", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            return new InputStreamResource(s3Client.getObject(getObjectRequest));
        } catch (Exception e) {
            throw new IllegalArgumentException("The file cannot be read: " + filename, e);
        }
    }

    public void delete(String filename) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            logger.error("Error deleting file: {}", filename, e);
        }
    }
}