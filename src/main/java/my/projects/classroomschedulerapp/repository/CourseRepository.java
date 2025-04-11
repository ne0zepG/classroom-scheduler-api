package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Course findByCourseCode(String courseCode);
    List<Course> findByDepartmentId(Long departmentId);
    boolean existsByCourseCode(String courseCode);
}