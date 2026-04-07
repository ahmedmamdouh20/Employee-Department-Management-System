package com.demo.ems.repo;

import com.demo.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository  extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailIgnoreCase(String email);
    Page<Employee> findAllByDepartmentId(Long departmentId, Pageable pageable);

}
