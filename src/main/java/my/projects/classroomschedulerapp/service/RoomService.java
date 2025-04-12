package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.RoomDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Building;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.repository.BuildingRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

   public RoomService(RoomRepository roomRepository, BuildingRepository buildingRepository) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    // Get all rooms
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get room by ID
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return convertToDto(room);
    }

    // Create a new room
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = convertToEntity(roomDto);
        Room savedRoom = roomRepository.save(room);
        return convertToDto(savedRoom);
    }

    // Update an existing room
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        
        room.setRoomNumber(roomDto.getRoomNumber());
        
        // Get building by ID
        Building building = buildingRepository.findById(roomDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + roomDto.getBuildingId()));
        room.setBuilding(building);
        
        room.setCapacity(roomDto.getCapacity());
        room.setHasProjector(roomDto.isHasProjector());
        room.setHasComputers(roomDto.isHasComputers());
        
        Room updatedRoom = roomRepository.save(room);
        return convertToDto(updatedRoom);
    }

    // Delete a room
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    // Find available rooms for a given date and time
    public List<RoomDto> findAvailableRooms(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return roomRepository.findAvailableRooms(date, startTime, endTime).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Convert Room entity to DTO
    private RoomDto convertToDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getRoomNumber(),
                room.getBuilding().getId(),
                room.getBuilding().getName(),
                room.getCapacity(),
                room.isHasProjector(),
                room.isHasComputers()
        );
    }

    // Convert Room DTO to entity
    private Room convertToEntity(RoomDto roomDto) {
        Room room = new Room();
        room.setRoomNumber(roomDto.getRoomNumber());
        
        // Get building by ID
        Building building = buildingRepository.findById(roomDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + roomDto.getBuildingId()));
        room.setBuilding(building);
        
        room.setCapacity(roomDto.getCapacity());
        room.setHasProjector(roomDto.isHasProjector());
        room.setHasComputers(roomDto.isHasComputers());
        return room;
    }
}