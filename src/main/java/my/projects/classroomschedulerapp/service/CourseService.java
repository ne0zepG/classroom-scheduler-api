package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.CourseDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Course;
import my.projects.classroomschedulerapp.model.Program;
import my.projects.classroomschedulerapp.repository.CourseRepository;
import my.projects.classroomschedulerapp.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;


    public CourseService(CourseRepository courseRepository, ProgramRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.programRepository = departmentRepository;
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

    // Get courses by program ID
    public List<CourseDto> getCoursesByProgram(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException("Program not found with id: " + programId);
        }
        return courseRepository.findByProgramId(programId).stream()
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

        course.setCourseCode(courseDto.getCourseCode());
        course.setDescription(courseDto.getDescription());

        // Update program if provided
        if (courseDto.getProgramId() != null) {
            Program program = programRepository.findById(courseDto.getProgramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + courseDto.getProgramId()));
            course.setProgram(program);
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
                course.getProgram().getId(),
                course.getProgram().getName()
        );
    }

    // Convert CourseDto to Course entity
    private Course convertToEntity(CourseDto courseDto) {
        Course course = new Course();
        course.setCourseCode(courseDto.getCourseCode());
        course.setDescription(courseDto.getDescription());

        // Set program
        Program program = programRepository.findById(courseDto.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + courseDto.getProgramId()));
        course.setProgram(program);

        return course;
    }
}
