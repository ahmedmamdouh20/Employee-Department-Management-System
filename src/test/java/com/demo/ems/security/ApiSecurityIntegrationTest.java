package com.demo.ems.security;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getEndpointRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/departments"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Authentication is required to access this resource"));
	}

	@Test
	void getEndpointAllowsReadOnlyUser() throws Exception {
		mockMvc.perform(get("/api/departments")
				.with(httpBasic("user", "user123")))
				.andExpect(status().isOk());
	}

	@Test
	void writeEndpointRejectsReadOnlyUser() throws Exception {
		mockMvc.perform(post("/api/departments")
				.with(httpBasic("user", "user123"))
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "Security",
					  "description": "Access control"
					}
					"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
	}

	@Test
	void writeEndpointAllowsAdminUser() throws Exception {
		mockMvc.perform(post("/api/departments")
				.with(httpBasic("admin", "admin123"))
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "Security",
					  "description": "Access control"
					}
					"""))
				.andExpect(status().isOk());
	}
}
