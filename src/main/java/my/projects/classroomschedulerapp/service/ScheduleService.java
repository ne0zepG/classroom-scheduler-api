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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

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

    // Get all schedules
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get schedule by ID
    public ScheduleDto getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return convertToDto(schedule);
    }

    // Create a new schedule
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        // Find and validate entities
        EntityResults entities = findAndValidateEntities(scheduleDto);

        // Check for schedule conflicts
        checkForScheduleConflicts(entities.getRoom(), scheduleDto.getDate(),
                scheduleDto.getStartTime(), scheduleDto.getEndTime(), null);

        // Create schedule
        Schedule schedule = new Schedule();
        populateScheduleFromDto(schedule, entities.getRoom(), entities.getCourse(), entities.getUser(), scheduleDto);
        schedule.setStatus(Schedule.Status.PENDING);

        // Set audit information
        schedule.setCreatedByEmail(entities.getUser().getEmail());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    // Update schedule
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        // Check if schedule exists
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        // Find and validate entities
        EntityResults entities = findAndValidateEntities(scheduleDto);

        // Check for conflicts with other schedules (excluding this one)
        checkForScheduleConflicts(entities.getRoom(), scheduleDto.getDate(),
                scheduleDto.getStartTime(), scheduleDto.getEndTime(), id);

        // Update schedule details
        populateScheduleFromDto(schedule, entities.getRoom(), entities.getCourse(), entities.getUser(), scheduleDto);
        // Note: We don't update the status here since that's done through a separate endpoint

        // Set audit information
        schedule.setUpdatedByEmail(entities.getUser().getEmail());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
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
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    // Get schedules by date
    public List<ScheduleDto> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findAllSchedulesForDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get schedules by user ID
    public List<ScheduleDto> getSchedulesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return scheduleRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get schedules by user email
    public List<ScheduleDto> getSchedulesByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return scheduleRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Create a recurring schedule based on a pattern
    public List<ScheduleDto> createRecurringSchedule(RecurringScheduleRequestDto requestDto) {
        // Get recurrence pattern
        RecurrencePatternDto pattern = requestDto.getRecurrencePattern();
        BaseScheduleDto baseSchedule = requestDto.getBaseSchedule();

        // Validate room and user exist
        Room room = roomRepository.findById(baseSchedule.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + baseSchedule.getRoomId()));

        // Check if user exists
        User user = userRepository.findById(baseSchedule.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + baseSchedule.getUserId()));

        // Check if course exists
        Course course = courseRepository.findById(baseSchedule.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + baseSchedule.getCourseId()));

        // Generate all dates in the pattern
        List<LocalDate> scheduleDates = generateDatesByPattern(pattern);

        // Check for conflicts on all dates
        for (LocalDate date : scheduleDates) {
            List<Schedule> conflictingSchedules = scheduleRepository.findByRoomAndDate(room, date);
            for (Schedule existingSchedule : conflictingSchedules) {
                if (hasTimeConflict(existingSchedule, baseSchedule.getStartTime(), baseSchedule.getEndTime())) {
                    throw new ScheduleConflictException("The room has already a schedule during the requested time on " + date);
                }
            }
        }

        // Create schedules for all dates
        List<Schedule> createdSchedules = new ArrayList<>();

        for (LocalDate date : scheduleDates) {
            Schedule schedule = new Schedule();
            schedule.setRoom(room);
            schedule.setUser(user);
            schedule.setDate(date);
            schedule.setStartTime(baseSchedule.getStartTime());
            schedule.setEndTime(baseSchedule.getEndTime());
            schedule.setCourse(course);
            schedule.setStatus(Schedule.Status.PENDING);

            // Audit information
            schedule.setCreatedByEmail(user.getEmail());
            schedule.setUpdatedByEmail(user.getEmail());

            createdSchedules.add(scheduleRepository.save(schedule));
        }

        // Convert to DTOs and return
        return createdSchedules.stream()
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
    public ScheduleDto updateScheduleStatus(Long id, Schedule.Status status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    // Batch update status for multiple schedules
    public List<ScheduleDto> updateScheduleStatusBatch(List<Long> ids, Schedule.Status status, User currentUser) {
        // Find all schedules with the given IDs
        List<Schedule> schedules = scheduleRepository.findAllById(ids);

        // Check if any schedules were not found
        if (schedules.size() < ids.size()) {
            // Log a warning
            System.out.println("Some schedules were not found during batch update");
        }

        // Update status for all found schedules
        for (Schedule schedule : schedules) {
            schedule.setStatus(status);
            schedule.setUpdatedByEmail(currentUser.getEmail());
        }

        // Save all at once (this uses a single transaction)
        List<Schedule> updatedSchedules = scheduleRepository.saveAll(schedules);

        // Convert to DTOs and return
        return updatedSchedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Batch delete schedules
    public void deleteSchedulesBatch(List<Long> ids) {
        // Find all schedules with the given IDs
        List<Schedule> schedules = scheduleRepository.findAllById(ids);

        // Check if any schedules were not found
        if (schedules.size() < ids.size()) {
            // Log a warning
            System.out.println("Some schedules were not found during batch delete");
        }

        // Delete all found schedules at once
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
        List<Schedule> conflictingSchedules = scheduleRepository.findByRoomAndDate(room, date);
        for (Schedule existingSchedule : conflictingSchedules) {
            // Skip comparing with itself if updating
            if ((!existingSchedule.getId().equals(excludeScheduleId)) &&
                    hasTimeConflict(existingSchedule, startTime, endTime)) {
                throw new ScheduleConflictException("The room has already a schedule during the requested time");
            }
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
    private ScheduleDto convertToDto(Schedule schedule) {
        // TODO: Replace with actual user name retrieval
        String createdByName = "admin@college.edu";
        String updatedByName = "admin@college.edu";

        if (schedule.getCreatedByEmail() != null) {
            User createdBy = userRepository.findByEmail(schedule.getCreatedByEmail()).orElse(null);
            if (createdBy != null) {
                createdByName = createdBy.getName();
            }
        }

        if (schedule.getUpdatedByEmail() != null) {
            User updatedBy = userRepository.findByEmail(schedule.getUpdatedByEmail()).orElse(null);
            if (updatedBy != null) {
                updatedByName = updatedBy.getName();
            }
        }

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
    
}