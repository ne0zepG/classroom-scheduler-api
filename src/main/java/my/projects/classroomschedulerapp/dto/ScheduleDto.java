package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.projects.classroomschedulerapp.model.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long userId;
    private String userName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long courseId;
    private String courseCode;
    private String courseDescription;
    private Schedule.Status status;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdated;
    private String createdByEmail;
    private String createdByName;
    private String updatedByEmail;
    private String updatedByName;
}