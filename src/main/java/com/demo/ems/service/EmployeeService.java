package com.demo.ems.service;

import java.util.List;

import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.dto.EmployeeUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

	EmployeeDTO createEmployee(EmployeeUpsertRequest employeeUpsertRequest);

	List<EmployeeDTO> bulkCreateEmployees(List<EmployeeUpsertRequest> employeeUpsertRequests);

	List<EmployeeDTO> bulkUploadEmployees(MultipartFile file);

	EmployeeDTO getEmployeeById(Long id);

	List<EmployeeDTO> getAllEmployees();

	Page<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId, Pageable pageable);

	EmployeeDTO updateEmployee(Long id, EmployeeUpsertRequest employeeUpsertRequest);

	void deleteEmployee(Long id);
}
