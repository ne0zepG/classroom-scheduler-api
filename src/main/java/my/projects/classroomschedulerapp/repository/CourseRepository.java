package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Course findByCourseCode(String courseCode);
    List<Course> findByProgramId(Long programId);
    boolean existsByCourseCode(String courseCode);
}