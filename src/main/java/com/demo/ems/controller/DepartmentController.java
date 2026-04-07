package com.demo.ems.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.dto.DepartmentDTO;
import com.demo.ems.service.DepartmentService;

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
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Department CRUD APIs")
public class DepartmentController {

	private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@PostMapping
	@Operation(summary = "Create new department")
	@ApiResponse(responseCode = "200", description = "Department created", content = @Content(schema = @Schema(implementation = DepartmentDTO.class)))
	public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentUpsertRequest request) {
		DepartmentDTO response = departmentService.createDepartment(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create departments")
	@ApiResponse(responseCode = "200", description = "Departments created", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.class))))
	public ResponseEntity<List<DepartmentDTO>> bulkCreateDepartments(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(departmentService.bulkUploadDepartments(file));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get department by id")
	@ApiResponse(responseCode = "200", description = "Department found", content = @Content(schema = @Schema(implementation = DepartmentDTO.class)))
	public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
		return ResponseEntity.ok(departmentService.getDepartmentById(id));
	}

	@GetMapping
	@Operation(summary = "Get all departments")
	@ApiResponse(responseCode = "200", description = "Departments retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.class))))
	public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
		return ResponseEntity.ok(departmentService.getAllDepartments());
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update department")
	@ApiResponse(responseCode = "200", description = "Department updated", content = @Content(schema = @Schema(implementation = DepartmentDTO.class)))
	public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id,
			@Valid @RequestBody DepartmentUpsertRequest request) {
		return ResponseEntity.ok(departmentService.updateDepartment(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete department")
	@ApiResponse(responseCode = "204", description = "Department deleted")
	public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
		departmentService.deleteDepartment(id);
		return ResponseEntity.noContent().build();
	}
}
