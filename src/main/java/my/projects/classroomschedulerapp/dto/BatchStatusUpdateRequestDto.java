package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.projects.classroomschedulerapp.model.Schedule;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatusUpdateRequestDto {
    private List<Long> ids;
    private Schedule.Status status;
}
