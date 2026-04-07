package com.demo.ems.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.ems.dto.EmployeeUpsertRequest;
import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@Test
	void createEmployeeReturnsCreated() throws Exception {
		EmployeeUpsertRequest request = employeeRequest();
		given(employeeService.createEmployee(any(EmployeeUpsertRequest.class))).willReturn(employeeDto(5L));

		mockMvc.perform(post("/api/employees")
				.contentType(APPLICATION_JSON)
				.content(employeeRequestJson()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.email").value("ahmed@example.com"));
	}

	@Test
	void bulkCreateEmployees() throws Exception {
		given(employeeService.bulkUploadEmployees(any()))
				.willReturn(List.of(employeeDto(5L), employeeDto(6L)));

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"employee-bulk-template.csv",
				"text/csv",
				("""
					firstName,lastName,email,phoneNumber,hireDate,salary,position,departmentId,status,managerId
					Ahmed,Mamdouh,ahmed@example.com,+201001234567,2026-04-01,15000.00,Backend Engineer,1,ACTIVE,2
					Sara,Ali,sara@example.com,+201001234568,2026-04-02,16000.00,QA Engineer,1,ACTIVE,2
					""").getBytes());

		mockMvc.perform(multipart("/api/employees/bulk").file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].email").value("ahmed@example.com"))
				.andExpect(jsonPath("$[1].email").value("ahmed@example.com"));
	}

	@Test
	void getEmployeeById() throws Exception {
		given(employeeService.getEmployeeById(5L)).willReturn(employeeDto(5L));

		mockMvc.perform(get("/api/employees/5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.firstName").value("Ahmed"));
	}

	@Test
	void getAllEmployeesList() throws Exception {
		given(employeeService.getAllEmployees()).willReturn(List.of(employeeDto(5L), employeeDto(6L)));

		mockMvc.perform(get("/api/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].email").value("ahmed@example.com"));
	}

	@Test
	void getEmployeesByDepartmentId() throws Exception {
		given(employeeService.getEmployeesByDepartmentId(eq(1L), any()))
				.willReturn(new PageImpl<>(List.of(employeeDto(5L), employeeDto(6L)), PageRequest.of(0, 2), 2));

		mockMvc.perform(get("/api/employees/departments/1?page=0&size=2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.content[0].email").value("ahmed@example.com"))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void updateEmployee() throws Exception {
		EmployeeUpsertRequest request = employeeRequest();
		given(employeeService.updateEmployee(eq(5L), any(EmployeeUpsertRequest.class))).willReturn(employeeDto(5L));

		mockMvc.perform(put("/api/employees/5")
				.contentType(APPLICATION_JSON)
				.content(employeeRequestJson()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.departmentName").value("Engineering"));
	}

	@Test
	void deleteEmployee() throws Exception {
		mockMvc.perform(delete("/api/employees/5"))
				.andExpect(status().isNoContent());

		then(employeeService).should().deleteEmployee(5L);
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
		request.setManagerId(2L);
		return request;
	}

	private EmployeeDTO employeeDto(Long id) {
		EmployeeDTO dto = new EmployeeDTO();
		dto.setId(id);
		dto.setFirstName("Ahmed");
		dto.setLastName("Mamdouh");
		dto.setEmail("ahmed@example.com");
		dto.setPhoneNumber("+201001234567");
		dto.setHireDate(LocalDate.parse("2026-04-01"));
		dto.setSalary(new BigDecimal("15000.00"));
		dto.setPosition("Backend Engineer");
		dto.setStatus("ACTIVE");
		dto.setDepartmentName("Engineering");
		dto.setManagerEmail("manager@example.com");
		dto.setCreatedAt(LocalDateTime.parse("2026-04-06T10:15:30"));
		dto.setUpdatedAt(LocalDateTime.parse("2026-04-06T10:15:30"));
		return dto;
	}

	private String employeeRequestJson() {
		return """
			{
			  "firstName": "Ahmed",
			  "lastName": "Mamdouh",
			  "email": "ahmed@example.com",
			  "phoneNumber": "+201001234567",
			  "hireDate": "2026-04-01",
			  "salary": 15000.00,
			  "position": "Backend Engineer",
			  "departmentId": 1,
			  "managerId": 2
			}
			""";
	}
}
