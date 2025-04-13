package my.projects.classroomschedulerapp.repository;

import my.projects.classroomschedulerapp.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByRoomNumber(String roomNumber);

    List<Room> findByCapacityGreaterThanEqual(int capacity);

    List<Room> findByHasProjector(boolean hasProjector);

    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
            "(SELECT b.room.id FROM Schedule b WHERE b.date = ?1 AND " +
            "((b.startTime <= ?3 AND b.endTime >= ?3) OR " +
            "(b.startTime <= ?2 AND b.endTime >= ?2) OR " +
            "(b.startTime >= ?2 AND b.endTime <= ?3)))")
    List<Room> findAvailableRooms(LocalDate date, LocalTime startTime, LocalTime endTime);
}