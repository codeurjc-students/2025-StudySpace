package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.repositories.UserRepository;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

@Component
//@Profile("!e2e")
public class DatabaseInitializer implements CommandLineRunner {


    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SoftwareRepository softwareRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository,
                               RoomRepository roomRepository,
                               SoftwareRepository softwareRepository,
                               ReservationRepository reservationRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.softwareRepository = softwareRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        


        //SUPER_ADMIN
        //create the admin above everything
        String adminEmail = "admin@studyspace.com"; 
        //if it is already on the database, do nothing
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            
            User admin = new User();
            admin.setName("SuperAdmin");
            admin.setEmail(adminEmail);
            admin.setEncodedPassword(passwordEncoder.encode("Admin12.")); 
            
            // we give him the ADMIN role and USER role
            admin.setRoles(Arrays.asList("USER", "ADMIN"));
            admin.setType(User.UserType.ADMIN);

            userRepository.save(admin);
            
            logger.info("ADMIN: " + adminEmail);
        }






        if (roomRepository.count() == 0) {
            logger.info("Inserting initial test data into the database...");

            // --- SOFTWARE ---
            Software eclipse = new Software();
            eclipse.setName("Eclipse IDE");
            eclipse.setVersion(2023.03f);
            eclipse.setDescription("Entorno de desarrollo Java.");

            Software matlab = new Software();
            matlab.setName("Matlab");
            matlab.setVersion(9.5f);
            matlab.setDescription("Plataforma de cálculo numérico.");

            Software autocad = new Software();
            autocad.setName("AutoCAD");
            autocad.setVersion(2024.0f);
            autocad.setDescription("Diseño asistido por computadora.");

            Software office = new Software();
            office.setName("Microsoft Office");
            office.setVersion(365.0f);
            office.setDescription("Paquete de ofimática.");




            softwareRepository.saveAll(Arrays.asList(eclipse, matlab, autocad, office));





            // --- ROOMS  ---
            Room lab1 = new Room();
            lab1.setName("Laboratorio A1");
            lab1.setCapacity(30);
            lab1.setCamp(Room.CampusType.MOSTOLES);
            lab1.setPlace("Aulario II");
            lab1.setCoordenades("40.332, -3.885");
            lab1.setSoftware(Arrays.asList(eclipse, matlab)); 

            Room aulaDiseno = new Room();
            aulaDiseno.setName("Sala de Diseño");
            aulaDiseno.setCapacity(15);
            aulaDiseno.setCamp(Room.CampusType.ALCORCON);
            aulaDiseno.setPlace("Edificio Principal");
            aulaDiseno.setCoordenades("40.345, -3.820");
            aulaDiseno.setSoftware(Arrays.asList(autocad)); 

            Room biblioteca = new Room();
            biblioteca.setName("Sala de Estudio 3");
            biblioteca.setCapacity(50);
            biblioteca.setCamp(Room.CampusType.VICALVARO);
            biblioteca.setPlace("Biblioteca Central");
            biblioteca.setCoordenades("40.405, -3.608");
            biblioteca.setSoftware(Arrays.asList(office)); 


            roomRepository.saveAll(Arrays.asList(lab1, aulaDiseno, biblioteca));




            // --- USERS  ---
            User student1 = new User();
            student1.setName("Ana Estudiante");
            student1.setEmail("ana@alumnos.urjc.es");
            student1.setEncodedPassword(passwordEncoder.encode("1234aA.."));
            student1.setRoles(Arrays.asList("USER"));
            student1.setType(User.UserType.USER_REGISTERED);

            User student3 = new User();
            student3.setName("Francisco Blanco");
            student3.setEmail("fran@gmail.com");
            student3.setEncodedPassword(passwordEncoder.encode("1234aA.."));
            student3.setRoles(Arrays.asList("USER"));
            student3.setType(User.UserType.USER_REGISTERED);

            User student2 = new User();
            student2.setName("Carlos Profesor");
            student2.setEmail("carlos@urjc.es");
            student2.setEncodedPassword(passwordEncoder.encode("1234aA.."));
            student2.setRoles(Arrays.asList("USER")); 
            student2.setType(User.UserType.USER_REGISTERED);

            userRepository.saveAll(Arrays.asList(student1, student2, student3));

            // --- RESERVATIONS ---
            long now = System.currentTimeMillis();
            long oneHour = 3600000L;
            long oneDay = 86400000L;

            
            Reservation res1 = new Reservation();
            res1.setStartDate(new Date(now + oneDay));
            res1.setEndDate(new Date(now + oneDay + (2 * oneHour))); 
            res1.setReason("Práctica de Programación");
            res1.setUser(student1);
            res1.setRoom(lab1);

           
            Reservation res2 = new Reservation();
            res2.setStartDate(new Date(now + (2 * oneDay)));
            res2.setEndDate(new Date(now + (2 * oneDay) + (4 * oneHour))); 
            res2.setReason("Clase de Diseño Técnico");
            res2.setUser(student2);
            res2.setRoom(aulaDiseno);

            Reservation res3 = new Reservation();
            res3.setStartDate(new Date(now + (3 * oneDay)));
            res3.setEndDate(new Date(now + (3 * oneDay) + (5 * oneHour))); 
            res3.setReason("Clase de Programación Avanzada");
            res3.setUser(student2);
            res3.setRoom(aulaDiseno);


            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
            LocalDate nextTuesday = today.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.TUESDAY));
            //6 hours red
            Reservation resRed1 = new Reservation();
            resRed1.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(8, 0)));
            resRed1.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(14, 0)));
            resRed1.setReason("Hackathon Urjc (Parte 1)");
            resRed1.setUser(student1);
            resRed1.setRoom(lab1);

            //  6 hours red
            Reservation resRed2 = new Reservation();
            resRed2.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(15, 0)));
            resRed2.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(21, 0)));
            resRed2.setReason("Hackathon Urjc (Parte 2)");
            resRed2.setUser(student2);
            resRed2.setRoom(lab1);


            // Yellow 7 hours
            Reservation resYellow = new Reservation();
            resYellow.setStartDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(9, 0)));
            resYellow.setEndDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(16, 0)));
            resYellow.setReason("Curso Intensivo Java");
            resYellow.setUser(student3);
            resYellow.setRoom(lab1); 


            //green 2 hours
            LocalDate nextWednesday = today.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.WEDNESDAY));
            Reservation resGreen = new Reservation();
            resGreen.setStartDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(10, 0)));
            resGreen.setEndDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(12, 0)));
            resGreen.setReason("Tutoría");
            resGreen.setUser(student1);
            resGreen.setRoom(lab1);

            reservationRepository.saveAll(Arrays.asList(resRed1, resRed2, resYellow, resGreen));

            //reservationRepository.saveAll(Arrays.asList(res1, res2,res3));

            logger.info("--------------------------------------");
            logger.info(" Database initialized with test data:");
            logger.info(" Rooms created: 3");
            logger.info(" Software created: 4");
            logger.info(" Users created: 2");
            logger.info(" Reservations created: 2");
            logger.info("--------------------------------------");
        }
    }
}