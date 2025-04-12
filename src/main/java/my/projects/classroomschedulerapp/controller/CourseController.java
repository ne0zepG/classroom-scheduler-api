package my.projects.classroomschedulerapp.controller;

import my.projects.classroomschedulerapp.dto.CourseDto;
import my.projects.classroomschedulerapp.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // This endpoint allows for retrieving all courses asynchronously
    @GetMapping
    public CompletableFuture<ResponseEntity<List<CourseDto>>> getAllCoursesAsync() {
        return courseService.getAllCoursesAsync()
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for retrieving a course by its ID asynchronously
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<CourseDto>> getCourseByIdAsync(@PathVariable Long id) {
        return courseService.getCourseByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint for getting courses by program ID asynchronously
    @GetMapping("/program/{programId}")
    public CompletableFuture<ResponseEntity<List<CourseDto>>> getCoursesByProgramAsync(@PathVariable Long programId) {
        return courseService.getCoursesByProgramAsync(programId)
                .thenApply(ResponseEntity::ok);
    }

    // This endpoint allows for creating a new course
    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    // This endpoint allows for updating an existing course
    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @RequestBody CourseDto courseDto) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDto));
    }

    // This endpoint allows for deleting a course by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}