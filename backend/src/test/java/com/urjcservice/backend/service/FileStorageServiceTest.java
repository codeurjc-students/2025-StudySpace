package com.urjcservice.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private S3Client s3ClientMock;
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        s3ClientMock = mock(S3Client.class);
        fileStorageService = new FileStorageService(s3ClientMock);

        // Inject the bucket name using reflection since it's populated by @Value
        ReflectionTestUtils.setField(fileStorageService, "bucketName", bucketName);
    }

    // --- TEST STORE (Save) ---

    @Test
    @DisplayName("Store should upload the file to S3/MinIO")
    void testStoreFile_Success() throws IOException {
        // GIVEN
        String filename = "test-image.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                "fake-image-content".getBytes());

        // WHEN
        String storedFilename = fileStorageService.store(file);

        // THEN
        assertNotNull(storedFilename);
        assertTrue(storedFilename.endsWith("_" + filename));

        // Verify that s3Client.putObject was called exactly once with any RequestBody
        verify(s3ClientMock, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Store should throw exception if file is empty")
    void testStoreFile_Empty_ShouldFail() {
        // GIVEN
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.store(emptyFile);
        });
        assertEquals("Error: empty file.", exception.getMessage());

        // Verify s3 was never called
        verify(s3ClientMock, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Store should throw IllegalStateException when IOException occurs")
    void testStore_Failure_IOException() throws IOException {
        // GIVEN
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("fail.txt");
        // Simulate an IO error when reading the file stream
        when(mockFile.getInputStream()).thenThrow(new IOException("Simulated Stream Error"));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> {
            fileStorageService.store(mockFile);
        });
    }

    // --- TEST LOAD ---

    @Test
    @DisplayName("LoadAsResource should return an InputStreamResource from S3")
    void testLoadAsResource_Success() throws IOException {
        // GIVEN
        String filename = "manual-file.txt";
        byte[] fakeData = "Contenido de prueba".getBytes();

        // Mock the response from S3
        ResponseInputStream<GetObjectResponse> mockS3Response = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream(fakeData));

        when(s3ClientMock.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Response);

        // WHEN
        Resource resource = fileStorageService.loadAsResource(filename);

        // THEN
        assertNotNull(resource);
        assertTrue(resource.exists());

        // Verify S3 client was called to get the object
        verify(s3ClientMock, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("LoadAsResource should throw exception if S3 throws an error")
    void testLoadAsResource_NotFound() {
        // GIVEN
        when(s3ClientMock.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("File not found").build());

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.loadAsResource("no-existe.txt");
        });
    }

    // --- TEST DELETE ---

    @Test
    @DisplayName("Delete should call S3 deleteObject")
    void testDelete_Success() {
        // GIVEN
        String filename = "borrame.txt";

        // WHEN
        fileStorageService.delete(filename);

        // THEN
        // Verify that s3Client.deleteObject was called exactly once
        verify(s3ClientMock, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Delete should not throw exception even if S3 fails (catches exception)")
    void testDelete_Failure_Handled() {
        // GIVEN
        String filename = "file-that-causes-error.txt";

        // Simulate S3 throwing an error when trying to delete
        doThrow(S3Exception.builder().message("Simulated S3 Error").build())
                .when(s3ClientMock).deleteObject(any(DeleteObjectRequest.class));

        // WHEN & THEN
        // Your code catches the exception and logs it, so it should NOT throw to the
        // caller
        assertDoesNotThrow(() -> fileStorageService.delete(filename));

        verify(s3ClientMock, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
}