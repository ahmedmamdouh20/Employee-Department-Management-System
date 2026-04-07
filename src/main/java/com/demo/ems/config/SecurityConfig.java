package com.demo.ems.config;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			AuthenticationEntryPoint authenticationEntryPoint,
			AccessDeniedHandler accessDeniedHandler) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
								"/h2-console/**")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/**").hasRole("ADMIN")
						.anyRequest().authenticated());

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService(AppSecurityProperties securityProperties,
			PasswordEncoder passwordEncoder) {
		List<UserDetails> users = securityProperties.getUsers().stream()
				.map(user -> User.builder()
						.username(user.getUsername())
						.password(passwordEncoder.encode(user.getPassword()))
						.roles(user.getRoles().toArray(String[]::new))
						.build())
				.toList();

		if (users.isEmpty()) {
			throw new IllegalStateException("At least one security user must be configured");
		}

		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> writeErrorResponse(
				response,
				HttpStatus.UNAUTHORIZED,
				"Authentication is required to access this resource");
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> writeErrorResponse(
				response,
				HttpStatus.FORBIDDEN,
				"You do not have permission to access this resource");
	}

	private void writeErrorResponse(HttpServletResponse response,
			HttpStatus status, String message) throws java.io.IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("""
				{"status":%d,"message":"%s"}
				""".formatted(status.value(), escapeJson(message)));
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
