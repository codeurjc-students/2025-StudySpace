package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.ReservationRepository;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.SoftwareRepository;
import com.urjcservice.backend.repositories.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "app.db.init.enabled", havingValue = "true", matchIfMissing = true)
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

                // SUPER_ADMIN
                // create the admin above everything
                String adminEmail = "studyspacetfg@gmail.com";
                // if it is already on the database, do nothing
                if (userRepository.findByEmail(adminEmail).isEmpty()) {

                        User admin = new User();
                        admin.setName("SuperAdmin");
                        admin.setEmail(adminEmail);
                        admin.setEncodedPassword(passwordEncoder.encode("Admin12."));

                        // we give him the ADMIN role and USER role
                        admin.setRoles(Arrays.asList("USER", "ADMIN"));
                        admin.setType(User.UserType.ADMIN);
                        admin.setEmailVerified(true);

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

                        Software vscode = new Software();
                        vscode.setName("Visual Studio Code");
                        vscode.setVersion(1.85f);
                        vscode.setDescription("Editor de código ligero.");

                        Software intellij = new Software();
                        intellij.setName("IntelliJ IDEA");
                        intellij.setVersion(2023.2f);
                        intellij.setDescription("IDE profesional para Java.");

                        Software spss = new Software();
                        spss.setName("SPSS Statistics");
                        spss.setVersion(29.0f);
                        spss.setDescription("Software para análisis estadístico.");

                        Software solidworks = new Software();
                        solidworks.setName("SolidWorks");
                        solidworks.setVersion(2023.0f);
                        solidworks.setDescription("Software de diseño CAD 3D.");

                        Software python = new Software();
                        python.setName("Python");
                        python.setVersion(3.11f);
                        python.setDescription("Intérprete y librerías base de Python.");

                        Software rstudio = new Software();
                        rstudio.setName("RStudio");
                        rstudio.setVersion(2023.06f);
                        rstudio.setDescription("Entorno para análisis de datos con R.");

                        Software docker = new Software();
                        docker.setName("Docker");
                        docker.setVersion(24.0f);
                        docker.setDescription("Plataforma de contenedores.");

                        Software git = new Software();
                        git.setName("Git");
                        git.setVersion(2.41f);
                        git.setDescription("Control de versiones.");

                        Software postman = new Software();
                        postman.setName("Postman");
                        postman.setVersion(10.15f);
                        postman.setDescription("Herramienta para pruebas de API REST.");

                        softwareRepository.saveAll(Arrays.asList(
                                        eclipse, matlab, autocad, office, vscode,
                                        intellij, spss, solidworks, python, rstudio, docker, git, postman));

                        // --- ROOMS ---
                        Room lab1 = new Room();
                        lab1.setName("Laboratorio A1");
                        lab1.setCapacity(30);
                        lab1.setCamp(Room.CampusType.MOSTOLES);
                        lab1.setPlace("Aulario II");
                        lab1.setCoordenades("40.332, -3.885");
                        lab1.setSoftware(Arrays.asList(eclipse, matlab));
                        lab1.setActive(true);

                        lab1.setImageName("lab1.jpg"); // room with image

                        Room aulaDiseno = new Room();
                        aulaDiseno.setName("Sala de Diseño");
                        aulaDiseno.setCapacity(15);
                        aulaDiseno.setCamp(Room.CampusType.ALCORCON);
                        aulaDiseno.setPlace("Edificio Principal");
                        aulaDiseno.setCoordenades("40.345, -3.820");
                        aulaDiseno.setSoftware(Arrays.asList(autocad));
                        aulaDiseno.setActive(true);

                        Room biblioteca = new Room();
                        biblioteca.setName("Sala de Estudio 3");
                        biblioteca.setCapacity(50);
                        biblioteca.setCamp(Room.CampusType.VICALVARO);
                        biblioteca.setPlace("Biblioteca Central");
                        biblioteca.setCoordenades("40.405, -3.608");
                        biblioteca.setSoftware(Arrays.asList(office));
                        biblioteca.setActive(true);

                        Room aulaMagna = new Room();
                        aulaMagna.setName("Aula Magna");
                        aulaMagna.setCapacity(150);
                        aulaMagna.setCamp(Room.CampusType.FUENLABRADA);
                        aulaMagna.setPlace("Edificio Principal Planta Baja");
                        aulaMagna.setCoordenades("40.283, -3.821");
                        aulaMagna.setSoftware(Arrays.asList()); // no software
                        aulaMagna.setActive(true);

                        Room salaReuniones = new Room();
                        salaReuniones.setName("Sala de Reuniones 1");
                        salaReuniones.setCapacity(8);
                        salaReuniones.setCamp(Room.CampusType.QUINTANA);
                        salaReuniones.setPlace("Planta 3, Puerta B");
                        salaReuniones.setCoordenades("40.428, -3.716");
                        salaReuniones.setSoftware(Arrays.asList(office));
                        salaReuniones.setActive(true);

                        Room labBio = new Room();
                        labBio.setName("Laboratorio Bioquímica");
                        labBio.setCapacity(25);
                        labBio.setCamp(Room.CampusType.ALCORCON);
                        labBio.setPlace("Edificio Lab III");
                        labBio.setCoordenades("40.344, -3.822");
                        labBio.setSoftware(Arrays.asList(matlab));
                        labBio.setActive(true);

                        Room tallerArqui = new Room();
                        tallerArqui.setName("Taller de Arquitectura");
                        tallerArqui.setCapacity(60);
                        tallerArqui.setCamp(Room.CampusType.FUENLABRADA);
                        tallerArqui.setPlace("Pabellón A");
                        tallerArqui.setCoordenades("40.285, -3.823");
                        tallerArqui.setSoftware(Arrays.asList(autocad, office));
                        tallerArqui.setActive(true);

                        Room zonaSilencio = new Room();
                        zonaSilencio.setName("Zona de Silencio Absoluto");
                        zonaSilencio.setCapacity(100);
                        zonaSilencio.setCamp(Room.CampusType.VICALVARO);
                        zonaSilencio.setPlace("Biblioteca Planta 2");
                        zonaSilencio.setCoordenades("40.405, -3.608");
                        zonaSilencio.setSoftware(Arrays.asList()); // no software
                        zonaSilencio.setActive(true);

                        Room estudioGrupal = new Room();
                        estudioGrupal.setName("Cabina Estudio Grupal");
                        estudioGrupal.setCapacity(6);
                        estudioGrupal.setCamp(Room.CampusType.MOSTOLES);
                        estudioGrupal.setPlace("Biblioteca Central");
                        estudioGrupal.setCoordenades("40.334, -3.881");
                        estudioGrupal.setSoftware(Arrays.asList(office));
                        estudioGrupal.setActive(true);

                        Room aula204 = new Room();
                        aula204.setName("Aula 204");
                        aula204.setCapacity(35);
                        aula204.setCamp(Room.CampusType.QUINTANA);
                        aula204.setPlace("Planta 2");
                        aula204.setCoordenades("40.428, -3.716");
                        aula204.setSoftware(Arrays.asList());
                        aula204.setActive(true);

                        // inactive rooms (maintenance, closed, etc)
                        Room aulaMantenimiento = new Room();
                        aulaMantenimiento.setName("Aula en Reparación");
                        aulaMantenimiento.setCapacity(20);
                        aulaMantenimiento.setCamp(Room.CampusType.QUINTANA);
                        aulaMantenimiento.setPlace("Planta Baja");
                        aulaMantenimiento.setCoordenades("40.428, -3.716");
                        aulaMantenimiento.setSoftware(Arrays.asList());
                        aulaMantenimiento.setActive(false); // under maintenance, not available for reservations

                        Room labCerrado = new Room();
                        labCerrado.setName("Lab Informática (Cerrado)");
                        labCerrado.setCapacity(30);
                        labCerrado.setCamp(Room.CampusType.MOSTOLES);
                        labCerrado.setPlace("Aulario I");
                        labCerrado.setCoordenades("40.330, -3.880");
                        labCerrado.setSoftware(Arrays.asList(eclipse));
                        labCerrado.setActive(false); // under maintenance, not available for reservations

                        Room aulaMultimedia = new Room();
                        aulaMultimedia.setName("Sala Multimedia");// room with image
                        aulaMultimedia.setCapacity(20);
                        aulaMultimedia.setCamp(Room.CampusType.FUENLABRADA);
                        aulaMultimedia.setPlace("Edificio de Comunicación");
                        aulaMultimedia.setCoordenades("40.283, -3.821");
                        aulaMultimedia.setSoftware(Arrays.asList(docker, git));
                        aulaMultimedia.setActive(true);

                        // image
                        aulaMultimedia.setImageName("sala_multimedia.jpg");

                        roomRepository.saveAll(Arrays.asList(
                                        lab1, aulaDiseno, biblioteca,
                                        aulaMagna, salaReuniones, labBio,
                                        tallerArqui, zonaSilencio, estudioGrupal, aula204,
                                        aulaMantenimiento, labCerrado, aulaMultimedia));

                        // --- USERS ---
                        User student1 = new User();
                        student1.setName("Ana Estudiante");
                        student1.setEmail("ana@alumnos.urjc.es");
                        student1.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student1.setRoles(Arrays.asList("USER"));
                        student1.setType(User.UserType.USER_REGISTERED);
                        student1.setEmailVerified(true);

                        User student2 = new User();
                        student2.setName("Carlos Profesor");
                        student2.setEmail("carlos@urjc.es");
                        student2.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student2.setRoles(Arrays.asList("USER"));
                        student2.setType(User.UserType.USER_REGISTERED);
                        student2.setEmailVerified(true);

                        // ADMIN user
                        User student3 = new User();
                        student3.setName("Francisco Blanco");
                        student3.setEmail("fran@gmail.com");
                        student3.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student3.setRoles(Arrays.asList("USER", "ADMIN"));
                        student3.setType(User.UserType.USER_REGISTERED);
                        student3.setEmailVerified(true);

                        student3.setImageName("fran_perfil.jpg");

                        // ADMIN user
                        User miAdmin = new User();
                        miAdmin.setName("Guillermo");
                        miAdmin.setEmail("guilleraes@gmail.com");
                        miAdmin.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        miAdmin.setRoles(Arrays.asList("USER", "ADMIN"));
                        miAdmin.setType(User.UserType.ADMIN);
                        miAdmin.setEmailVerified(true);

                        miAdmin.setImageName("guillermo_perfil.jpg");

                        // blocked user
                        User blockedUser = new User();
                        blockedUser.setName("Usuario Bloqueado");
                        blockedUser.setEmail("troll@gmail.com");
                        blockedUser.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        blockedUser.setRoles(Arrays.asList("USER"));
                        blockedUser.setType(User.UserType.USER_REGISTERED);
                        blockedUser.setBlocked(true);
                        blockedUser.setEmailVerified(true);

                        User student4 = new User();
                        student4.setName("Lucía Gómez");
                        student4.setEmail("lucia.gomez@alumnos.urjc.es");
                        student4.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student4.setRoles(Arrays.asList("USER"));
                        student4.setEmailVerified(true);
                        student4.setType(User.UserType.USER_REGISTERED);

                        User student5 = new User();
                        student5.setName("Miguel López");
                        student5.setEmail("m.lopez@urjc.es");
                        student5.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student5.setRoles(Arrays.asList("USER"));
                        student5.setEmailVerified(true);
                        student5.setType(User.UserType.USER_REGISTERED);

                        User student6 = new User();
                        student6.setName("Elena Rodríguez");
                        student6.setEmail("elena.r@gmail.com");
                        student6.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student6.setRoles(Arrays.asList("USER"));
                        student6.setEmailVerified(true);
                        student6.setType(User.UserType.USER_REGISTERED);

                        User student7 = new User();
                        student7.setName("David Sánchez");
                        student7.setEmail("david.s@alumnos.urjc.es");
                        student7.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        student7.setRoles(Arrays.asList("USER"));
                        student7.setEmailVerified(true);
                        student7.setType(User.UserType.USER_REGISTERED);

                        User userImg = new User();
                        userImg.setName("Laura Fotografía");// image user
                        userImg.setEmail("laura@urjc.es");
                        userImg.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        userImg.setRoles(Arrays.asList("USER"));
                        userImg.setEmailVerified(true);
                        userImg.setType(User.UserType.USER_REGISTERED);

                        // image
                        userImg.setImageName("laura_perfil.jpg");

                        User userExtra1 = new User();
                        userExtra1.setName("Pedro Novedad");
                        userExtra1.setEmail("pedro@gmail.com");
                        userExtra1.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        userExtra1.setRoles(Arrays.asList("USER"));
                        userExtra1.setEmailVerified(true);
                        userExtra1.setType(User.UserType.USER_REGISTERED);

                        User userExtra2 = new User();
                        userExtra2.setName("Sofía Apuntes");
                        userExtra2.setEmail("sofia@alumnos.urjc.es");
                        userExtra2.setEncodedPassword(passwordEncoder.encode("1234aA.."));
                        userExtra2.setRoles(Arrays.asList("USER"));
                        userExtra2.setEmailVerified(true);
                        userExtra2.setType(User.UserType.USER_REGISTERED);

                        userRepository.saveAll(Arrays.asList(
                                        student1, student2, student3,
                                        miAdmin, blockedUser,
                                        student4, student5, student6, student7, userImg, userExtra1, userExtra2));

                        // ==========================================
                        // 4. RESERVATIONS (Aprox 20 items)
                        // ==========================================
                        LocalDate today = LocalDate.now();
                        LocalDate nextMonday = today
                                        .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
                        LocalDate nextTuesday = today
                                        .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.TUESDAY));
                        LocalDate nextWednesday = today
                                        .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.WEDNESDAY));
                        LocalDate nextThursday = today
                                        .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.THURSDAY));
                        LocalDate nextFriday = today
                                        .with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.FRIDAY));

                        // --- Laboratorio A1 with calendar colors---
                        // red monaday 12 hours occupied
                        Reservation resRed1 = new Reservation();
                        resRed1.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(8, 0)));
                        resRed1.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(11, 0)));
                        resRed1.setReason("Hackathon Urjc (Parte 1)");
                        resRed1.setUser(student1);
                        resRed1.setRoom(lab1);
                        resRed1.setVerified(true);

                        Reservation resRed3 = new Reservation();
                        resRed3.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(12, 0)));
                        resRed3.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(15, 0)));
                        resRed3.setReason("Hackathon Urjc (Parte 2)");
                        resRed3.setUser(userExtra1);
                        resRed3.setRoom(lab1);
                        resRed3.setVerified(true);

                        Reservation resRed4 = new Reservation();
                        resRed4.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(15, 0)));
                        resRed4.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(18, 0)));
                        resRed4.setReason("Hackathon Urjc (Parte 3)");
                        resRed4.setUser(student6);
                        resRed4.setRoom(lab1);
                        resRed4.setVerified(true);

                        Reservation resRed2 = new Reservation();
                        resRed2.setStartDate(java.sql.Timestamp.valueOf(nextMonday.atTime(18, 0)));
                        resRed2.setEndDate(java.sql.Timestamp.valueOf(nextMonday.atTime(21, 0)));
                        resRed2.setReason("Hackathon Urjc (Parte 4)");
                        resRed2.setUser(student7);
                        resRed2.setRoom(lab1);
                        resRed2.setVerified(true);

                        // Yellow tuesday 7 hours occupied
                        Reservation resYellow1 = new Reservation();
                        resYellow1.setStartDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(9, 0)));
                        resYellow1.setEndDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(12, 0)));
                        resYellow1.setReason("Curso Intensivo Java");
                        resYellow1.setUser(userExtra1);
                        resYellow1.setRoom(lab1);
                        resYellow1.setVerified(true);

                        Reservation resYellow2 = new Reservation();
                        resYellow2.setStartDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(12, 0)));
                        resYellow2.setEndDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(15, 0)));
                        resYellow2.setReason("Curso Intensivo Java");
                        resYellow2.setUser(userExtra2);
                        resYellow2.setRoom(lab1);
                        resYellow2.setVerified(true);

                        Reservation resYellow3 = new Reservation();
                        resYellow3.setStartDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(15, 0)));
                        resYellow3.setEndDate(java.sql.Timestamp.valueOf(nextTuesday.atTime(16, 0)));
                        resYellow3.setReason("Curso Intensivo Java");
                        resYellow3.setUser(student7);
                        resYellow3.setRoom(lab1);
                        resYellow3.setVerified(true);

                        // green wenesday 2 hous occupied
                        Reservation resGreen = new Reservation();
                        resGreen.setStartDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(10, 0)));
                        resGreen.setEndDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(12, 0)));
                        resGreen.setReason("Tutoría");
                        resGreen.setUser(student1);
                        resGreen.setRoom(lab1);
                        resGreen.setVerified(true);

                        // Active and Verified
                        Reservation adminAct1 = new Reservation();
                        adminAct1.setStartDate(java.sql.Timestamp.valueOf(nextThursday.atTime(10, 0)));
                        adminAct1.setEndDate(java.sql.Timestamp.valueOf(nextThursday.atTime(13, 0)));
                        adminAct1.setReason("Reunión de Departamento");
                        adminAct1.setUser(student3);
                        adminAct1.setRoom(salaReuniones);
                        adminAct1.setVerified(true);

                        Reservation adminAct2 = new Reservation();
                        adminAct2.setStartDate(java.sql.Timestamp.valueOf(nextFriday.atTime(9, 0)));
                        adminAct2.setEndDate(java.sql.Timestamp.valueOf(nextFriday.atTime(11, 0)));
                        adminAct2.setReason("Supervisión Aula Magna");
                        adminAct2.setUser(student3);
                        adminAct2.setRoom(aulaMagna);
                        adminAct2.setVerified(true);

                        Reservation adminAct3 = new Reservation();
                        adminAct3.setStartDate(java.sql.Timestamp.valueOf(nextFriday.atTime(16, 0)));
                        adminAct3.setEndDate(java.sql.Timestamp.valueOf(nextFriday.atTime(18, 0)));
                        adminAct3.setReason("Mantenimiento Servidores");
                        adminAct3.setUser(student3);
                        adminAct3.setRoom(estudioGrupal);
                        adminAct3.setVerified(true);

                        Reservation adminAct4 = new Reservation();
                        adminAct4.setStartDate(java.sql.Timestamp.valueOf(nextMonday.plusDays(7).atTime(10, 0)));
                        adminAct4.setEndDate(java.sql.Timestamp.valueOf(nextMonday.plusDays(7).atTime(12, 0)));
                        adminAct4.setReason("Pruebas de Software");
                        adminAct4.setUser(student3);
                        adminAct4.setRoom(labBio);
                        adminAct4.setVerified(true);

                        // Completed
                        Reservation adminPast1 = new Reservation();
                        adminPast1.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(1).atTime(9, 0)));
                        adminPast1.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(1).atTime(11, 0)));
                        adminPast1.setReason("Reunión Rectorado");
                        adminPast1.setUser(student3);
                        adminPast1.setRoom(salaReuniones);
                        adminPast1.setVerified(true);

                        Reservation adminPast2 = new Reservation();
                        adminPast2.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(2).atTime(10, 0)));
                        adminPast2.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(2).atTime(12, 0)));
                        adminPast2.setReason("Revisión Inventario");
                        adminPast2.setUser(student3);
                        adminPast2.setRoom(tallerArqui);
                        adminPast2.setVerified(true);

                        Reservation adminPast3 = new Reservation();
                        adminPast3.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(3).atTime(11, 0)));
                        adminPast3.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(3).atTime(13, 0)));
                        adminPast3.setReason("Control de Acceso");
                        adminPast3.setUser(student3);
                        adminPast3.setRoom(zonaSilencio);
                        adminPast3.setVerified(true);

                        Reservation adminPast4 = new Reservation();
                        adminPast4.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(4).atTime(12, 0)));
                        adminPast4.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(4).atTime(14, 0)));
                        adminPast4.setReason("Configuración Redes");
                        adminPast4.setUser(student3);
                        adminPast4.setRoom(aula204);
                        adminPast4.setVerified(true);

                        Reservation adminPast5 = new Reservation();
                        adminPast5.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(5).atTime(16, 0)));
                        adminPast5.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(5).atTime(18, 0)));
                        adminPast5.setReason("Reunión Seguridad");
                        adminPast5.setUser(student3);
                        adminPast5.setRoom(salaReuniones);
                        adminPast5.setVerified(true);

                        // Canceled
                        Reservation adminCanc1 = new Reservation();
                        adminCanc1.setStartDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(15, 0)));
                        adminCanc1.setEndDate(java.sql.Timestamp.valueOf(nextWednesday.atTime(17, 0)));
                        adminCanc1.setReason("Auditoría (Suspendida)");
                        adminCanc1.setUser(student3);
                        adminCanc1.setRoom(zonaSilencio);
                        adminCanc1.setCancelled(true);
                        adminCanc1.setVerified(true);

                        Reservation adminCanc2 = new Reservation();
                        adminCanc2.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(6).atTime(9, 0)));
                        adminCanc2.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(6).atTime(11, 0)));
                        adminCanc2.setReason("Instalación Proyector");
                        adminCanc2.setUser(student3);
                        adminCanc2.setRoom(aula204);
                        adminCanc2.setCancelled(true);
                        adminCanc2.setVerified(true);

                        Reservation adminCanc3 = new Reservation();
                        adminCanc3.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(7).atTime(14, 0)));
                        adminCanc3.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(7).atTime(16, 0)));
                        adminCanc3.setReason("Mantenimiento Extra");
                        adminCanc3.setUser(student3);
                        adminCanc3.setRoom(lab1);
                        adminCanc3.setCancelled(true);
                        adminCanc3.setVerified(true);

                        // --- User Blocked ---
                        Reservation blockedRes = new Reservation();
                        blockedRes.setStartDate(java.sql.Timestamp.valueOf(today.minusDays(10).atTime(10, 0)));
                        blockedRes.setEndDate(java.sql.Timestamp.valueOf(today.minusDays(10).atTime(12, 0)));
                        blockedRes.setReason("Uso indebido del material");
                        blockedRes.setUser(blockedUser);
                        blockedRes.setRoom(aulaDiseno);
                        blockedRes.setVerified(true);

                        Reservation student4Res = new Reservation();
                        student4Res.setStartDate(java.sql.Timestamp.valueOf(nextThursday.atTime(18, 0)));
                        student4Res.setEndDate(java.sql.Timestamp.valueOf(nextThursday.atTime(20, 0)));
                        student4Res.setReason("Trabajo en Grupo");
                        student4Res.setUser(student4);
                        student4Res.setRoom(estudioGrupal);
                        student4Res.setVerified(true);

                        reservationRepository.saveAll(Arrays.asList(
                                        resRed1, resRed2, resRed3, resRed4, resYellow1, resYellow2, resYellow3,
                                        resGreen,
                                        adminAct1, adminAct2, adminAct3, adminAct4,
                                        adminPast1, adminPast2, adminPast3, adminPast4, adminPast5,
                                        adminCanc1, adminCanc2, adminCanc3,
                                        blockedRes, student4Res));

                        logger.info("--------------------------------------");
                        logger.info(" Database initialized with test data:");
                        logger.info(" Rooms created: 12");
                        logger.info(" Software created: 10");
                        logger.info(" Users created: 9");
                        logger.info(" Reservations created: 18");
                        logger.info("--------------------------------------");
                }
        }
}