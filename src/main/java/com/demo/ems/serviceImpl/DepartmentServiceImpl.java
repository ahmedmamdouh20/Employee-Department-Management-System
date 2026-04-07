package com.demo.ems.serviceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.exception.DepartmentNotFoundException;
import com.demo.ems.mapper.DepartmentMapper;
import com.demo.ems.util.BulkUploadRow;
import com.demo.ems.util.CsvTemplateParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.demo.ems.dto.DepartmentDTO;
import com.demo.ems.entity.Department;
import com.demo.ems.repo.DepartmentRepository;
import com.demo.ems.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final Validator validator;

	public DepartmentServiceImpl(DepartmentRepository departmentRepository, Validator validator) {
		this.departmentRepository = departmentRepository;
		this.validator = validator;
	}

	@Override
	public DepartmentDTO createDepartment(DepartmentUpsertRequest departmentRequest) {
		log.info("Creating department with name={}", departmentRequest.getName());
		validateDepartmentName(departmentRequest.getName(), null);

		Department department = DepartmentMapper.toEntity(departmentRequest);

		department=departmentRepository.save(department);
		log.info("Department created successfully with id={} and name={}", department.getId(), department.getName());

		return DepartmentMapper.toDto(department);
	}

	@Override
	public List<DepartmentDTO> bulkCreateDepartments(List<DepartmentUpsertRequest> departmentRequests) {
		log.info("Creating departments in bulk with requestCount={}", departmentRequests.size());
		validateBulkDepartmentRequests(departmentRequests);

		List<DepartmentDTO> savedDepartments = departmentRepository.saveAll(
				departmentRequests.stream()
						.map(DepartmentMapper::toEntity)
						.toList())
				.stream()
				.map(DepartmentMapper::toDto)
				.toList();
		log.info("Bulk department creation completed with createdCount={}", savedDepartments.size());
		return savedDepartments;
	}

	@Override
	public List<DepartmentDTO> bulkUploadDepartments(MultipartFile file) {
		log.info("Uploading departments from template file={}", file == null ? null : file.getOriginalFilename());
		List<BulkUploadRow<DepartmentUpsertRequest>> rows = CsvTemplateParser.parseDepartmentTemplate(file);
		for (BulkUploadRow<DepartmentUpsertRequest> row : rows) {
			try {
				validateRequest(row.value());
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Row " + row.rowNumber() + ": " + ex.getMessage(), ex);
			}
		}
		return bulkCreateDepartments(rows.stream()
				.map(BulkUploadRow::value)
				.toList());
	}

	@Override
	public DepartmentDTO getDepartmentById(Long id) {
		log.info("Fetching department by id={}", id);
		return DepartmentMapper.toDto(findDepartment(id));
	}

	@Override
	public List<DepartmentDTO> getAllDepartments() {
		log.info("Fetching all departments");
		return departmentRepository.findAll()
				.stream()
				.map(DepartmentMapper::toDto)
				.toList();
	}

	@Override
	public DepartmentDTO updateDepartment(Long id, DepartmentUpsertRequest departmentRequest) {
		log.info("Updating department id={} with name={}", id, departmentRequest.getName());
		Department department = findDepartment(id);
		validateDepartmentName(departmentRequest.getName(), id);

		BeanUtils.copyProperties(departmentRequest, department);
		if (departmentRequest.getStatus() == null) {
			department.setStatus("ACTIVE");
		}

		department=departmentRepository.save(department);
		log.info("Department updated successfully with id={} and name={}", department.getId(), department.getName());

		return DepartmentMapper.toDto(department);
	}

	@Override
	public void deleteDepartment(Long id) {
		log.info("Deleting department id={}", id);
		Department department = findDepartment(id);
		long employeesCount = departmentRepository.countEmployeesByDepartmentId(id);
		if (employeesCount > 0) {
			log.warn("Department deletion rejected for id={} because {} employees are assigned", id, employeesCount);
			throw new IllegalArgumentException("Department cannot be deleted while employees are assigned");
		}
		departmentRepository.delete(department);
		log.info("Department deleted successfully with id={}", id);
	}

	private Department findDepartment(Long id) {
		return departmentRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Department not found for id={}", id);
					return new DepartmentNotFoundException(id);
				});
	}

	private void validateDepartmentName(String name, Long id) {
		if (name == null || name.isBlank()) {
			log.warn("Department name validation failed because name is blank for departmentId={}", id);
			throw new IllegalArgumentException("Department name is required");
		}

		boolean exists = departmentRepository.findByNameIgnoreCase(name)
				.filter(existing ->  !existing.getId().equals(id))
				.isPresent();
		if (exists) {
			log.warn("Department name validation failed because name={} already exists for departmentId={}", name, id);
			throw new IllegalArgumentException("Department name already exists");
		}
	}

	private void validateRequest(DepartmentUpsertRequest request) {
		Set<ConstraintViolation<DepartmentUpsertRequest>> violations = validator.validate(request);
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

	private void validateBulkDepartmentRequests(List<DepartmentUpsertRequest> requests) {
		Set<String> namesInBatch = new HashSet<>();
		for (DepartmentUpsertRequest request : requests) {
			validateRequest(request);
			String normalizedName = request.getName().trim().toLowerCase();
			if (!namesInBatch.add(normalizedName)) {
				throw new IllegalArgumentException("Department name already exists");
			}
			validateDepartmentName(request.getName(), null);
		}
	}

}
