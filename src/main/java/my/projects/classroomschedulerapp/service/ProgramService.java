package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.ProgramDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.model.Program;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import my.projects.classroomschedulerapp.repository.ProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProgramService {

    private static final Logger logger = LoggerFactory.getLogger(ProgramService.class);

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;

    public ProgramService(ProgramRepository programRepository, DepartmentRepository departmentRepository) {
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
    }

    // Asynchronous method to get all programs
    @Async("taskExecutor")
    public CompletableFuture<List<ProgramDto>> getAllProgramsAsync() {
        logger.debug("Asynchronously fetching all programs");
        List<ProgramDto> programs = getAllPrograms();
        return CompletableFuture.completedFuture(programs);
    }

    // Asynchronous method to get programs by department
    @Async("taskExecutor")
    public CompletableFuture<List<ProgramDto>> getProgramsByDepartmentAsync(Long departmentId) {
        logger.debug("Asynchronously fetching programs for department id: {}", departmentId);
        List<ProgramDto> programs = getProgramsByDepartment(departmentId);
        return CompletableFuture.completedFuture(programs);
    }

    @Async("taskExecutor")
    public CompletableFuture<ProgramDto> getProgramByIdAsync(Long id) {
        logger.debug("Asynchronously fetching program with id: {}", id);
        ProgramDto program = getProgramById(id);
        return CompletableFuture.completedFuture(program);
    }

    // Get all programs
    @Transactional(readOnly = true)
    public List<ProgramDto> getAllPrograms() {
        logger.debug("Fetching all programs");
        List<ProgramDto> programs = programRepository.findAll().parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} programs", programs.size());
        return programs;
    }

    // Get program by ID
    @Transactional(readOnly = true)
    @Cacheable(value = "programDetails", key = "#id")
    public ProgramDto getProgramById(Long id) {
        logger.debug("Fetching program with id: {}", id);
        Program program = programRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Program not found with id: {}", id);
                    return new ResourceNotFoundException("Program not found with id: " + id);
                });
        logger.debug("Found program: {}", program.getName());
        return convertToDto(program);
    }

    // Get programs by department ID
    @Transactional(readOnly = true)
    @Cacheable(value = "programsByDepartment", key = "#departmentId")
    public List<ProgramDto> getProgramsByDepartment(Long departmentId) {
        logger.debug("Fetching programs for department id: {}", departmentId);

        if (!departmentRepository.existsById(departmentId)) {
            logger.error("Department not found with id: {}", departmentId);
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }

        List<ProgramDto> programs = programRepository.findByDepartmentId(departmentId).parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        logger.debug("Found {} programs for department id: {}", programs.size(), departmentId);
        return programs;
    }

    // Create a new program
    @Transactional
    public ProgramDto createProgram(ProgramDto programDto) {
        logger.info("Creating new program: {}", programDto.getName());

        try {
            Program program = convertToEntity(programDto);
            Program savedProgram = programRepository.save(program);
            logger.info("Program created successfully with id: {}", savedProgram.getId());
            return convertToDto(savedProgram);
        } catch (ResourceNotFoundException e) {
            logger.error("Failed to create program: {}", e.getMessage());
            throw e;
        }
    }

    // Update an existing program
    @Transactional
    public ProgramDto updateProgram(Long id, ProgramDto programDto) {
        logger.info("Updating program with id: {}", id);

        Program program = programRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Program not found with id: {}", id);
                    return new ResourceNotFoundException("Program not found with id: " + id);
                });

        logger.debug("Found program to update: {}", program.getName());
        program.setName(programDto.getName());
        program.setCode(programDto.getCode());

        // Update department if provided
        if (programDto.getDepartmentId() != null) {
            logger.debug("Updating program department to id: {}", programDto.getDepartmentId());
            Department department = departmentRepository.findById(programDto.getDepartmentId())
                    .orElseThrow(() -> {
                        logger.error("Department not found with id: {}", programDto.getDepartmentId());
                        return new ResourceNotFoundException("Department not found with id: " + programDto.getDepartmentId());
                    });
            program.setDepartment(department);
        }

        Program updatedProgram = programRepository.save(program);
        logger.info("Program updated successfully: {}", updatedProgram.getId());
        return convertToDto(updatedProgram);
    }

    // Delete a program
    @Transactional
    public void deleteProgram(Long id) {
        logger.info("Deleting program with id: {}", id);

        if (!programRepository.existsById(id)) {
            logger.error("Program not found with id: {}", id);
            throw new ResourceNotFoundException("Program not found with id: " + id);
        }

        programRepository.deleteById(id);
        logger.info("Program successfully deleted with id: {}", id);
    }

    // Convert Program entity to ProgramDto
    private ProgramDto convertToDto(Program program) {
        return new ProgramDto(
                program.getId(),
                program.getName(),
                program.getCode(),
                program.getDepartment().getId(),
                program.getDepartment().getName()
        );
    }

    // Convert ProgramDto to Program entity
    private Program convertToEntity(ProgramDto programDto) {
        Program program = new Program();
        program.setName(programDto.getName());
        program.setCode(programDto.getCode());

        // Set department
        Department department = departmentRepository.findById(programDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + programDto.getDepartmentId()));
        program.setDepartment(department);

        return program;
    }


}
