package com.demo.ems.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.demo.ems.dto.DepartmentUpsertRequest;
import com.demo.ems.dto.EmployeeUpsertRequest;

public final class CsvTemplateParser {

	private static final String DEPARTMENT_HEADER = "name,description,status";
	private static final String EMPLOYEE_HEADER = "firstName,lastName,email,phoneNumber,hireDate,salary,position,departmentId,status,managerId";

	private CsvTemplateParser() {
	}

	public static List<BulkUploadRow<DepartmentUpsertRequest>> parseDepartmentTemplate(MultipartFile file) {
		List<String[]> rows = readRows(file, DEPARTMENT_HEADER);
		List<BulkUploadRow<DepartmentUpsertRequest>> requests = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			String[] columns = rows.get(i);
			if (columns.length != 3) {
				throw new IllegalArgumentException("Invalid department template format at line " + (i + 2));
			}
			DepartmentUpsertRequest request = new DepartmentUpsertRequest();
			request.setName(valueOrNull(columns[0]));
			request.setDescription(valueOrNull(columns[1]));
			request.setStatus(valueOrNull(columns[2]));
			requests.add(new BulkUploadRow<>(i + 2, request));
		}
		return requests;
	}

	public static List<BulkUploadRow<EmployeeUpsertRequest>> parseEmployeeTemplate(MultipartFile file) {
		List<String[]> rows = readRows(file, EMPLOYEE_HEADER);
		List<BulkUploadRow<EmployeeUpsertRequest>> requests = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			String[] columns = rows.get(i);
			if (columns.length != 10) {
				throw new IllegalArgumentException("Invalid employee template format at line " + (i + 2));
			}
			try {
				EmployeeUpsertRequest request = new EmployeeUpsertRequest();
				request.setFirstName(valueOrNull(columns[0]));
				request.setLastName(valueOrNull(columns[1]));
				request.setEmail(valueOrNull(columns[2]));
				request.setPhoneNumber(valueOrNull(columns[3]));
				request.setHireDate(valueOrNull(columns[4]) == null ? null : LocalDate.parse(columns[4].trim()));
				request.setSalary(valueOrNull(columns[5]) == null ? null : new BigDecimal(columns[5].trim()));
				request.setPosition(valueOrNull(columns[6]));
				request.setDepartmentId(valueOrNull(columns[7]) == null ? null : Long.valueOf(columns[7].trim()));
				request.setStatus(valueOrNull(columns[8]));
				request.setManagerId(valueOrNull(columns[9]) == null ? null : Long.valueOf(columns[9].trim()));
				requests.add(new BulkUploadRow<>(i + 2, request));
			} catch (RuntimeException ex) {
				throw new IllegalArgumentException("Invalid employee template values at line " + (i + 2), ex);
			}
		}
		return requests;
	}

	private static List<String[]> readRows(MultipartFile file, String expectedHeader) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Template file is required");
		}

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null || !expectedHeader.equals(header.trim())) {
				throw new IllegalArgumentException("Invalid template header");
			}

			List<String[]> rows = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				rows.add(line.split(",", -1));
			}
			return rows;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to read template file", ex);
		}
	}

	private static String valueOrNull(String value) {
		String trimmed = value == null ? null : value.trim();
		return trimmed == null || trimmed.isEmpty() ? null : trimmed;
	}
}
