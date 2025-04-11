package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.projects.classroomschedulerapp.model.Schedule;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseScheduleDto {
    private Long roomId;
    private String roomNumber;
    private Long userId;
    private String userName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long courseId;
    private String courseCode;
    private String courseDescription;
    private Schedule.Status status;
}