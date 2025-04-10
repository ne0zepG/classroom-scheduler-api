package my.projects.classroomschedulerapp.config;

import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.model.User;
import my.projects.classroomschedulerapp.repository.ScheduleRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
import my.projects.classroomschedulerapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ScheduleRepository scheduleRepository,
                           RoomRepository roomRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@college.edu")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@college.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
        }

        // Create faculty user if not exists
        if (!userRepository.existsByEmail("faculty@college.edu")) {
            User faculty = new User();
            faculty.setName("Faculty Member");
            faculty.setEmail("faculty@college.edu");
            faculty.setPassword(passwordEncoder.encode("faculty123"));
            faculty.setRole(User.Role.FACULTY);
            userRepository.save(faculty);
        }

        // Add some example rooms if not exists
        if (roomRepository.count() == 0) {
            // Building A rooms
            Room room1 = new Room();
            room1.setRoomNumber("ST101");
            room1.setBuilding("ST Building");
            room1.setCapacity(30);
            room1.setHasProjector(true);
            room1.setHasComputers(true);
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setRoomNumber("ST111A");
            room2.setBuilding("ST Building");
            room2.setCapacity(40);
            room2.setHasProjector(true);
            room2.setHasComputers(false);
            roomRepository.save(room2);

            // Computer Lab
            Room room3 = new Room();
            room3.setRoomNumber("ST208");
            room3.setBuilding("ST Building");
            room3.setCapacity(40);
            room3.setHasProjector(true);
            room3.setHasComputers(false);
            roomRepository.save(room3);

            // Lecture Hall
            Room room4 = new Room();
            room4.setRoomNumber("ST411");
            room4.setBuilding("ST Building");
            room4.setCapacity(40);
            room4.setHasProjector(true);
            room4.setHasComputers(false);
            roomRepository.save(room4);
        }

        // Create sample schedules if none exist
        if (scheduleRepository.count() == 0) {
            // Get users
            User admin = userRepository.findByEmail("admin@college.edu").orElseThrow();
            User faculty = userRepository.findByEmail("faculty@college.edu").orElseThrow();

            // Get rooms
            Room roomST101 = roomRepository.findByRoomNumber("ST101");
            Room roomST111A = roomRepository.findByRoomNumber("ST111A");
            Room roomST208 = roomRepository.findByRoomNumber("ST208");
            Room roomST411 = roomRepository.findByRoomNumber("ST411");

            LocalDate today = LocalDate.now();

            // Morning class in A101
            Schedule schedule1 = new Schedule();
            schedule1.setRoom(roomST101);
            schedule1.setUser(faculty);
            schedule1.setDate(today);
            schedule1.setStartTime(LocalTime.of(9, 0));
            schedule1.setEndTime(LocalTime.of(10, 30));
            schedule1.setPurpose("Computer Engineering as Discipline");
            schedule1.setStatus(Schedule.Status.APPROVED);
            scheduleRepository.save(schedule1);

            // Computer lab session
            Schedule schedule2 = new Schedule();
            schedule2.setRoom(roomST111A);
            schedule2.setUser(faculty);
            schedule2.setDate(today);
            schedule2.setStartTime(LocalTime.of(13, 0));
            schedule2.setEndTime(LocalTime.of(14, 30));
            schedule2.setPurpose("Living in I.T. Era");
            schedule2.setStatus(Schedule.Status.APPROVED);
            scheduleRepository.save(schedule2);

            // Tomorrow's meeting
            Schedule schedule3 = new Schedule();
            schedule3.setRoom(roomST411);
            schedule3.setUser(admin);
            schedule3.setDate(today.plusDays(1));
            schedule3.setStartTime(LocalTime.of(10, 0));
            schedule3.setEndTime(LocalTime.of(12, 0));
            schedule3.setPurpose("Discrete Mathematics");
            schedule3.setStatus(Schedule.Status.APPROVED);
            scheduleRepository.save(schedule3);

            // Pending schedules
            Schedule schedule4 = new Schedule();
            schedule4.setRoom(roomST208);
            schedule4.setUser(faculty);
            schedule4.setDate(today.plusDays(2));
            schedule4.setStartTime(LocalTime.of(14, 0));
            schedule4.setEndTime(LocalTime.of(15, 30));
            schedule4.setPurpose("Environmental Science and Engineering");
            schedule4.setStatus(Schedule.Status.PENDING);
            scheduleRepository.save(schedule4);

            // Next week schedules
            Schedule schedule5 = new Schedule();
            schedule5.setRoom(roomST101);
            schedule5.setUser(faculty);
            schedule5.setDate(today.plusDays(7));
            schedule5.setStartTime(LocalTime.of(10, 0));
            schedule5.setEndTime(LocalTime.of(11, 30));
            schedule5.setPurpose("Numerical Methods");
            schedule5.setStatus(Schedule.Status.APPROVED);
            scheduleRepository.save(schedule5);
        }
    }
}