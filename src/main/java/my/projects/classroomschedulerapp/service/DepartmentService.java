package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.DepartmentDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Get all departments
    public List<DepartmentDto> getAllDepartments() {
        logger.debug("Fetching all departments");
        List<DepartmentDto> departments = departmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Found {} departments", departments.size());
        return departments;
    }

    // Get department by ID
    public DepartmentDto getDepartmentById(Long id) {
        logger.debug("Fetching department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Department not found with id: {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });
        logger.debug("Found department: {}", department.getName());
        return convertToDto(department);
    }

    // Create a new department
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        logger.info("Creating new department: {}", departmentDto.getName());
        Department department = convertToEntity(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department created successfully with id: {}", savedDepartment.getId());
        return convertToDto(savedDepartment);
    }

    // Update an existing department
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        logger.info("Updating department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Department not found with id: {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });

        logger.debug("Found department to update: {}", department.getName());
        department.setName(departmentDto.getName());
        Department updatedDepartment = departmentRepository.save(department);
        logger.info("Department updated successfully with id: {}", updatedDepartment.getId());
        return convertToDto(updatedDepartment);
    }

    // Delete a department
    public void deleteDepartment(Long id) {
        logger.info("Deleting department with id: {}", id);
        if (!departmentRepository.existsById(id)) {
            logger.error("Department not found with id: {}", id);
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
        logger.info("Department successfully deleted with id: {}", id);
    }

    // Convert Department entity to DepartmentDto
    private DepartmentDto convertToDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getName()
        );
    }

    // Convert DepartmentDto to Department entity
    private Department convertToEntity(DepartmentDto departmentDto) {
        Department department = new Department();
        department.setName(departmentDto.getName());
        return department;
    }
}
