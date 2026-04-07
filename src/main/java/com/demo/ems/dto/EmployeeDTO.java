package com.demo.ems.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "EmployeeDTO", description = "Employee response payload")
public class EmployeeDTO {

	@Schema(description = "Employee identifier", example = "10")
	private Long id;

	@Schema(description = "Employee first name", example = "Ahmed")
	private String firstName;

	@Schema(description = "Employee last name", example = "Mamdouh")
	private String lastName;

	@Schema(description = "Employee email", example = "ahmed@example.com")
	private String email;

	@Schema(description = "Employee phone number", example = "+201001234567")
	private String phoneNumber;

	@Schema(description = "Employee hire date", example = "2026-04-01")
	private LocalDate hireDate;

	@Schema(description = "Employee salary", example = "15000.00")
	private BigDecimal salary;

	@Schema(description = "Employee position", example = "Backend Engineer")
	private String position;

	@Schema(description = "Employee status", example = "ACTIVE")
	private String status;

	@Schema(description = "Related department identifier", example = "1")
	private String departmentName;

	@Schema(description = "Manager identifier", example = "2")
	private String managerEmail;

	@Schema(description = "Employee creation timestamp", example = "2026-04-06T19:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Employee update timestamp", example = "2026-04-06T19:30:00")
	private LocalDateTime updatedAt;

}
