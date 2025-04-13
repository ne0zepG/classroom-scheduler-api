package my.projects.classroomschedulerapp.controller;

import my.projects.classroomschedulerapp.dto.BatchStatusUpdateRequestDto;
import my.projects.classroomschedulerapp.dto.RecurringScheduleRequestDto;
import my.projects.classroomschedulerapp.dto.ScheduleDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.User;
import my.projects.classroomschedulerapp.repository.UserRepository;
import my.projects.classroomschedulerapp.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    public ScheduleController(ScheduleService scheduleService, UserRepository userRepository) {
        this.scheduleService = scheduleService;
        this.userRepository = userRepository;
    }

    // This endpoint allows for retrieving all schedules asynchronously
    @GetMapping
    public CompletableFuture<ResponseEntity<List<ScheduleDto>>> getAllSchedulesAsync() {
        return scheduleService.getAllSchedulesAsync()
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for retrieving a schedule by its ID asynchronously
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<ScheduleDto>> getScheduleByIdAsync(@PathVariable Long id) {
        return scheduleService.getScheduleByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for creating a new schedule
    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(@RequestBody ScheduleDto scheduleDto) {
        return new ResponseEntity<>(scheduleService.createSchedule(scheduleDto), HttpStatus.CREATED);
    }

    // This endpoint allows for updating an existing schedule
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleDto scheduleDto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDto));
    }

    // This endpoint allows for updating the status of a schedule
    @PatchMapping("/{id}/status")
    public ResponseEntity<ScheduleDto> updateScheduleStatus(
            @PathVariable Long id,
            @RequestParam Schedule.Status status) {
        return ResponseEntity.ok(scheduleService.updateScheduleStatus(id, status));
    }

    // This endpoint allows for deleting a schedule by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    // This endpoint allows for filtering schedules by date asynchronously
    @GetMapping("/date/{date}")
    public CompletableFuture<ResponseEntity<List<ScheduleDto>>> getScheduleByDateAsync(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getSchedulesByDateAsync(date)
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for filtering schedules by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByUser(userId));
    }

    // This endpoint allows for filtering schedules by email
    @GetMapping("/email/{email}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByEmail(@PathVariable String email) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByEmail(email);
        return ResponseEntity.ok(schedules);
    }

    // This endpoint allows for creating a recurring schedule asynchronously
    @PostMapping("/recurring")
    public CompletableFuture<ResponseEntity<List<ScheduleDto>>> createRecurringScheduleAsync(
            @RequestBody RecurringScheduleRequestDto requestDto) {
        return scheduleService.createRecurringScheduleAsync(requestDto)
                .thenApply(schedules -> new ResponseEntity<>(schedules, HttpStatus.CREATED));
    }

    // This endpoint allows for batch updating of schedule statuses
    @PatchMapping("/batch/status")
    public ResponseEntity<List<ScheduleDto>> updateScheduleStatusBatch(
            @RequestBody BatchStatusUpdateRequestDto request) {
        // TODO: Implement authentication and authorization checks
        User currentUser = userRepository.findByEmail("admin@college.edu")
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        List<ScheduleDto> updatedSchedules = scheduleService.updateScheduleStatusBatch(
                request.getIds(), request.getStatus(), currentUser);
        return ResponseEntity.ok(updatedSchedules);
    }

    // This endpoint allows for batch deletion of schedules
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteSchedulesBatch(@RequestBody List<Long> ids) {
        scheduleService.deleteSchedulesBatch(ids);
        return ResponseEntity.noContent().build();
    }
}