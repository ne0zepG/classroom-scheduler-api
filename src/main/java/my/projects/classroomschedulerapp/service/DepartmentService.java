package my.projects.classroomschedulerapp.service;

import my.projects.classroomschedulerapp.dto.DepartmentDto;
import my.projects.classroomschedulerapp.exception.ResourceNotFoundException;
import my.projects.classroomschedulerapp.model.Department;
import my.projects.classroomschedulerapp.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Get all departments
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get department by ID
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return convertToDto(department);
    }

    // Create a new department
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = convertToEntity(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        return convertToDto(savedDepartment);
    }

    // Update an existing department
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        department.setName(departmentDto.getName());
        Department updatedDepartment = departmentRepository.save(department);
        return convertToDto(updatedDepartment);
    }

    // Delete a department
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
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
