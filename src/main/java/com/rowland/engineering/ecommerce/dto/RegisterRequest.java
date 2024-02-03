package com.rowland.engineering.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.NaturalId;

@Data
@Builder
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 2,max = 40, message = "First name should be between two characters and forty characters")
    private String firstName;
    @NotBlank
    @Size(min = 2,max = 40, message = "Last name should be between two characters and forty characters")
    private String lastName;

    @NotBlank(message ="Date of birth must not be blank") //@Past - LocalDate
    private String dateOfBirth;

    @NotBlank(message ="Username must not be blank")
    @Size(min = 2,max = 40 , message = "Last name should be between two characters and forty characters")
    private String username;

    @NotBlank(message ="Mobile must not be blank")
    @Size(min = 2,max = 40)
    private String mobile;

    @Email(message = "Enter a valid email")
    @NaturalId
    private String email;

    @NotBlank(message ="Mobile must not be blank")
    @Size(min = 4)
    private String password;

    private String vendor;
    private String companyName;
    private String territory;

}
