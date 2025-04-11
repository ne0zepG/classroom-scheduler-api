package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    Building findByName(String name);
}
