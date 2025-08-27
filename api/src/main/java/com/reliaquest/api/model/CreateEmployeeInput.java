package com.reliaquest.api.model;
 
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEmployeeInput {
    @NotBlank @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;

    @NotNull @Min(1)
    private Integer salary;

    @NotNull @Min(16)
    @Max(75)
    private Integer age;

    @NotBlank @Size(min = 2, message = "Title must be at least 2 characters")
    private String title;
}
