package com.demo.ems.mapper;

import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.dto.EmployeeUpsertRequest;
import com.demo.ems.entity.Department;
import com.demo.ems.entity.Employee;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

@UtilityClass
public class EmployeeMapper {

	public static EmployeeDTO toDto(Employee employee) {
		EmployeeDTO employeeDTO = new EmployeeDTO();
		BeanUtils.copyProperties(employee, employeeDTO);
		employeeDTO.setDepartmentName(employee.getDepartment() == null ? null : employee.getDepartment().getName());
		employeeDTO.setManagerEmail(employee.getManager() == null ? null : employee.getManager().getEmail());
		return employeeDTO;
	}

	public static Employee toEntity(EmployeeUpsertRequest request, Department department, Employee manager) {
		Employee employee = new Employee();
		apply(employee, request, department, manager);
		return employee;
	}

	public static void updateEntity(Employee employee, EmployeeUpsertRequest request, Department department,
			Employee manager) {
		apply(employee, request, department, manager);
	}

	private static void apply(Employee employee, EmployeeUpsertRequest request, Department department, Employee manager) {
		BeanUtils.copyProperties(request, employee);
		employee.setStatus(request.getStatus() == null || request.getStatus().isBlank()
				? "ACTIVE"
				: request.getStatus());
		employee.setDepartment(department);
		employee.setManager(manager);
	}
}
