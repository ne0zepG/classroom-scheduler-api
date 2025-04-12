package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.BaseScheduleDto;
import my.projects.classroomschedulerapp.dto.RecurrencePatternDto;
import my.projects.classroomschedulerapp.dto.RecurringScheduleRequestDto;
import my.projects.classroomschedulerapp.dto.ScheduleDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.exception.ScheduleConflictException;
import my.projects.classroomschedulerapp.model.Course;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.User;
import my.projects.classroomschedulerapp.repository.CourseRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
import my.projects.classroomschedulerapp.repository.ScheduleRepository;
import my.projects.classroomschedulerapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           RoomRepository roomRepository,
                           CourseRepository courseRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // Asynchronous method to get all schedules
    @Async("taskExecutor")
    public CompletableFuture<List<ScheduleDto>> getAllSchedulesAsync() {
        logger.debug("Asynchronously fetching all schedules");
        List<ScheduleDto> schedules = getAllSchedules();
        return CompletableFuture.completedFuture(schedules);
    }

    // Asynchronous method to get schedules by date
    @Async("taskExecutor")
    public CompletableFuture<List<ScheduleDto>> getSchedulesByDateAsync(LocalDate date) {
        logger.debug("Asynchronously fetching schedules for date: {}", date);
        List<ScheduleDto> schedules = getSchedulesByDate(date);
        return CompletableFuture.completedFuture(schedules);
    }

    // Asynchronous method to get schedules by user ID
    @Async("taskExecutor")
    public CompletableFuture<List<ScheduleDto>> createRecurringScheduleAsync(RecurringScheduleRequestDto requestDto) {
        List<ScheduleDto> schedules = createRecurringSchedule(requestDto);
        return CompletableFuture.completedFuture(schedules);
    }

    @Async("taskExecutor")
    public CompletableFuture<ScheduleDto> getScheduleByIdAsync(Long id) {
        logger.debug("Asynchronously fetching schedule with id: {}", id);
        ScheduleDto schedule = getScheduleById(id);
        return CompletableFuture.completedFuture(schedule);
    }

    // Get all schedules
    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        logger.debug("Fetching all schedules");
        List<ScheduleDto> schedules = scheduleRepository.findAll()
                .parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} schedules", schedules.size());
        return schedules;
    }

    // Get schedule by ID
    @Transactional
    @Cacheable(value = "scheduleDetails", key = "#id")
    public ScheduleDto getScheduleById(Long id) {
        logger.debug("Fetching schedule with id: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        logger.debug("Found schedule: {}", schedule.getId());
        return convertToDto(schedule);
    }

    // Create a new schedule
    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        logger.debug("Creating new schedule for room: {}, date: {}, time: {}-{}",
                scheduleDto.getRoomId(), scheduleDto.getDate(),
                scheduleDto.getStartTime(), scheduleDto.getEndTime());

        // Find and validate entities
        EntityResults entities = findAndValidateEntities(scheduleDto);
        logger.debug("Validated entities - room: {}, course: {}, user: {}",
                entities.getRoom().getRoomNumber(),
                entities.getCourse().getCourseCode(),
                entities.getUser().getEmail());


        // Check for schedule conflicts
        checkForScheduleConflicts(entities.getRoom(), scheduleDto.getDate(),
                scheduleDto.getStartTime(), scheduleDto.getEndTime(), null);
        logger.debug("No schedule conflicts found");

        // Create schedule
        Schedule schedule = new Schedule();
        populateScheduleFromDto(schedule, entities.getRoom(), entities.getCourse(), entities.getUser(), scheduleDto);
        schedule.setStatus(Schedule.Status.PENDING);

        // Set audit information
        schedule.setCreatedByEmail(entities.getUser().getEmail());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        logger.debug("Schedule created successfully with id: {}", savedSchedule.getId());
        return convertToDto(savedSchedule);
    }

    // Update schedule
    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        logger.debug("Updating schedule with id: {}", id);

        // Check if schedule exists
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        // Find and validate entities
        EntityResults entities = findAndValidateEntities(scheduleDto);
        logger.debug("Validated entities for update - room: {}, course: {}, user: {}",
                entities.getRoom().getRoomNumber(),
                entities.getCourse().getCourseCode(),
                entities.getUser().getEmail());

        // Check for conflicts with other schedules (excluding this one)
        checkForScheduleConflicts(entities.getRoom(), scheduleDto.getDate(),
                scheduleDto.getStartTime(), scheduleDto.getEndTime(), id);
        logger.debug("No schedule conflicts found for update");

        // Update schedule details
        populateScheduleFromDto(schedule, entities.getRoom(), entities.getCourse(), entities.getUser(), scheduleDto);
        // Note: We don't update the status here since that's done through a separate endpoint

        // Set audit information
        schedule.setUpdatedByEmail(entities.getUser().getEmail());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        logger.debug("Schedule updated successfully: {}", updatedSchedule.getId());
        return convertToDto(updatedSchedule);
    }
    
    // Utility class to hold entity lookup results
    private static class EntityResults {
        private final Room room;
        private final Course course;
        private final User user;

        public EntityResults(Room room, Course course, User user) {
            this.room = room;
            this.course = course;
            this.user = user;
        }

        public Room getRoom() { return room; }
        public Course getCourse() { return course; }
        public User getUser() { return user; }
    }

    // Delete schedule
    @Transactional
    public void deleteSchedule(Long id) {
        logger.debug("Deleting schedule with id: {}", id);
        if (!scheduleRepository.existsById(id)) {
            logger.error("Schedule not found with id: {}", id);
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        logger.debug("Schedule successfully deleted with id: {}", id);
        scheduleRepository.deleteById(id);
    }

    // Get schedules by date
    @Transactional(readOnly = true)
    @Cacheable(value = "schedulesByDate", key = "#date.toString()")
    public List<ScheduleDto> getSchedulesByDate(LocalDate date) {
        logger.debug("Fetching schedules for date: {}", date);
        List<Schedule> schedules = scheduleRepository.findAllSchedulesForDate(date);
        List<ScheduleDto> scheduleDtoByDate = schedules.parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} schedules for date: {}", scheduleDtoByDate.size());
        return scheduleDtoByDate;
    }

    // Get schedules by user ID
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByUser(Long userId) {
        logger.debug("Fetching schedules for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        List<ScheduleDto> scheduleDtoByUser = scheduleRepository.findByUser(user).parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} schedules for user id: {}", scheduleDtoByUser.size(), userId);
        return scheduleDtoByUser;
    }

    // Get schedules by user email
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByEmail(String email) {
        logger.debug("Fetching schedules for user email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });

        List<ScheduleDto> scheduleDtoByEmail = scheduleRepository.findByUser(user).parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} schedules for user email: {}", scheduleDtoByEmail.size(), email);
        return scheduleDtoByEmail;
    }

    // Create a recurring schedule based on a pattern
    @Transactional
    public List<ScheduleDto> createRecurringSchedule(RecurringScheduleRequestDto requestDto) {
        // Get recurrence pattern and base schedule
        RecurrencePatternDto pattern = requestDto.getRecurrencePattern();
        BaseScheduleDto baseSchedule = requestDto.getBaseSchedule();

        logger.info("Creating recurring schedule with pattern starting {} and ending {}",
                pattern.getStartDate(), pattern.getEndDate());

        logger.debug("Processing recurring schedule for room: {}, course: {}, user: {}",
                baseSchedule.getRoomId(), baseSchedule.getCourseId(), baseSchedule.getUserId());

        // Validate room and user exist
        Room room = roomRepository.findById(baseSchedule.getRoomId())
                .orElseThrow(() -> {
                    logger.error("Room not found with id: {}", baseSchedule.getRoomId());
                    return new ResourceNotFoundException("Room not found with id: " + baseSchedule.getRoomId());
                });

        // Check if user exists
        User user = userRepository.findById(baseSchedule.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", baseSchedule.getUserId());
                    return new ResourceNotFoundException("User not found with id: " + baseSchedule.getUserId());
                });

        // Check if course exists
        Course course = courseRepository.findById(baseSchedule.getCourseId())
                .orElseThrow(() -> {
                    logger.error("Course not found with id: {}", baseSchedule.getCourseId());
                    return new ResourceNotFoundException("Course not found with id: " + baseSchedule.getCourseId());
                });

        // Generate all dates in the pattern - using pattern variable
        List<LocalDate> scheduleDates = generateDatesByPattern(pattern);
        logger.debug("Generated {} dates for recurring schedule", scheduleDates.size());

        // Check for conflicts on all dates
        checkForConflictsInParallel(room, scheduleDates, baseSchedule.getStartTime(), baseSchedule.getEndTime());

        // Create schedules for all dates - batch insert
        List<Schedule> schedulesToCreate = new ArrayList<>();

        for (LocalDate date : scheduleDates) {
            Schedule schedule = new Schedule();
            schedule.setRoom(room);
            schedule.setUser(user);
            schedule.setDate(date);
            schedule.setStartTime(baseSchedule.getStartTime());
            schedule.setEndTime(baseSchedule.getEndTime());
            schedule.setCourse(course);
            schedule.setStatus(Schedule.Status.PENDING);
            schedule.setCreatedByEmail(user.getEmail());
            schedule.setUpdatedByEmail(user.getEmail());

            schedulesToCreate.add(schedule);
        }

        // Batch save all schedules at once
        List<Schedule> createdSchedules = scheduleRepository.saveAll(schedulesToCreate);

        // Convert to DTOs in parallel
        return createdSchedules.parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Generate dates based on the recurrence pattern
    private List<LocalDate> generateDatesByPattern(RecurrencePatternDto pattern) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = pattern.getStartDate();

        while (!currentDate.isAfter(pattern.getEndDate())) {
            // Get day of week (Java's DayOfWeek is 1-based with Monday=1, Sunday=7)
            // Convert to our 0-based system (Sunday=0, Monday=1)
            int dayOfWeek = currentDate.getDayOfWeek().getValue() % 7; // Convert to 0-based

            if (pattern.getDaysOfWeek().contains(dayOfWeek)) {
                dates.add(currentDate);
            }

            currentDate = currentDate.plusDays(1);
        }

        return dates;
    }

    // Update schedule status
    @Transactional
    public ScheduleDto updateScheduleStatus(Long id, Schedule.Status status) {
        logger.debug("Updating schedule status with id: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        logger.debug("Schedule status updated successfully: {}", updatedSchedule.getId());
        return convertToDto(updatedSchedule);
    }

    // Batch update status for multiple schedules
    @Transactional
    public List<ScheduleDto> updateScheduleStatusBatch(List<Long> ids, Schedule.Status status, User currentUser) {
        logger.info("Batch updating status to {} for {} schedules", status, ids.size());

        // Find all schedules with the given IDs
        List<Schedule> schedules = scheduleRepository.findAllById(ids);

        // Check if any schedules were not found
        if (schedules.size() < ids.size()) {
            logger.warn("Some schedules were not found during batch update. Requested: {}, Found: {}",
                    ids.size(), schedules.size());
        }

        // Copy schedules to avoid concurrent modification issues
        List<Schedule> schedulesToUpdate = new ArrayList<>(schedules);
        schedulesToUpdate.parallelStream().forEach(schedule -> {
            schedule.setStatus(status);
            schedule.setUpdatedByEmail(currentUser.getEmail());
        });

        // Save all at once (this uses a single transaction)
        List<Schedule> updatedSchedules = scheduleRepository.saveAll(schedulesToUpdate);
        logger.info("Successfully updated status for {} schedules", updatedSchedules.size());

        // Convert to DTOs and return
        return updatedSchedules.parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Batch delete schedules
    @Transactional
    public void deleteSchedulesBatch(List<Long> ids) {
        logger.debug("Deleting schedules batch with ids: {}", ids);
        // Find all schedules with the given IDs
        List<Schedule> schedules = scheduleRepository.findAllById(ids);

        // Check if any schedules were not found
        if (schedules.size() < ids.size()) {
            // Log a warning
            logger.warn("Some schedules were not found during batch delete. Requested: {}, Found: {}",
                    ids.size(), schedules.size());
        }

        // Delete all found schedules at once
        logger.debug("Successfully deleting schedules batch with ids: {}", ids);
        scheduleRepository.deleteAllInBatch(schedules);
    }

    // Update the time conflict check to work with LocalTime directly
    private boolean hasTimeConflict(Schedule existingSchedule, LocalTime newStartTime, LocalTime newEndTime) {
        return !newEndTime.isBefore(existingSchedule.getStartTime()) &&
                !newStartTime.isAfter(existingSchedule.getEndTime());
    }

    // Helper method to find and validate entities
    private EntityResults findAndValidateEntities(ScheduleDto scheduleDto) {
        // Check if room exists
        Room room = roomRepository.findById(scheduleDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + scheduleDto.getRoomId()));

        // Check if course exists
        Course course = courseRepository.findById(scheduleDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + scheduleDto.getCourseId()));

        // Check if user exists
        User user = userRepository.findById(scheduleDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + scheduleDto.getUserId()));

        return new EntityResults(room, course, user);
    }

    // Helper method to check for schedule conflicts
    private void checkForScheduleConflicts(Room room, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeScheduleId) {
        logger.debug("Checking for schedule conflicts: room={}, date={}, time={}-{}, excludeId={}",
                room.getRoomNumber(), date, startTime, endTime, excludeScheduleId);
        List<Schedule> conflictingSchedules = scheduleRepository.findByRoomAndDate(room, date);
        for (Schedule existingSchedule : conflictingSchedules) {
            // Skip comparing with itself if updating
            if ((!existingSchedule.getId().equals(excludeScheduleId)) &&
                    hasTimeConflict(existingSchedule, startTime, endTime)) {
                throw new ScheduleConflictException("The room has already a schedule during the requested time");
            }
        }
    }

    // Check for conflicts in parallel
    private void checkForConflictsInParallel(Room room, List<LocalDate> dates,
                                             LocalTime startTime, LocalTime endTime) {
        List<String> conflictingDates = dates.parallelStream()
                .filter(date -> {
                    List<Schedule> existingSchedules = scheduleRepository.findByRoomAndDate(room, date);
                    return existingSchedules.stream()
                            .anyMatch(existing -> hasTimeConflict(existing, startTime, endTime));
                })
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        if (!conflictingDates.isEmpty()) {
            String errorMessage = "The room has already a schedule during the requested time on dates: "
                    + String.join(", ", conflictingDates);
            throw new ScheduleConflictException(errorMessage);
        }
    }

    // Helper method to populate schedule from DTO
    private void populateScheduleFromDto(Schedule schedule, Room room, Course course, User user, ScheduleDto scheduleDto) {
        schedule.setRoom(room);
        schedule.setUser(user);
        schedule.setDate(scheduleDto.getDate());
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setEndTime(scheduleDto.getEndTime());
        schedule.setCourse(course);

        // Set created by email if it's a new schedule (id is null)
        if (schedule.getId() == null) {
            schedule.setCreatedByEmail(user.getEmail());
        }

        // Always update the updated by email
        schedule.setUpdatedByEmail(user.getEmail());
    }

    // Convert Schedule entity to ScheduleDto
    // Thread-safe method for DTO conversion with caching user info
    private ScheduleDto convertToDto(Schedule schedule) {
        // Get user information with local caching to reduce database hits
         String createdByName = getUserName(schedule.getCreatedByEmail());
         String updatedByName = getUserName(schedule.getUpdatedByEmail());

        return new ScheduleDto(
                schedule.getId(),
                schedule.getRoom().getId(),
                schedule.getRoom().getRoomNumber(),
                schedule.getUser().getId(),
                schedule.getUser().getName(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getCourse().getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getDescription(),
                schedule.getStatus(),
                schedule.getCreationDate(),
                schedule.getLastUpdated(),
                schedule.getCreatedByEmail(),
                createdByName,
                schedule.getUpdatedByEmail(),
                updatedByName
        );
    }

    // Get username by email with caching
    @Cacheable(value = "userDetails", key = "#email")
    public String getUserName(String email) {
        if (email == null) return null;

        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getName() : email;
    }
    
}