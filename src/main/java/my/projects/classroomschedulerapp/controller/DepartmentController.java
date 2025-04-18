package my.projects.classroomschedulerapp.controller;

import my.projects.classroomschedulerapp.dto.DepartmentDto;
import my.projects.classroomschedulerapp.service.DepartmentService;
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
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // This endpoint allows for retrieving all departments asynchronously
    @GetMapping
    public CompletableFuture<ResponseEntity<List<DepartmentDto>>> getAllDepartmentsAsync() {
        return departmentService.getAllDepartmentsAsync()
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for retrieving a department by its ID asynchronously
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<DepartmentDto>> getDepartmentByIdAsync(@PathVariable Long id) {
        return departmentService.getDepartmentByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for creating a new department
    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@RequestBody DepartmentDto departmentDto) {
        DepartmentDto createdDepartment = departmentService.createDepartment(departmentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    // This endpoint allows for updating an existing department
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @RequestBody DepartmentDto departmentDto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentDto));
    }

    // This endpoint allows for deleting a department by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
