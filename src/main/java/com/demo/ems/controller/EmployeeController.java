package com.demo.ems.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ems.dto.EmployeeUpsertRequest;
import com.demo.ems.dto.EmployeeDTO;
import com.demo.ems.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee CRUD APIs")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@PostMapping
	@Operation(summary = "Create employee")
	@ApiResponse(responseCode = "200", description = "Employee created", content = @Content(schema = @Schema(implementation = EmployeeDTO.class)))
	public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeUpsertRequest request) {
		EmployeeDTO response = employeeService.createEmployee(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create employees")
	@ApiResponse(responseCode = "200", description = "Employees created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeDTO.class))))
	public ResponseEntity<List<EmployeeDTO>> bulkCreateEmployees(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(employeeService.bulkUploadEmployees(file));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get employee by id")
	@ApiResponse(responseCode = "200", description = "Employee found", content = @Content(schema = @Schema(implementation = EmployeeDTO.class)))
	public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
		return ResponseEntity.ok(employeeService.getEmployeeById(id));
	}

	@GetMapping
	@Operation(summary = "Get all employees")
	@ApiResponse(responseCode = "200", description = "Employees retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeDTO.class))))
	public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
		return ResponseEntity.ok(employeeService.getAllEmployees());
	}

	@GetMapping("/departments/{departmentId}")
	@Operation(summary = "Get employees by department id")
	@ApiResponse(responseCode = "200", description = "Employees retrieved by department", content = @Content(schema = @Schema(implementation = EmployeeDTO.class)))
	public ResponseEntity<Page<EmployeeDTO>> getEmployeesByDepartmentId(@PathVariable Long departmentId,
			Pageable pageable) {
		return ResponseEntity.ok(employeeService.getEmployeesByDepartmentId(departmentId, pageable));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update employee")
	@ApiResponse(responseCode = "200", description = "Employee updated", content = @Content(schema = @Schema(implementation = EmployeeDTO.class)))
	public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id,
			@Valid @RequestBody EmployeeUpsertRequest request) {
		return ResponseEntity.ok(employeeService.updateEmployee(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete employee")
	@ApiResponse(responseCode = "204", description = "Employee deleted")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.noContent().build();
	}
}
