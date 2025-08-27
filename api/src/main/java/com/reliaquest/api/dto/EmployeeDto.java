package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class EmployeeDto {
    public UUID id;

    @JsonProperty("employee_name")
    public String name;

    @JsonProperty("employee_salary")
    public Integer salary;

    @JsonProperty("employee_age")
    public Integer age;

    @JsonProperty("employee_title")
    public String title;

    @JsonProperty("employee_email")
    public String email;
}


