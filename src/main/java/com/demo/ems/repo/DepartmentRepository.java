package com.demo.ems.repo;

import com.demo.ems.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByNameIgnoreCase(String name);

    @Query("select count(e) from Employee e where e.department.id = :deptId")
    long countEmployeesByDepartmentId(Long deptId);
}
