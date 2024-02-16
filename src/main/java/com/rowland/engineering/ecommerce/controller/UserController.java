package com.rowland.engineering.ecommerce.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.rowland.engineering.ecommerce.dto.*;
import com.rowland.engineering.ecommerce.model.Category;
import com.rowland.engineering.ecommerce.model.User;
import com.rowland.engineering.ecommerce.security.CurrentUser;
import com.rowland.engineering.ecommerce.security.UserPrincipal;
import com.rowland.engineering.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    @Operation(
            description = "Get user by Id",
            summary = "Returns user by providing user id"
    )
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable(value = "id") Long id) {
        return userService.findUserById(id);
    }



    @Operation(
            description = "Get all registered users",
            summary = "Returns all registered users"
    )
    @GetMapping("/all")
    public List<User> getUsers(){
        return userService.getAllUsers();
    }


    @Operation(
            description = "Gets current authenticated user",
            summary = "Returns authenticated users basic information"
    )
    @GetMapping("/user/me")
    public Optional<UserSummary> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return userService.getBasicUserInfo(currentUser);
    }



    @Operation(
            description = "Returns list of all product vendors",
            summary = "Retrieves all vendors"
    )
    @GetMapping("/getVendors")
    public List<GetVendorsResponse> getAllVendors() {
        return userService.getAllVendors();
    }



    @Operation(
            description = "Updates registered users information",
            summary = "Enables user update profile information"
    )
    @PatchMapping("/updateUserInformation")
    public ResponseEntity<String> updateUserInformation(@Valid @RequestParam("companyLogo") MultipartFile companyLogo,
                                                       @RequestParam("profilePicture") MultipartFile profilePicture,
                                                       @RequestParam("userId") String userId,
                                                       @RequestParam("vendorCompany") String vendorCompany,
                                                       @RequestParam("territory") String territory,
                                                       @RequestParam("mobile") String mobile,
                                                       @RequestParam("email") String email,
                                                       @RequestParam("username") String username,
                                                       @RequestParam("lastName") String lastName,
                                                       @RequestParam("firstName") String firstName) throws IOException {
        return userService.updateUserInformation(companyLogo,  profilePicture,  userId, vendorCompany,  territory, mobile, email, username, lastName , firstName);

    }

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@CurrentUser UserPrincipal userPrincipal,
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest, userPrincipal);
        return new ResponseEntity<>("Password changed successfully!", HttpStatus.ACCEPTED);
    }




}
