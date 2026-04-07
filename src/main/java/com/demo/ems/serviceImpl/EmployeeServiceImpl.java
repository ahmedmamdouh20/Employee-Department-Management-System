package com.demo.ems.serviceImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.demo.ems.dto.EmployeeUpsertRequest;
import com.demo.ems.exception.DepartmentNotFoundException;
import com.demo.ems.exception.EmployeeNotFoundException;
import com.demo.ems.mapper.EmployeeMapper;
import com.demo.ems.util.BulkUploadRow;
import com.demo.ems.util.CsvTemplateParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.entity.Department;
import com.demo.ems.entity.Employee;
import com.demo.ems.repo.DepartmentRepository;
import com.demo.ems.repo.EmployeeRepository;
import com.demo.ems.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final Validator validator;

	public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
			Validator validator) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.validator = validator;
	}

	@Override
	public EmployeeDTO createEmployee(EmployeeUpsertRequest employeeRequest) {
		log.info("Creating employee with email={} for departmentId={}",
				employeeRequest.getEmail(), employeeRequest.getDepartmentId());
		validateEmployeeEmail(employeeRequest.getEmail(), null);

		Department department = findDepartment(employeeRequest.getDepartmentId());
		Employee manager = employeeRequest.getManagerId() == null ? null : findEmployee(employeeRequest.getManagerId());
		Employee employee = EmployeeMapper.toEntity(employeeRequest, department, manager);
		employee=employeeRepository.save(employee);
		log.info("Employee created successfully with id={} and email={}", employee.getId(), employee.getEmail());
		return EmployeeMapper.toDto(employee);
	}

	@Override
	public List<EmployeeDTO> bulkCreateEmployees(List<EmployeeUpsertRequest> employeeRequests) {
		log.info("Creating employees in bulk with requestCount={}", employeeRequests.size());
		validateBulkEmployeeRequests(employeeRequests);

		Map<Long, Department> departmentsById = preloadDepartments(employeeRequests);
		Map<Long, Employee> managersById = preloadManagers(employeeRequests);
		List<EmployeeDTO> savedEmployees = employeeRepository.saveAll(
				employeeRequests.stream()
						.map(request -> toEmployeeEntity(request, departmentsById, managersById))
						.toList())
				.stream()
				.map(EmployeeMapper::toDto)
				.toList();
		log.info("Bulk employee creation completed with createdCount={}", savedEmployees.size());
		return savedEmployees;
	}

	@Override
	public List<EmployeeDTO> bulkUploadEmployees(MultipartFile file) {
		log.info("Uploading employees from template file={}", file == null ? null : file.getOriginalFilename());
		List<BulkUploadRow<EmployeeUpsertRequest>> rows = CsvTemplateParser.parseEmployeeTemplate(file);
		for (BulkUploadRow<EmployeeUpsertRequest> row : rows) {
			try {
				validateRequest(row.value());
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Row " + row.rowNumber() + ": " + ex.getMessage(), ex);
			}
		}
		return bulkCreateEmployees(rows.stream()
				.map(BulkUploadRow::value)
				.toList());
	}

	@Override
	public EmployeeDTO getEmployeeById(Long id) {
		log.info("Fetching employee by id={}", id);
		return EmployeeMapper.toDto(findEmployee(id));
	}

	@Override
	public List<EmployeeDTO> getAllEmployees() {
		log.info("Fetching all employees");
		return employeeRepository.findAll()
				.stream()
				.map(EmployeeMapper::toDto)
				.toList();
	}

	@Override
	public Page<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId, Pageable pageable) {
		log.info("Fetching employees by departmentId={} with page={} and size={}",
				departmentId, pageable.getPageNumber(), pageable.getPageSize());
		findDepartment(departmentId);
		return employeeRepository.findAllByDepartmentId(departmentId, pageable)
				.map(EmployeeMapper::toDto);
	}

	@Override
	public EmployeeDTO updateEmployee(Long id, EmployeeUpsertRequest employeeRequest) {
		log.info("Updating employee id={} with email={}", id, employeeRequest.getEmail());
		Employee employee = findEmployee(id);
		validateEmployeeEmail(employeeRequest.getEmail(), id);

		Department department = findDepartment(employeeRequest.getDepartmentId());
		if (id.equals(employeeRequest.getManagerId())) {
			log.warn("Employee update rejected because employee id={} was assigned as their own manager", id);
			throw new IllegalArgumentException("Employee cannot be their own manager");
		}
		Employee manager = employeeRequest.getManagerId() == null ? null : findEmployee(employeeRequest.getManagerId());
		EmployeeMapper.updateEntity(employee, employeeRequest, department, manager);

		Employee updatedEmployee = employeeRepository.save(employee);
		log.info("Employee updated successfully with id={} and email={}", updatedEmployee.getId(), updatedEmployee.getEmail());
		return EmployeeMapper.toDto(updatedEmployee);
	}

	@Override
	public void deleteEmployee(Long id) {
		log.info("Deleting employee id={}", id);
		Employee employee = findEmployee(id);
		employeeRepository.delete(employee);
		log.info("Employee deleted successfully with id={}", id);
	}

	private Employee findEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Employee not found for id={}", id);
					return new EmployeeNotFoundException(id);
				});
	}

	private Department findDepartment(Long id) {
		return departmentRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Department not found for id={}", id);
					return new DepartmentNotFoundException(id);
				});
	}

	private void validateEmployeeEmail(String email, Long employeeId) {
		if (email == null || email.isBlank()) {
			log.warn("Employee email validation failed because email is blank for employeeId={}", employeeId);
			throw new IllegalArgumentException("Employee email is required");
		}

		boolean exists = employeeRepository.findByEmailIgnoreCase(email)
				.filter(existing -> !existing.getId().equals(employeeId))
				.isPresent();
		if (exists) {
			log.warn("Employee email validation failed because email={} already exists for employeeId={}",
					email, employeeId);
			throw new IllegalArgumentException("Employee email already exists");
		}
	}

	private void validateRequest(EmployeeUpsertRequest request) {
		Set<ConstraintViolation<EmployeeUpsertRequest>> violations = validator.validate(request);
		if (violations.isEmpty()) {
			return;
		}

		String message = violations.stream()
				.map(ConstraintViolation::getMessage)
				.filter(defaultMessage -> defaultMessage != null && defaultMessage.toLowerCase().contains("required"))
				.findFirst()
				.or(() -> violations.stream()
						.map(ConstraintViolation::getMessage)
						.filter(defaultMessage -> defaultMessage != null && !defaultMessage.isBlank())
						.findFirst())
				.orElse("Request validation failed");
		throw new IllegalArgumentException(message);
	}

	private void validateBulkEmployeeRequests(List<EmployeeUpsertRequest> requests) {
		Set<String> emailsInBatch = new HashSet<>();
		for (EmployeeUpsertRequest request : requests) {
			validateRequest(request);
			String normalizedEmail = request.getEmail().trim().toLowerCase();
			if (!emailsInBatch.add(normalizedEmail)) {
				throw new IllegalArgumentException("Employee email already exists");
			}
			validateEmployeeEmail(request.getEmail(), null);
		}
	}

	private Map<Long, Department> preloadDepartments(List<EmployeeUpsertRequest> requests) {
		Map<Long, Department> departmentsById = new HashMap<>();
		for (EmployeeUpsertRequest request : requests) {
			departmentsById.computeIfAbsent(request.getDepartmentId(), this::findDepartment);
		}
		return departmentsById;
	}

	private Map<Long, Employee> preloadManagers(List<EmployeeUpsertRequest> requests) {
		Map<Long, Employee> managersById = new HashMap<>();
		for (EmployeeUpsertRequest request : requests) {
			Long managerId = request.getManagerId();
			if (managerId != null) {
				managersById.computeIfAbsent(managerId, this::findEmployee);
			}
		}
		return managersById;
	}

	private Employee toEmployeeEntity(EmployeeUpsertRequest request, Map<Long, Department> departmentsById,
			Map<Long, Employee> managersById) {
		return EmployeeMapper.toEntity(
				request,
				departmentsById.get(request.getDepartmentId()),
				request.getManagerId() == null ? null : managersById.get(request.getManagerId()));
	}

}
