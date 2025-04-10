package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringScheduleRequestDto {
    private BaseScheduleDto baseSchedule;
    private RecurrencePatternDto recurrencePattern;

}