package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.ProgramDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.model.Program;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import my.projects.classroomschedulerapp.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;

    public ProgramService(ProgramRepository programRepository, DepartmentRepository departmentRepository) {
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
    }

    // Get all programs
    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get program by ID
    public ProgramDto getProgramById(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));
        return convertToDto(program);
    }

    // Get programs by department ID
    public List<ProgramDto> getProgramsByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return programRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Create a new program
    public ProgramDto createProgram(ProgramDto programDto) {
        Program program = convertToEntity(programDto);
        Program savedProgram = programRepository.save(program);
        return convertToDto(savedProgram);
    }

    // Update an existing program
    public ProgramDto updateProgram(Long id, ProgramDto programDto) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));

        program.setName(programDto.getName());
        program.setCode(programDto.getCode());

        // Update department if provided
        if (programDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(programDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + programDto.getDepartmentId()));
            program.setDepartment(department);
        }

        Program updatedProgram = programRepository.save(program);
        return convertToDto(updatedProgram);
    }

    // Delete a program
    public void deleteProgram(Long id) {
        if (!programRepository.existsById(id)) {
            throw new ResourceNotFoundException("Program not found with id: " + id);
        }
        programRepository.deleteById(id);
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
