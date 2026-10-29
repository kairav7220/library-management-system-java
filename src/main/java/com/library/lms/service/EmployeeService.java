package com.library.lms.service;

import com.library.lms.entity.Employee;
import com.library.lms.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, IdGenerator idGenerator,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> listActive() {
        return employeeRepository.findByStatus(0);
    }

    public Optional<Employee> getByRowNum(Integer rowNum) {
        return employeeRepository.findById(rowNum);
    }

    public Optional<Employee> getById(String empId) {
        return employeeRepository.findByEmpId(empId);
    }

    public List<Employee> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return listActive().stream()
                .filter(e -> contains(e.getName(), q) || contains(e.getEmail(), q)
                        || contains(e.getPhone(), q) || contains(e.getEmpId(), q)
                        || contains(e.getDesignation(), q))
                .toList();
    }

    @Transactional
    public Employee create(String name, String rawPassword, String email, String phone, String designation,
                           BigDecimal salary, String permanentAddress, String temporaryAddress) {
        Validators.requireNonBlank("Name", name);
        Validators.requireEmail("Email", email);
        Validators.requireNonNegative("Salary", salary);
        Employee employee = new Employee();
        employee.setEmpId(idGenerator.nextId("EMP", employeeRepository.findAll(), Employee::getRowNum));
        employee.setName(name.trim());
        employee.setUserId(null);
        employee.setPassword(rawPassword == null || rawPassword.isBlank()
                ? null : passwordEncoder.encode(rawPassword));
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setDesignation(designation);
        employee.setSalary(salary);
        employee.setUserRowNum("");
        employee.setPermanentAddress(permanentAddress);
        employee.setTemporaryAddress(temporaryAddress);
        employee.setStatus(0);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Integer rowNum, String name, String rawPassword, String email, String phone,
                           String designation, BigDecimal salary,
                           String permanentAddress, String temporaryAddress) {
        Employee employee = employeeRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        Validators.requireNonBlank("Name", name);
        employee.setName(name.trim());
        if (rawPassword != null && !rawPassword.isBlank()) {
            employee.setPassword(passwordEncoder.encode(rawPassword));
        }
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setDesignation(designation);
        employee.setSalary(salary);
        employee.setPermanentAddress(permanentAddress);
        employee.setTemporaryAddress(temporaryAddress);
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDelete(Integer rowNum) {
        Employee employee = employeeRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        employee.setStatus(1);
        employeeRepository.save(employee);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
