package my.projects.classroomschedulerapp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String courseCode;
    private String description;
    private Long departmentId;
    private String departmentName;
}
