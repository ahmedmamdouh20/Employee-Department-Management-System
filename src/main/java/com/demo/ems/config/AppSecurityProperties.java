package com.demo.ems.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
@Data
public class AppSecurityProperties {

	private List<UserDefinition> users = new ArrayList<>();

	public List<UserDefinition> getUsers() {
		return users;
	}

	public void setUsers(List<UserDefinition> users) {
		this.users = users;
	}

	@Data
	public static class UserDefinition {
		private String username;
		private String password;
		private final List<String> roles = new ArrayList<>();
	}
}
