package com.demo.ems.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "DepartmentDTO", description = "Department response payload")
public class DepartmentDTO {

	@Schema(description = "Department identifier", example = "1")
	private Long id;

	@Schema(description = "Department name", example = "Engineering")
	private String name;

	@Schema(description = "Department description", example = "Builds and maintains the platform")
	private String description;

	@Schema(description = "Department status", example = "ACTIVE")
	private String status;

	@Schema(description = "Department creation timestamp", example = "2026-04-06T19:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Department update timestamp", example = "2026-04-06T19:30:00")
	private LocalDateTime updatedAt;

}
