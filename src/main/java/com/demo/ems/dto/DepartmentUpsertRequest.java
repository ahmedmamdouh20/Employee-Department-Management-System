package com.demo.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "DepartmentCreateRequest", description = "Department create or update request")
public class DepartmentUpsertRequest {

    @Schema(description = "Department name", example = "Software Engineer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Department description", example = "Builds and maintains the platform")
    @Size(max = 500, message = "Department description must not exceed 500 characters")
    private String description;

    @Schema(description = "Department status", example = "Active or Inactive")
    @Size(max = 10, message = "Department status must not exceed 10 characters")
    private String status;
}
