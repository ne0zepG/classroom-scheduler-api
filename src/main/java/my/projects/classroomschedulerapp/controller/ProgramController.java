package my.projects.classroomschedulerapp.controller;

import my.projects.classroomschedulerapp.dto.ProgramDto;
import my.projects.classroomschedulerapp.service.ProgramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {
    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    // This endpoint allows for retrieving all programs
    @GetMapping
    public ResponseEntity<List<ProgramDto>> getAllPrograms() {
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    // This endpoint allows for retrieving a program by its ID
    @GetMapping("/{id}")
    public ResponseEntity<ProgramDto> getProgramById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getProgramById(id));
    }

    // This endpoint allows for retrieving programs by department ID
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ProgramDto>> getProgramsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(programService.getProgramsByDepartment(departmentId));
    }

    // This endpoint allows for creating a new program
    @PostMapping
    public ResponseEntity<ProgramDto> createProgram(@RequestBody ProgramDto programDto) {
        ProgramDto createdProgram = programService.createProgram(programDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProgram);
    }

    // This endpoint allows for updating an existing program
    @PutMapping("/{id}")
    public ResponseEntity<ProgramDto> updateProgram(@PathVariable Long id, @RequestBody ProgramDto programDto) {
        return ResponseEntity.ok(programService.updateProgram(id, programDto));
    }

    // This endpoint allows for deleting a program by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }
}
