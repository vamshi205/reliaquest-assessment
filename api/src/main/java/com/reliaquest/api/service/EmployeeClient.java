package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeDto;
import com.reliaquest.api.dto.ServerResponse;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class EmployeeClient {

    private final RestTemplate restTemplate;
    private final String serverBaseUrl;

    public EmployeeClient(
            RestTemplate restTemplate, @Value("${mock.server.base-url:http://localhost:8112}") String serverBaseUrl) {
        this.restTemplate = restTemplate;
        this.serverBaseUrl = serverBaseUrl;
    }

    public List<Employee> getAll() {
        final var url = serverBaseUrl + "/api/v1/employee";
        ResponseEntity<ServerResponse<List<EmployeeDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<ServerResponse<List<EmployeeDto>>>() {});
        List<EmployeeDto> dtos = Objects.requireNonNull(response.getBody()).data;
        List<Employee> employeesList = (dtos == null) ? List.of() : mapToEmployees(dtos);
        return employeesList;
    }
public Optional<Employee> getById(String id) {
    final var url = serverBaseUrl + "/api/v1/employee/" + id;
    try {
        ResponseEntity<ServerResponse<EmployeeDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ServerResponse<EmployeeDto>>() {}
        );

        EmployeeDto dto = Objects.requireNonNull(response.getBody()).getData();
        return Optional.ofNullable(dto).map(EmployeeClient::mapToEmployee);

    } catch (HttpClientErrorException.NotFound e) {
        return Optional.empty();
    } catch (HttpClientErrorException | HttpServerErrorException e) {
        throw e;
    } catch (Exception e) {
        throw new HttpServerErrorException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                "Upstream server error when fetching employee by id");
    }
}


    public Employee create(CreateEmployeeInput input) {
        final var url = serverBaseUrl + "/api/v1/employee";
        HttpEntity<CreateEmployeeInput> entity = new HttpEntity<>(input);
        ResponseEntity<ServerResponse<EmployeeDto>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<ServerResponse<EmployeeDto>>() {}
        );
        return mapToEmployee(Objects.requireNonNull(response.getBody()).data);
    }

    public boolean deleteByName(String name) {
        final var url = serverBaseUrl + "/api/v1/employee";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("name", name), headers);
        try {
            ResponseEntity<ServerResponse<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<ServerResponse<Boolean>>() {}
            );
            Boolean ok = Objects.requireNonNull(response.getBody()).data;
            return Boolean.TRUE.equals(ok);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw e;
        }
    }

    private static List<Employee> mapToEmployees(List<EmployeeDto> dtos) {
        return dtos.stream().map(EmployeeClient::mapToEmployee).collect(Collectors.toList());
    }

    private static Employee mapToEmployee(EmployeeDto dto) {
        return Employee.builder()
                .id(dto.id != null ? dto.id.toString() : null)
                .name(dto.name)
                .salary(dto.salary)
                .age(dto.age)
                .title(dto.title)
                .email(dto.email)
                .build();
    }
}
