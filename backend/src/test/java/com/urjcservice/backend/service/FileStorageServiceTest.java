package com.urjcservice.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    
    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();

        
        ReflectionTestUtils.setField(fileStorageService, "storageLocation", tempDir.toString());
        
        fileStorageService.init();
    }

    // --- TEST STORE (Save) ---

    @Test
    @DisplayName("Store should save the file in the directory")
    void testStoreFile_Success() throws IOException {
        // GIVEN
        String filename = "test-image.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                filename, 
                "image/jpeg", 
                "fake-image-content".getBytes()
        );

        // WHEN
        String storedFilename = fileStorageService.store(file);

        // THEN
        assertNotNull(storedFilename);
        
        //file exists
        Path expectedPath = tempDir.resolve(storedFilename);
        assertTrue(Files.exists(expectedPath), "El fichero debería existir en el disco");
        
        assertArrayEquals("fake-image-content".getBytes(), Files.readAllBytes(expectedPath));
    }

    @Test
    @DisplayName("Store should throw exception if file is empty")
    void testStoreFile_Empty_ShouldFail() {
        // GIVEN
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(emptyFile);
        });
    }

    // --- TEST LOAD ---

    @Test
    @DisplayName("LoadAsResource should return a readable resource")
    void testLoadAsResource_Success() throws IOException {
        // GIVEN
        String filename = "manual-file.txt";
        Path filePath = tempDir.resolve(filename);
        Files.writeString(filePath, "Contenido de prueba");

        // WHEN
        Resource resource = fileStorageService.loadAsResource(filename);

        // THEN
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    @DisplayName("LoadAsResource should throw exception or fail if file not found")
    void testLoadAsResource_NotFound() {
        // WHEN & THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadAsResource("no-existe.txt");
        });
    }

    // --- TEST DELETE ---

    @Test
    @DisplayName("Delete should remove the file")
    void testDelete_Success() throws IOException {
        // GIVEN
        String filename = "borrame.txt";
        Path filePath = tempDir.resolve(filename);
        Files.createFile(filePath);
        assertTrue(Files.exists(filePath)); 

        // WHEN
        fileStorageService.delete(filename);

        // THEN
        assertFalse(Files.exists(filePath), "El fichero debería haber sido eliminado");
    }
    
    // --- TEST INIT ---
    
    @Test
    @DisplayName("Init should create directory if not exists")
    void testInit() {

        fileStorageService.init();
        assertTrue(Files.exists(tempDir));
    }
    @Test
    @DisplayName("Store should throw RuntimeException when IOException occurs")
    void testStore_Failure_IOException() throws IOException {
        // GIVEN
        MultipartFile mockFile = mock(MultipartFile.class);
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("fail.txt");
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated Disk Error"));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(mockFile);
        });
    }

    @Test
    @DisplayName("Delete should catch IOException (e.g., trying to delete non-empty dir)")
    void testDelete_Failure_IOException() throws IOException {
        // GIVEN
        String dirName = "folder-with-data";
        Path dirPath = tempDir.resolve(dirName);
        Files.createDirectory(dirPath);
        Files.createFile(dirPath.resolve("child.txt"));

        // WHEN & THEN
        assertDoesNotThrow(() -> fileStorageService.delete(dirName));

        assertTrue(Files.exists(dirPath));
    }

    @Test
    @DisplayName("Init should throw RuntimeException if directory creation fails")
    void testInit_Failure() throws IOException {
        // GIVEN
        String clashName = "im-a-file-not-a-dir";
        Path existingFile = tempDir.resolve(clashName);
        Files.createFile(existingFile);

        FileStorageService serviceWithError = new FileStorageService();
        
        ReflectionTestUtils.setField(serviceWithError, "storageLocation", existingFile.toString());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            serviceWithError.init();
        });
        
        assertTrue(ex.getMessage().contains("The storage folder could not be initialized"));
    }
}