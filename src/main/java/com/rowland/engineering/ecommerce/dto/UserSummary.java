package com.rowland.engineering.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummary {
    private Long id;
    private String username;
    private String firstName;
    private String email;
    private String role;
}
