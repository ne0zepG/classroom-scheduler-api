package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.CourseDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Course;
import my.projects.classroomschedulerapp.model.Program;
import my.projects.classroomschedulerapp.repository.CourseRepository;
import my.projects.classroomschedulerapp.repository.ProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;


    public CourseService(CourseRepository courseRepository, ProgramRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.programRepository = departmentRepository;
    }

    // Get all courses
    public List<CourseDto> getAllCourses() {
        logger.debug("Fetching all courses");
        List<CourseDto> courses = courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} courses", courses.size());
        return courses;
    }

    // Get course by ID
    public CourseDto getCourseById(Long id) {
        logger.debug("Fetching course with id: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Course not found with id: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });
        logger.debug("Found course: {}", course.getCourseCode());
        return convertToDto(course);
    }

    // Get courses by program ID
    public List<CourseDto> getCoursesByProgram(Long programId) {
        logger.debug("Fetching courses for program id: {}", programId);

        if (!programRepository.existsById(programId)) {
            logger.error("Program not found with id: {}", programId);
            throw new ResourceNotFoundException("Program not found with id: " + programId);
        }

        List<CourseDto> courses = courseRepository.findByProgramId(programId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        logger.debug("Found {} courses for program id: {}", courses.size(), programId);
        return courses;
    }

    // Create a new course
    public CourseDto createCourse(CourseDto courseDto) {
        logger.info("Creating new course: {}", courseDto.getCourseCode());

        try {
            Course course = convertToEntity(courseDto);
            Course savedCourse = courseRepository.save(course);
            logger.info("Course created successfully with id: {}", savedCourse.getId());
            return convertToDto(savedCourse);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to create course: {}", e.getMessage());
            throw e;
        }
    }

    // Update an existing course
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        logger.info("Updating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Course not found with id: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });

        logger.debug("Found course to update: {}", course.getCourseCode());
        course.setCourseCode(courseDto.getCourseCode());
        course.setDescription(courseDto.getDescription());

        // Update program if provided
        if (courseDto.getProgramId() != null) {
            logger.debug("Updating course program to id: {}", courseDto.getProgramId());
            Program program = programRepository.findById(courseDto.getProgramId())
                    .orElseThrow(() -> {
                        logger.error("Program not found with id: {}", courseDto.getProgramId());
                        return new ResourceNotFoundException("Program not found with id: " + courseDto.getProgramId());
                    });
            course.setProgram(program);
        }

        Course updatedCourse = courseRepository.save(course);
        logger.info("Course updated successfully: {}", updatedCourse.getId());
        return convertToDto(updatedCourse);
    }

    // Delete a course
    public void deleteCourse(Long id) {
        logger.info("Deleting course with id: {}", id);

        if (!courseRepository.existsById(id)) {
            logger.error("Course not found with id: {}", id);
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }

        courseRepository.deleteById(id);
        logger.info("Course successfully deleted with id: {}", id);
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
