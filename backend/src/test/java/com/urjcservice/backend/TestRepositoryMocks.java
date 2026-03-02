package com.urjcservice.backend;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.repositories.UserRepository;
import com.urjcservice.backend.service.FileStorageService;

@Configuration
@Profile("test")
public class TestRepositoryMocks {

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public RoomRepository roomRepository() {
        return Mockito.mock(RoomRepository.class);
    }

    @Bean
    public SoftwareRepository softwareRepository() {
        return Mockito.mock(SoftwareRepository.class);
    }

    @Bean
    public ReservationRepository reservationRepository() {
        return Mockito.mock(ReservationRepository.class);
    }

    // provide a mock mail sender so EmailService can be instantiated during tests
    @Bean
    public JavaMailSender mailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    // mock the file storage service to avoid filesystem operations during tests
    @Bean
    public FileStorageService fileStorageService() {
        return Mockito.mock(FileStorageService.class);
    }
}
