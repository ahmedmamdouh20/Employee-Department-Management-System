package com.demo.ems.mapper;

import com.demo.ems.dto.DepartmentDTO;
import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.entity.Department;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

@UtilityClass
public class DepartmentMapper {

	public static DepartmentDTO toDto(Department department) {
		DepartmentDTO departmentDTO = new DepartmentDTO();
		BeanUtils.copyProperties(department, departmentDTO);
		return departmentDTO;
	}

	public static Department toEntity(DepartmentUpsertRequest request) {
		Department department = new Department();
		BeanUtils.copyProperties(request, department);
		if (request.getStatus() == null) {
			department.setStatus("ACTIVE");
		}
		return department;
	}
}
