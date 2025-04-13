package my.projects.classroomschedulerapp.config;

import my.projects.classroomschedulerapp.model.Building;
import my.projects.classroomschedulerapp.model.Course;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.model.Program;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.User;
import my.projects.classroomschedulerapp.repository.BuildingRepository;
import my.projects.classroomschedulerapp.repository.CourseRepository;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import my.projects.classroomschedulerapp.repository.ProgramRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
import my.projects.classroomschedulerapp.repository.ScheduleRepository;
import my.projects.classroomschedulerapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ScheduleRepository scheduleRepository, BuildingRepository buildingRepository,
                           RoomRepository roomRepository,
                           DepartmentRepository departmentRepository,
                           ProgramRepository programRepository,
                           CourseRepository courseRepository,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.scheduleRepository = scheduleRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
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

        // Create buildings if they don't exist
        Map<String, Building> buildings = new HashMap<>();
        if (buildingRepository.count() == 0) {
            Building stBuilding = new Building();
            stBuilding.setName("ST Building");
            buildingRepository.save(stBuilding);
            buildings.put("ST Building", stBuilding);

            Building libBuilding = new Building();
            libBuilding.setName("Library Building");
            buildingRepository.save(libBuilding);
            buildings.put("Library Building", libBuilding);

            Building adminBuilding = new Building();
            adminBuilding.setName("Admin Building");
            buildingRepository.save(adminBuilding);
            buildings.put("Admin Building", adminBuilding);
        } else {
            // Load existing buildings into map for reference
            buildingRepository.findAll().forEach(building ->
                    buildings.put(building.getName(), building));
        }

        // Add example rooms if not exists
        if (roomRepository.count() == 0) {
            Room room1 = new Room();
            room1.setRoomNumber("ST101");
            room1.setBuilding(buildings.get("ST Building"));
            room1.setCapacity(30);
            room1.setHasProjector(true);
            room1.setHasComputers(true);
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setRoomNumber("ST111A");
            room2.setBuilding(buildings.get("ST Building"));
            room2.setCapacity(40);
            room2.setHasProjector(true);
            room2.setHasComputers(false);
            roomRepository.save(room2);

            Room room3 = new Room();
            room3.setRoomNumber("ST208");
            room3.setBuilding(buildings.get("ST Building"));
            room3.setCapacity(40);
            room3.setHasProjector(true);
            room3.setHasComputers(false);
            roomRepository.save(room3);

            Room room4 = new Room();
            room4.setRoomNumber("ST411");
            room4.setBuilding(buildings.get("ST Building"));
            room4.setCapacity(40);
            room4.setHasProjector(true);
            room4.setHasComputers(false);
            roomRepository.save(room4);

            Room libRoom = new Room();
            libRoom.setRoomNumber("LIB101");
            libRoom.setBuilding(buildings.get("Library Building"));
            libRoom.setCapacity(20);
            libRoom.setHasProjector(true);
            libRoom.setHasComputers(true);
            roomRepository.save(libRoom);

            Room adminRoom = new Room();
            adminRoom.setRoomNumber("ADM201");
            adminRoom.setBuilding(buildings.get("Admin Building"));
            adminRoom.setCapacity(15);
            adminRoom.setHasProjector(false);
            adminRoom.setHasComputers(true);
            roomRepository.save(adminRoom);
        }

        // Create departments if they don't exist
        Map<String, Department> departments = new HashMap<>();
        if (departmentRepository.count() == 0) {
            Department engDepartment = new Department();
            engDepartment.setName("College of Engineering");
            departmentRepository.save(engDepartment);
            departments.put("College of Engineering", engDepartment);

            Department sciDepartment = new Department();
            sciDepartment.setName("College of Science");
            departmentRepository.save(sciDepartment);
            departments.put("College of Science", sciDepartment);

            Department humanitiesDepartment = new Department();
            humanitiesDepartment.setName("College of Humanities");
            departmentRepository.save(humanitiesDepartment);
            departments.put("College of Humanities", humanitiesDepartment);
        } else {
            departmentRepository.findAll().forEach(department ->
                    departments.put(department.getName(), department));
        }

        // Create programs if they don't exist
        Map<String, Program> programs = new HashMap<>();
        if (programRepository.count() == 0) {
            // Engineering programs
            Program civilEngProgram = new Program();
            civilEngProgram.setName("Bachelor of Science in Civil Engineering");
            civilEngProgram.setCode("BSCE");
            civilEngProgram.setDepartment(departments.get("College of Engineering"));
            programRepository.save(civilEngProgram);
            programs.put("BSCE", civilEngProgram);

            Program compEngProgram = new Program();
            compEngProgram.setName("Bachelor of Science in Computer Engineering");
            compEngProgram.setCode("BSCpE");
            compEngProgram.setDepartment(departments.get("College of Engineering"));
            programRepository.save(compEngProgram);
            programs.put("BSCpE", compEngProgram);

            Program geodeticProgram = new Program();
            geodeticProgram.setName("Bachelor of Science in Geodetic Engineering");
            geodeticProgram.setCode("BSGE");
            geodeticProgram.setDepartment(departments.get("College of Engineering"));
            programRepository.save(geodeticProgram);
            programs.put("BSGE", geodeticProgram);

            // Science programs
            Program mathProgram = new Program();
            mathProgram.setName("Bachelor of Science in Mathematics");
            mathProgram.setCode("BSM");
            mathProgram.setDepartment(departments.get("College of Science"));
            programRepository.save(mathProgram);
            programs.put("BSM", mathProgram);

            Program physicsProgram = new Program();
            physicsProgram.setName("Bachelor of Science in Physics");
            physicsProgram.setCode("BSP");
            physicsProgram.setDepartment(departments.get("College of Science"));
            programRepository.save(physicsProgram);
            programs.put("BSP", physicsProgram);

            // Humanities programs
            Program literatureProgram = new Program();
            literatureProgram.setName("Bachelor of Arts in Literature");
            literatureProgram.setCode("BAL");
            literatureProgram.setDepartment(departments.get("College of Humanities"));
            programRepository.save(literatureProgram);
            programs.put("BAL", literatureProgram);
        } else {
            programRepository.findAll().forEach(program ->
                    programs.put(program.getCode(), program));
        }

        // Create courses if they don't exist
        Map<String, Course> courses = new HashMap<>();
        if (courseRepository.count() == 0) {
            Course compEngCourse = new Course();
            compEngCourse.setCourseCode("CPE111");
            compEngCourse.setDescription("Computer Engineering as Discipline");
            compEngCourse.setProgram(programs.get("BSCpE"));
            courseRepository.save(compEngCourse);
            courses.put("Computer Engineering as Discipline", compEngCourse);

            Course itCourse = new Course();
            itCourse.setCourseCode("GEC9");
            itCourse.setDescription("Living in I.T. Era");
            itCourse.setProgram(programs.get("BSCpE"));
            courseRepository.save(itCourse);
            courses.put("Living in I.T. Era", itCourse);

            Course mathCourse = new Course();
            mathCourse.setCourseCode("CPE121");
            mathCourse.setDescription("Discrete Mathematics");
            mathCourse.setProgram(programs.get("BSM"));
            courseRepository.save(mathCourse);
            courses.put("Discrete Mathematics", mathCourse);

            Course envCourse = new Course();
            envCourse.setCourseCode("CPE417");
            envCourse.setDescription("Environmental Science and Engineering");
            envCourse.setProgram(programs.get("BSCE"));
            courseRepository.save(envCourse);
            courses.put("Environmental Science and Engineering", envCourse);

            Course numMethods = new Course();
            numMethods.setCourseCode("CPE223");
            numMethods.setDescription("Numerical Methods");
            numMethods.setProgram(programs.get("BSM"));
            courseRepository.save(numMethods);
            courses.put("Numerical Methods", numMethods);
        } else {
            courseRepository.findAll().forEach(course ->
                    courses.put(course.getDescription(), course));
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

            // Dummy schedules
            Schedule schedule1 = new Schedule();
            schedule1.setRoom(roomST101);
            schedule1.setUser(faculty);
            schedule1.setDate(today);
            schedule1.setStartTime(LocalTime.of(9, 0));
            schedule1.setEndTime(LocalTime.of(10, 30));
            schedule1.setCourse(courses.get("Computer Engineering as Discipline"));
            schedule1.setStatus(Schedule.Status.APPROVED);
            schedule1.setCreatedByEmail(faculty.getEmail());
            schedule1.setUpdatedByEmail(admin.getEmail());
            scheduleRepository.save(schedule1);

            Schedule schedule2 = new Schedule();
            schedule2.setRoom(roomST111A);
            schedule2.setUser(faculty);
            schedule2.setDate(today);
            schedule2.setStartTime(LocalTime.of(13, 0));
            schedule2.setEndTime(LocalTime.of(14, 30));
            schedule2.setCourse(courses.get("Living in I.T. Era"));
            schedule2.setStatus(Schedule.Status.APPROVED);
            schedule2.setCreatedByEmail(faculty.getEmail());
            schedule2.setUpdatedByEmail(admin.getEmail());
            scheduleRepository.save(schedule2);

            Schedule schedule3 = new Schedule();
            schedule3.setRoom(roomST411);
            schedule3.setUser(admin);
            schedule3.setDate(today.plusDays(1));
            schedule3.setStartTime(LocalTime.of(10, 0));
            schedule3.setEndTime(LocalTime.of(12, 0));
            schedule3.setCourse(courses.get("Discrete Mathematics"));
            schedule3.setStatus(Schedule.Status.APPROVED);
            schedule3.setCreatedByEmail(faculty.getEmail());
            schedule3.setUpdatedByEmail(admin.getEmail());
            scheduleRepository.save(schedule3);

            Schedule schedule4 = new Schedule();
            schedule4.setRoom(roomST208);
            schedule4.setUser(faculty);
            schedule4.setDate(today.plusDays(2));
            schedule4.setStartTime(LocalTime.of(14, 0));
            schedule4.setEndTime(LocalTime.of(15, 30));
            schedule4.setCourse(courses.get("Environmental Science and Engineering"));
            schedule4.setStatus(Schedule.Status.PENDING);
            schedule4.setCreatedByEmail(faculty.getEmail());
            schedule4.setUpdatedByEmail(admin.getEmail());
            scheduleRepository.save(schedule4);

            Schedule schedule5 = new Schedule();
            schedule5.setRoom(roomST101);
            schedule5.setUser(faculty);
            schedule5.setDate(today.plusDays(7));
            schedule5.setStartTime(LocalTime.of(10, 0));
            schedule5.setEndTime(LocalTime.of(11, 30));
            schedule5.setCourse(courses.get("Numerical Methods"));
            schedule5.setStatus(Schedule.Status.APPROVED);
            schedule5.setCreatedByEmail(faculty.getEmail());
            schedule5.setUpdatedByEmail(admin.getEmail());
            scheduleRepository.save(schedule5);
        }
    }
}