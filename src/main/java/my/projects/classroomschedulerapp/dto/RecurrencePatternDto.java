package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurrencePatternDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> daysOfWeek; // 0 = Sunday, 1 = Monday, etc.
}