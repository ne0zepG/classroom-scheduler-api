package my.projects.classroomschedulerapp.controller;

import my.projects.classroomschedulerapp.dto.BatchStatusUpdateRequestDto;
import my.projects.classroomschedulerapp.dto.RecurringScheduleRequestDto;
import my.projects.classroomschedulerapp.dto.ScheduleDto;
import my.projects.classroomschedulerapp.model.Schedule;
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

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // This endpoint allows for retrieving all schedules
    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    // This endpoint allows for retrieving a schedule by its ID
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
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

    // This endpoint allows for filtering schedules by date
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ScheduleDto>> getScheduleByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDate(date));
    }

    // This endpoint allows for filtering schedules by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByUser(userId));
    }

    // This endpoint allows for creating a recurring schedule
    @PostMapping("/recurring")
    public ResponseEntity<List<ScheduleDto>> createRecurringSchedule(@RequestBody RecurringScheduleRequestDto requestDto) {
        List<ScheduleDto> createdSchedules = scheduleService.createRecurringSchedule(requestDto);
        return new ResponseEntity<>(createdSchedules, HttpStatus.CREATED);
    }

    // This endpoint allows for batch updating of schedule statuses
    @PatchMapping("/batch/status")
    public ResponseEntity<List<ScheduleDto>> updateScheduleStatusBatch(
            @RequestBody BatchStatusUpdateRequestDto request) {
        List<ScheduleDto> updatedSchedules = scheduleService.updateScheduleStatusBatch(
                request.getIds(), request.getStatus());
        return ResponseEntity.ok(updatedSchedules);
    }
}