package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.reliaquest.api.model.Employee;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

@SpringBootTest(classes = {com.reliaquest.api.ApiApplication.class})
class EmployeeClientTest {

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    void getById_404_returnsEmpty() {
        String base = "http://localhost:8112";
        EmployeeClient client = new EmployeeClient(restTemplate, base);
        UUID id = UUID.randomUUID();

        server.expect(request -> request.getURI().toString().equals(base + "/api/v1/employee/" + id))
                .andRespond(request -> new org.springframework.mock.http.client.MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND));

        Optional<Employee> result = client.getById(id.toString());
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByName_setsJsonContentType_andBubblesErrors() {
        String base = "http://localhost:8112";
        EmployeeClient client = new EmployeeClient(restTemplate, base);

        server.expect(request -> {
                    assertThat(request.getMethod()).isEqualTo(HttpMethod.DELETE);
                    assertThat(request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.APPLICATION_JSON_VALUE);
                })
                .andRespond(request -> new org.springframework.mock.http.client.MockClientHttpResponse(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> client.deleteByName("Alice"));
    }
}


