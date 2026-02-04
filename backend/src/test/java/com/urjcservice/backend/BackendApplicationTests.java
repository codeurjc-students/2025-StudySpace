package com.urjcservice.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.urjcservice.backend.service.EmailService;
import com.urjcservice.backend.service.FileStorageService;

@SpringBootTest
class BackendApplicationTests {

	@MockBean
    private EmailService emailService;

    @MockBean
    private FileStorageService fileStorageService;
	
	@Test
	void contextLoads() {
	}

}
