package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.RoomDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Building;
import my.projects.classroomschedulerapp.model.Room;
import my.projects.classroomschedulerapp.repository.BuildingRepository;
import my.projects.classroomschedulerapp.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final ObjectProvider<RoomService> self;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    public RoomService(ObjectProvider<RoomService> self,
                       RoomRepository roomRepository, BuildingRepository buildingRepository) {
        this.self = self;
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    // Asynchronous method to get all rooms
    @Async("taskExecutor")
    public CompletableFuture<List<RoomDto>> getAllRoomsAsync() {
        logger.debug("Asynchronously fetching all rooms");
        List<RoomDto> rooms = self.getObject().getAllRooms();
        return CompletableFuture.completedFuture(rooms);
    }

    // Asynchronous method to find available rooms
    @Async("taskExecutor")
    public CompletableFuture<List<RoomDto>> findAvailableRoomsAsync(LocalDate date, LocalTime startTime, LocalTime endTime) {
        logger.debug("Asynchronously finding available rooms for date: {}, time: {}-{}", date, startTime, endTime);
        List<RoomDto> availableRooms = self.getObject().findAvailableRooms(date, startTime, endTime);
        return CompletableFuture.completedFuture(availableRooms);
    }

    // Asynchronous method to get room by ID
    @Async("taskExecutor")
    public CompletableFuture<RoomDto> getRoomByIdAsync(Long id) {
        logger.debug("Asynchronously fetching room with id: {}", id);
        RoomDto room = self.getObject().getRoomById(id);
        return CompletableFuture.completedFuture(room);
    }

    // Get all rooms
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        logger.debug("Fetching all rooms");
        List<RoomDto> rooms = roomRepository.findAll().parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} rooms", rooms.size());
        return rooms;
    }

    // Get room by ID
    @Transactional(readOnly = true)
    @Cacheable(value = "roomDetails", key = "#id")
    public RoomDto getRoomById(Long id) {
        logger.debug("Fetching room with id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Room not found with id: {}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });
        logger.debug("Found room: {}", room.getRoomNumber());
        return convertToDto(room);
    }

    // Create a new room
    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        logger.info("Creating new room: {}", roomDto.getRoomNumber());
        try {
            Room room = convertToEntity(roomDto);
            Room savedRoom = roomRepository.save(room);
            logger.info("Room created successfully with id: {}", savedRoom.getId());
            return convertToDto(savedRoom);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to create room: {}", e.getMessage());
            throw e;
        }
    }

    // Update an existing room
    @Transactional
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        logger.info("Updating room with id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Room not found with id: {}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });

        logger.debug("Found room to update: {}", room.getRoomNumber());
        room.setRoomNumber(roomDto.getRoomNumber());

        // Get building by ID
        logger.debug("Looking up building with id: {}", roomDto.getBuildingId());
        Building building = buildingRepository.findById(roomDto.getBuildingId())
                .orElseThrow(() -> {
                    logger.error("Building not found with id: {}", roomDto.getBuildingId());
                    return new ResourceNotFoundException("Building not found with id: " + roomDto.getBuildingId());
                });
        room.setBuilding(building);

        room.setCapacity(roomDto.getCapacity());
        room.setHasProjector(roomDto.isHasProjector());
        room.setHasComputers(roomDto.isHasComputers());

        Room updatedRoom = roomRepository.save(room);
        logger.info("Room updated successfully: {}", updatedRoom.getId());
        return convertToDto(updatedRoom);
    }

    // Delete a room
    @Transactional
    public void deleteRoom(Long id) {
        logger.info("Deleting room with id: {}", id);
        if (!roomRepository.existsById(id)) {
            logger.error("Room not found with id: {}", id);
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
        logger.info("Room successfully deleted with id: {}", id);
    }

    // Find available rooms for a given date and time
    @Transactional(readOnly = true)
    @Cacheable(value = "availableRooms", key = "{#date.toString(), #startTime.toString(), #endTime.toString()}")
    public List<RoomDto> findAvailableRooms(LocalDate date, LocalTime startTime, LocalTime endTime) {
        logger.debug("Finding available rooms for date: {}, time: {}-{}", date, startTime, endTime);
        List<RoomDto> availableRooms = roomRepository.findAvailableRooms(date, startTime, endTime).parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} available rooms", availableRooms.size());
        return availableRooms;
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