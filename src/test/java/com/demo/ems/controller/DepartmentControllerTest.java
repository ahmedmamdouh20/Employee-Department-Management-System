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

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.dto.DepartmentDTO;
import com.demo.ems.service.DepartmentService;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DepartmentService departmentService;

	@Test
	void createDepartmentReturnsCreated() throws Exception {
		DepartmentUpsertRequest request = new DepartmentUpsertRequest();
		request.setName("Engineering");
		request.setDescription("Platform");

		DepartmentDTO response = departmentDto(1L, "Engineering");
		given(departmentService.createDepartment(any(DepartmentUpsertRequest.class))).willReturn(response);

		mockMvc.perform(post("/api/departments")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "Engineering",
					  "description": "Platform"
					}
					"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Engineering"));
	}

	@Test
	void bulkCreateDepartments() throws Exception {
		given(departmentService.bulkUploadDepartments(any()))
				.willReturn(List.of(
						departmentDto(1L, "Engineering"),
						departmentDto(2L, "HR")));

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"department-bulk-template.csv",
				"text/csv",
				("""
					name,description,status
					Engineering,Platform,ACTIVE
					HR,People,ACTIVE
					""").getBytes());

		mockMvc.perform(multipart("/api/departments/bulk").file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Engineering"))
				.andExpect(jsonPath("$[1].name").value("HR"));
	}

	@Test
	void getDepartmentById() throws Exception {
		given(departmentService.getDepartmentById(1L)).willReturn(departmentDto(1L, "Engineering"));

		mockMvc.perform(get("/api/departments/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Engineering"));
	}

	@Test
	void getAllDepartments() throws Exception {
		given(departmentService.getAllDepartments()).willReturn(List.of(
				departmentDto(1L, "Engineering"),
				departmentDto(2L, "HR")));

		mockMvc.perform(get("/api/departments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Engineering"))
				.andExpect(jsonPath("$[1].name").value("HR"));
	}

	@Test
	void updateDepartment() throws Exception {
		DepartmentUpsertRequest request = new DepartmentUpsertRequest();
		request.setName("Engineering");
		request.setDescription("Updated");

		given(departmentService.updateDepartment(eq(1L), any(DepartmentUpsertRequest.class)))
				.willReturn(departmentDto(1L, "Engineering"));

		mockMvc.perform(put("/api/departments/1")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "Engineering",
					  "description": "Updated"
					}
					"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Engineering"));
	}

	@Test
	void createDepartmentThrowBadRequest() throws Exception {
		given(departmentService.createDepartment(any(DepartmentUpsertRequest.class)))
				.willThrow(new IllegalArgumentException("Department name already exists"));

		mockMvc.perform(post("/api/departments")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "Engineering",
					  "description": "Platform"
					}
					"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Department name already exists"));
	}

	@Test
	void deleteDepartmentReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/departments/1"))
				.andExpect(status().isNoContent());

		then(departmentService).should().deleteDepartment(1L);
	}

	private DepartmentDTO departmentDto(Long id, String name) {
		DepartmentDTO dto = new DepartmentDTO();
		dto.setId(id);
		dto.setName(name);
		dto.setDescription("Description");
		dto.setStatus("ACTIVE");
		dto.setCreatedAt(LocalDateTime.parse("2026-04-06T10:15:30"));
		dto.setUpdatedAt(LocalDateTime.parse("2026-04-06T10:15:30"));
		return dto;
	}
}
