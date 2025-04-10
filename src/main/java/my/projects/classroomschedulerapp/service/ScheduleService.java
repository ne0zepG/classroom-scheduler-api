package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.BaseScheduleDto;
import my.projects.classroomschedulerapp.dto.RecurrencePatternDto;
import my.projects.classroomschedulerapp.dto.RecurringScheduleRequestDto;
import my.projects.classroomschedulerapp.dto.ScheduleDto;
import my.projects.classroomschedulerapp.exception.ScheduleConflictException;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.model.User;
import my.projects.classroomschedulerapp.repository.ScheduleRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
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
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
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
        // Check if room exists
        Room room = roomRepository.findById(scheduleDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + scheduleDto.getRoomId()));

        // Check if user exists
        User user = userRepository.findById(scheduleDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + scheduleDto.getUserId()));

        // Check for schedule conflicts
        List<Schedule> conflictingSchedules = scheduleRepository.findByRoomAndDate(room, scheduleDto.getDate());
        for (Schedule existingSchedule : conflictingSchedules) {
            if (hasTimeConflict(existingSchedule, scheduleDto)) {
                throw new ScheduleConflictException("The room has already an schedule during the requested time");
            }
        }

        // Create schedule
        Schedule schedule = new Schedule();
        schedule.setRoom(room);
        schedule.setUser(user);
        schedule.setDate(scheduleDto.getDate());
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setEndTime(scheduleDto.getEndTime());
        schedule.setPurpose(scheduleDto.getPurpose());
        schedule.setStatus(Schedule.Status.PENDING);

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    // Update schedule status
    public ScheduleDto updateScheduleStatus(Long id, Schedule.Status status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

    // Update schedule
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        // Check if schedule exists
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        // Check if room exists
        Room room = roomRepository.findById(scheduleDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + scheduleDto.getRoomId()));

        // Check if user exists
        User user = userRepository.findById(scheduleDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + scheduleDto.getUserId()));

        // Check for conflicts with other schedule (excluding this one)
        List<Schedule> conflictingSchedules = scheduleRepository.findByRoomAndDate(room, scheduleDto.getDate());
        for (Schedule existingSchedule : conflictingSchedules) {
            // Skip comparing with itself
            if (!existingSchedule.getId().equals(id) && hasTimeConflict(existingSchedule, scheduleDto)) {
                throw new ScheduleConflictException("The room has already an schedule during the requested time");
            }
        }

        // Update schedule details
        schedule.setRoom(room);
        schedule.setUser(user);
        schedule.setDate(scheduleDto.getDate());
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setEndTime(scheduleDto.getEndTime());
        schedule.setPurpose(scheduleDto.getPurpose());
        // Note: We don't update the status here since that's done through a separate endpoint

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
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

    private boolean hasTimeConflict(Schedule existing, ScheduleDto requested) {
        // Check if time periods overlap
        return !((existing.getEndTime().isBefore(requested.getStartTime()) || 
                existing.getEndTime().equals(requested.getStartTime())) ||
                (existing.getStartTime().isAfter(requested.getEndTime()) || 
                existing.getStartTime().equals(requested.getEndTime())));
    }

    private ScheduleDto convertToDto(Schedule schedule) {
        return new ScheduleDto(
                schedule.getId(),
                schedule.getRoom().getId(),
                schedule.getRoom().getRoomNumber(),
                schedule.getUser().getId(),
                schedule.getUser().getName(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getPurpose(),
                schedule.getStatus()
        );
    }

    // Create a recurring schedule based on a pattern
    public List<ScheduleDto> createRecurringSchedule(RecurringScheduleRequestDto requestDto) {
        // Get recurrence pattern
        RecurrencePatternDto pattern = requestDto.getRecurrencePattern();
        BaseScheduleDto baseSchedule = requestDto.getBaseSchedule();

        // Validate room and user exist
        Room room = roomRepository.findById(baseSchedule.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + baseSchedule.getRoomId()));

        User user = userRepository.findById(baseSchedule.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + baseSchedule.getUserId()));

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
            schedule.setPurpose(baseSchedule.getPurpose());
            schedule.setStatus(Schedule.Status.PENDING);

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

    // Batch update status for multiple schedules
    public List<ScheduleDto> updateScheduleStatusBatch(List<Long> ids, Schedule.Status status) {
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
        }

        // Save all at once (this uses a single transaction)
        List<Schedule> updatedSchedules = scheduleRepository.saveAll(schedules);

        // Convert to DTOs and return
        return updatedSchedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update the time conflict check to work with LocalTime directly
    private boolean hasTimeConflict(Schedule existingSchedule, LocalTime newStartTime, LocalTime newEndTime) {
        return !newEndTime.isBefore(existingSchedule.getStartTime()) &&
                !newStartTime.isAfter(existingSchedule.getEndTime());
    }
}