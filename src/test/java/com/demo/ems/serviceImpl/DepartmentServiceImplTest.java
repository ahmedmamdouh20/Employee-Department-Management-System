package com.demo.ems.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.demo.ems.dto.DepartmentDTO;
import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.entity.Department;
import com.demo.ems.exception.DepartmentNotFoundException;
import com.demo.ems.repo.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

	@Mock
	private DepartmentRepository departmentRepository;

	private DepartmentServiceImpl departmentService;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		departmentService = new DepartmentServiceImpl(departmentRepository, validator);
	}

	@Test
	void createDepartment() {
		DepartmentUpsertRequest request = new DepartmentUpsertRequest();
		request.setName("Engineering");
		request.setDescription("Platform");

		Department savedDepartment = new Department();
		savedDepartment.setId(1L);
		savedDepartment.setName("Engineering");
		savedDepartment.setDescription("Platform");
		savedDepartment.setStatus("ACTIVE");

		when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.empty());
		when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartment);

		DepartmentDTO result = departmentService.createDepartment(request);

		verify(departmentRepository).save(any(Department.class));
		assertEquals(1L, result.getId());
		assertEquals("Engineering", result.getName());
		assertEquals("ACTIVE", result.getStatus());
		assertEquals("Platform", result.getDescription());
	}

	@Test
	void createDepartmentThrowsWhenNameAlreadyExists() {
		Department existingDepartment = new Department();
		existingDepartment.setId(99L);
		existingDepartment.setName("Engineering");

		DepartmentUpsertRequest request = new DepartmentUpsertRequest();
		request.setName("Engineering");

		when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(existingDepartment));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> departmentService.createDepartment(request));

		assertEquals("Department name already exists", exception.getMessage());
		verify(departmentRepository, never()).save(any(Department.class));
	}

	@Test
	void getDepartmentByIdThrowsWhenDepartmentDoesNotExist() {
		when(departmentRepository.findById(10L)).thenReturn(Optional.empty());

		DepartmentNotFoundException exception = assertThrows(DepartmentNotFoundException.class,
				() -> departmentService.getDepartmentById(10L));

		assertEquals("Department not found with id 10", exception.getMessage());
	}

	@Test
	void updateDepartmentUpdatesExistingDepartment() {
		Department existingDepartment = new Department();
		existingDepartment.setId(5L);
		existingDepartment.setName("Engineering");
		existingDepartment.setStatus("ACTIVE");

		DepartmentUpsertRequest request = new DepartmentUpsertRequest();
		request.setName("Product");
		request.setDescription("Product team");
		request.setStatus("INACTIVE");

		Department savedDepartment = new Department();
		savedDepartment.setId(5L);
		savedDepartment.setName("Product");
		savedDepartment.setDescription("Product team");
		savedDepartment.setStatus("INACTIVE");

		when(departmentRepository.findById(5L)).thenReturn(Optional.of(existingDepartment));
		when(departmentRepository.findByNameIgnoreCase("Product")).thenReturn(Optional.empty());
		when(departmentRepository.save(existingDepartment)).thenReturn(savedDepartment);

		DepartmentDTO result = departmentService.updateDepartment(5L, request);

		assertEquals("Product", existingDepartment.getName());
		assertEquals("Product team", existingDepartment.getDescription());
		assertEquals("INACTIVE", existingDepartment.getStatus());
		assertEquals(5L, result.getId());
		assertEquals("Product", result.getName());
	}

	@Test
	void deleteDepartmentThrowsWhenEmployeesAreAssigned() {
		Department department = new Department();
		department.setId(3L);
		department.setName("HR");

		when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));
		when(departmentRepository.countEmployeesByDepartmentId(3L)).thenReturn(2L);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> departmentService.deleteDepartment(3L));

		assertEquals("Department cannot be deleted while employees are assigned", exception.getMessage());
		verify(departmentRepository, never()).delete(any(Department.class));
	}

	@Test
	void getAllDepartmentsReturnsMappedDtos() {
		Department engineering = new Department();
		engineering.setId(1L);
		engineering.setName("Engineering");
		engineering.setStatus("ACTIVE");

		Department hr = new Department();
		hr.setId(2L);
		hr.setName("HR");
		hr.setStatus("INACTIVE");

		when(departmentRepository.findAll()).thenReturn(List.of(engineering, hr));

		List<DepartmentDTO> result = departmentService.getAllDepartments();

		assertEquals(2, result.size());
		assertEquals("Engineering", result.getFirst().getName());
		assertEquals("HR", result.get(1).getName());
		assertTrue(result.stream().anyMatch(dto -> "INACTIVE".equals(dto.getStatus())));
	}

}
