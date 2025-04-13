package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.model.Schedule;
import my.projects.classroomschedulerapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByRoom(Room room);

    List<Schedule> findByRoomAndDate(Room room, LocalDate date);

    List<Schedule> findByUser(User user);

    List<Schedule> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM Schedule b WHERE b.date = ?1")
    List<Schedule> findAllSchedulesForDate(LocalDate date);
}