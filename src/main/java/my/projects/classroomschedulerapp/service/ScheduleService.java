package my.projects.classroomschedulerapp.service;

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

    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ScheduleDto getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return convertToDto(schedule);
    }

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

    public ScheduleDto updateScheduleStatus(Long id, Schedule.Status status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToDto(updatedSchedule);
    }

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
                throw new ScheduleConflictException("The room is already booked during the requested time");
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

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    public List<ScheduleDto> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findAllSchedulesForDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

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
}