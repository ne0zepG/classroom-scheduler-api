package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByDepartmentId(Long departmentId);
    boolean existsByCode(String code);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}
