package my.projects.classroomschedulerapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomNumber;
    private String building;
    private int capacity;
    private boolean hasProjector;
    private boolean hasComputers;
}