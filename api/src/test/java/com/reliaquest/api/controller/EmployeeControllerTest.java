package com.reliaquest.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeClient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EmployeeController.class, properties = {
        "spring.mvc.throw-exception-if-no-handler-found=true"
})
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeClient employeeClient;

    private static Employee emp(String id, String name, Integer salary) {
        return Employee.builder().id(id).name(name).salary(salary).age(30).title("Engineer").email("e@x.com").build();
    }

    @Test
    void getAllEmployees_returnsList() throws Exception {
        given(employeeClient.getAll()).willReturn(List.of(emp("1","Alice",100), emp("2","Bob",200)));
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void searchEmployees_filtersBySubstring_caseInsensitive() throws Exception {
        given(employeeClient.getAll()).willReturn(List.of(emp("1","Alice",100), emp("2","Bob",200)));
        mockMvc.perform(get("/api/v1/employee/search/al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void getEmployeeById_returnsEmployee_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        given(employeeClient.getById(id.toString())).willReturn(Optional.of(emp(id.toString(),"Alice",100)));
        mockMvc.perform(get("/api/v1/employee/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getEmployeeById_returns404_whenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(employeeClient.getById(id.toString())).willReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/employee/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found: " + id));
    }

    @Test
    void getEmployeeById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/employee/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void deleteEmployee_success_returnsDeletedName() throws Exception {
        UUID id = UUID.randomUUID();
        given(employeeClient.getById(id.toString())).willReturn(Optional.of(emp(id.toString(),"Alice",100)));
        given(employeeClient.deleteByName("Alice")).willReturn(true);
        mockMvc.perform(delete("/api/v1/employee/" + id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/plain")));
    }

    @Test
    void deleteEmployee_missing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(employeeClient.getById(id.toString())).willReturn(Optional.empty());
        mockMvc.perform(delete("/api/v1/employee/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void deleteEmployee_upstream5xx_returns502() throws Exception {
        UUID id = UUID.randomUUID();
        given(employeeClient.getById(id.toString())).willReturn(Optional.of(emp(id.toString(),"Alice",100)));
        given(employeeClient.deleteByName("Alice")).willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        mockMvc.perform(delete("/api/v1/employee/" + id))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }

    @Test
    void unknownRoute_returns404_json() throws Exception {
        mockMvc.perform(get("/api/v1/employee/foo/bar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found"));
    }
}


