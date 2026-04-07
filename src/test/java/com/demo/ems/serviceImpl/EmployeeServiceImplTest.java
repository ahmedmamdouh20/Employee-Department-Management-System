package com.demo.ems.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.dto.EmployeeUpsertRequest;
import com.demo.ems.entity.Department;
import com.demo.ems.entity.Employee;
import com.demo.ems.exception.EmployeeNotFoundException;
import com.demo.ems.repo.DepartmentRepository;
import com.demo.ems.repo.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	private EmployeeServiceImpl employeeService;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		employeeService = new EmployeeServiceImpl(employeeRepository, departmentRepository, validator);
	}

	@Test
	void createEmployeeIsOk() {
		Department department = department(1L, "Engineering");
		EmployeeUpsertRequest request = employeeRequest();

		Employee savedEmployee = employee(5L, "ahmed@example.com", department, null);

		when(employeeRepository.findByEmailIgnoreCase("ahmed@example.com")).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
		when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

		EmployeeDTO result = employeeService.createEmployee(request);

		verify(employeeRepository).save(any(Employee.class));
		assertEquals(5L, result.getId());
		assertEquals("Ahmed", result.getFirstName());
		assertEquals("ACTIVE", result.getStatus());
		assertEquals("Engineering", result.getDepartmentName());
		assertNull(result.getManagerEmail());
	}
	@Test
	void createEmployeeThrowsWhenEmailAlreadyExists() {
		Employee existingEmployee = employee(9L, "ahmed@example.com", null, null);
		EmployeeUpsertRequest request = employeeRequest();

		when(employeeRepository.findByEmailIgnoreCase("ahmed@example.com")).thenReturn(Optional.of(existingEmployee));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> employeeService.createEmployee(request));

		assertEquals("Employee email already exists", exception.getMessage());
		verify(employeeRepository, never()).save(any(Employee.class));
	}

	@Test
	void getEmployeeByIdThrowsWhenEmployeeDoesNotExist() {
		when(employeeRepository.findById(10L)).thenReturn(Optional.empty());

		EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class,
				() -> employeeService.getEmployeeById(10L));

		assertEquals("Employee not found with id 10", exception.getMessage());
	}

	@Test
	void updateEmployeeThrowsWhenEmployeeIsOwnManager() {
		Department department = department(1L, "Engineering");
		Employee existingEmployee = employee(5L, "ahmed@example.com", department, null);
		EmployeeUpsertRequest request = employeeRequest();
		request.setManagerId(5L);

		when(employeeRepository.findById(5L)).thenReturn(Optional.of(existingEmployee));
		when(employeeRepository.findByEmailIgnoreCase("ahmed@example.com")).thenReturn(Optional.of(existingEmployee));
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> employeeService.updateEmployee(5L, request));

		assertEquals("Employee cannot be their own manager", exception.getMessage());
		verify(employeeRepository, never()).save(any(Employee.class));
	}

	@Test
	void updateEmployeeUpdatesExistingEmployee() {
		Department department = department(1L, "Engineering");
		Department product = department(2L, "Product");
		Employee manager = employee(2L, "manager@example.com", department, null);
		Employee existingEmployee = employee(5L, "ahmed@example.com", department, null);

		EmployeeUpsertRequest request = employeeRequest();
		request.setEmail("ahmed.updated@example.com");
		request.setDepartmentId(2L);
		request.setManagerId(2L);
		request.setStatus("INACTIVE");

		Employee savedEmployee = employee(5L, "ahmed.updated@example.com", product, manager);
		savedEmployee.setStatus("INACTIVE");

		when(employeeRepository.findById(5L)).thenReturn(Optional.of(existingEmployee));
		when(employeeRepository.findByEmailIgnoreCase("ahmed.updated@example.com")).thenReturn(Optional.empty());
		when(departmentRepository.findById(2L)).thenReturn(Optional.of(product));
		when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
		when(employeeRepository.save(existingEmployee)).thenReturn(savedEmployee);

		EmployeeDTO result = employeeService.updateEmployee(5L, request);

		assertEquals("ahmed.updated@example.com", existingEmployee.getEmail());
		assertEquals(product, existingEmployee.getDepartment());
		assertEquals(manager, existingEmployee.getManager());
		assertEquals("INACTIVE", existingEmployee.getStatus());
		assertEquals("Product", result.getDepartmentName());
		assertEquals("manager@example.com", result.getManagerEmail());
	}

	@Test
	void deleteEmployeeDeletesExistingEmployee() {
		Employee employee = employee(5L, "ahmed@example.com", null, null);
		when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));

		employeeService.deleteEmployee(5L);

		verify(employeeRepository).delete(employee);
	}

	@Test
	void getAllEmployeesReturnsMappedDtos() {
		Department engineering = department(1L, "Engineering");
		Employee manager = employee(2L, "manager@example.com", engineering, null);
		Employee employee = employee(5L, "ahmed@example.com", engineering, manager);

		when(employeeRepository.findAll()).thenReturn(List.of(employee));

		List<EmployeeDTO> result = employeeService.getAllEmployees();

		assertEquals(1, result.size());
		assertEquals("Engineering", result.getFirst().getDepartmentName());
		assertEquals("manager@example.com", result.getFirst().getManagerEmail());
	}

	private EmployeeUpsertRequest employeeRequest() {
		EmployeeUpsertRequest request = new EmployeeUpsertRequest();
		request.setFirstName("Ahmed");
		request.setLastName("Mamdouh");
		request.setEmail("ahmed@example.com");
		request.setPhoneNumber("+201001234567");
		request.setHireDate(LocalDate.parse("2026-04-01"));
		request.setSalary(new BigDecimal("15000.00"));
		request.setPosition("Backend Engineer");
		request.setDepartmentId(1L);
		request.setManagerId(null);
		return request;
	}

	private Department department(Long id, String name) {
		Department department = new Department();
		department.setId(id);
		department.setName(name);
		department.setStatus("ACTIVE");
		return department;
	}

	private Employee employee(Long id, String email, Department department, Employee manager) {
		Employee employee = new Employee();
		employee.setId(id);
		employee.setFirstName("Ahmed");
		employee.setLastName("Mamdouh");
		employee.setEmail(email);
		employee.setPhoneNumber("+201001234567");
		employee.setHireDate(LocalDate.parse("2026-04-01"));
		employee.setSalary(new BigDecimal("15000.00"));
		employee.setPosition("Backend Engineer");
		employee.setStatus("ACTIVE");
		employee.setDepartment(department);
		employee.setManager(manager);
		return employee;
	}
}
