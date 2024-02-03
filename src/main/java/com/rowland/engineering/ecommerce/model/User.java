package com.rowland.engineering.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.rowland.engineering.ecommerce.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.util.*;


//@JsonFilter("GetUserExceptUserPassword")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class User extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2,max = 40)
    private String firstName;

    @NotBlank
    @Size(min = 2,max = 40)
    private String lastName;

    @NotBlank
    @Size(min = 2,max = 20)
    private String username;


    @NotBlank
    @Size(max = 15)
    private String mobile;

    @NotBlank
    private String dateOfBirth;


    @Email
    @NaturalId
    private String email;


    @NotBlank
    private String password;


    private String isVendor;

    private String vendorCompany;

    private String territory;

    private String companyLogoUrl;
    private String profilePictureUrl;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


    public User(String firstName, String lastName, String dateOfBirth, String username, String email, String password, String mobile) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
    }



}