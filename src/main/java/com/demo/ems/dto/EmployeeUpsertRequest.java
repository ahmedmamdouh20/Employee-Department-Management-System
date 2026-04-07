package com.demo.ems.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(name = "EmployeeCreateRequest", description = "Employee create or update request")
public class EmployeeUpsertRequest {

    @Schema(description = "Employee first name", example = "Ahmed", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "first name is required")
    @Size(min = 2, max = 20, message = "first name must be between 2 and 20 characters")
    private String firstName;

    @Schema(description = "Employee last name", example = "Mamdouh", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "last name is required")
    @Size(min = 2, max = 20, message = "last name must be between 2 and 20 characters")
    private String lastName;

    @Schema(description = "Employee email", example = "ahmed@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "email is required")
    @Email
    @Size(min = 2, max = 100, message = "email must be between 2 and 100 characters")
    private String email;

    @Schema(description = "Employee phone number", example = "+201001234567")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$",  message = "phone number must match the pattern")
    private String phoneNumber;

    @Schema(description = "Employee hire date", example = "2026-04-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "hire date is required")
    private LocalDate hireDate;

    @Schema(description = "Employee salary", example = "15000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "salary is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal salary;

    @Schema(description = "Employee position", example = "Backend Engineer")
    @Size(max = 100)
    private String position;

    @Schema(description = "Department identifier", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "department id is required")
    private Long departmentId;

    @Schema(description = "Employee status", example = "ACTIVE")
    private String status;

    @Schema(description = "Manager identifier", example = "2")
    @NotNull(message = "manager id is required")
    private Long managerId;
}
