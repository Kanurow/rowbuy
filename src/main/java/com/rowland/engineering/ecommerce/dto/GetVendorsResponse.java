package com.rowland.engineering.ecommerce.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GetVendorsResponse {

    @NotBlank
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String username;


    @NotBlank
    private String mobile;


    @NotBlank
    private String email;

    @NotBlank
    private String vendorCompany;

    @NotBlank
    private String territory;

    private String companyLogoUrl;

    private String profilePictureUrl;
}
