package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeClient;
import com.reliaquest.api.exception.ResourceNotFoundException;
import java.util.UUID;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private final EmployeeClient employeeClient;
    private final Validator validator;

    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeClient.getAll());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable("searchString") String searchString) {
        List<Employee> allEmployees = employeeClient.getAll();  

        if (searchString == null || searchString.isBlank()) {
            return ResponseEntity.ok(allEmployees);
        }

        List<Employee> filteredEmployees = allEmployees.stream()
                .filter(e -> e.getName() != null &&
                        e.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredEmployees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid employee id format");
        }

        Employee employee = employeeClient.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        final var all = employeeClient.getAll();
        final var max = all.stream()
                .map(Employee::getSalary)
                .filter(s -> s != null)
                .max(Integer::compareTo)
                .orElse(0);
        return ResponseEntity.ok(max);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        final var topSalaryEmps = employeeClient.getAll().stream()
                .sorted(Comparator.comparing(Employee::getSalary, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(topSalaryEmps);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeInput employeeDetailsInput) {
        var violations = validator.validate(employeeDetailsInput);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        Employee created = employeeClient.create(employeeDetailsInput);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Message", "Employee successfully added: " + created.getName())
                .body(created);
    }

    

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable("id") String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Employee not found");
        }

        Optional<Employee> employee = employeeClient.getById(id);
        if (employee.isEmpty()) {
            throw new ResourceNotFoundException("Employee not found");
        }
        boolean isSuccess = employeeClient.deleteByName(employee.get().getName());
        if (!isSuccess) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
        return ResponseEntity.ok(employee.get().getName() + " deleted successfully. ");
    }
}
