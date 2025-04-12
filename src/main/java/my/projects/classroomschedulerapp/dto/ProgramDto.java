package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDto {
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private String departmentName;
}
