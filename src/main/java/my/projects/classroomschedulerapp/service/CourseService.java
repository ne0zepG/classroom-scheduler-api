package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.CourseDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Course;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.repository.CourseRepository;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    // Get all courses
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get course by ID
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return convertToDto(course);
    }

    // Get courses by department ID
    public List<CourseDto> getCoursesByDepartment(Long departmentId) {
        // Verify department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Create a new course
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = convertToEntity(courseDto);
        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    // Update an existing course
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Update course fields
        course.setCourseCode(courseDto.getCourseCode());
        course.setDescription(courseDto.getDescription());

        // Update department if provided
        if (courseDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDto.getDepartmentId()));
            course.setDepartment(department);
        }

        Course updatedCourse = courseRepository.save(course);
        return convertToDto(updatedCourse);
    }

    // Delete a course
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    // Convert Course entity to CourseDto
    private CourseDto convertToDto(Course course) {
        return new CourseDto(
                course.getId(),
                course.getCourseCode(),
                course.getDescription(),
                course.getDepartment().getId(),
                course.getDepartment().getName()
        );
    }

    // Convert CourseDto to Course entity
    private Course convertToEntity(CourseDto courseDto) {
        Course course = new Course();
        course.setCourseCode(courseDto.getCourseCode());
        course.setDescription(courseDto.getDescription());

        // Set department
        Department department = departmentRepository.findById(courseDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDto.getDepartmentId()));
        course.setDepartment(department);

        return course;
    }
}
