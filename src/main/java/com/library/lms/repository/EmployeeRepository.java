package com.library.lms.repository;

import com.library.lms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmpId(String empId);

    List<Employee> findByStatus(Integer status);
}
