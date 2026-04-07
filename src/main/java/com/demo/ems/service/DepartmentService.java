package com.demo.ems.service;

import java.util.List;

import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.dto.DepartmentDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DepartmentService {

	DepartmentDTO createDepartment(DepartmentUpsertRequest departmentCreateRequest);

	List<DepartmentDTO> bulkCreateDepartments(List<DepartmentUpsertRequest> departmentCreateRequests);

	List<DepartmentDTO> bulkUploadDepartments(MultipartFile file);

	DepartmentDTO getDepartmentById(Long id);

	List<DepartmentDTO> getAllDepartments();

	DepartmentDTO updateDepartment(Long id,  DepartmentUpsertRequest departmentCreateRequest);

	void deleteDepartment(Long id);
}
