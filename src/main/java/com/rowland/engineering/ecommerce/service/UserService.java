package com.rowland.engineering.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rowland.engineering.ecommerce.dto.*;
import com.rowland.engineering.ecommerce.exception.BadRequestException;
import com.rowland.engineering.ecommerce.model.Product;
import com.rowland.engineering.ecommerce.model.User;
import com.rowland.engineering.ecommerce.repository.UserRepository;
import com.rowland.engineering.ecommerce.security.UserPrincipal;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @PostConstruct
    public void initializeCloudinary() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }



    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }


    public Optional<UserSummary> getBasicUserInfo(UserPrincipal currentUser) {
        Optional<User> activeUser = userRepository.findById(currentUser.getId());
        return activeUser.stream().map(user -> {
            UserSummary userDetail = new UserSummary();

            userDetail.setId(user.getId());
            userDetail.setUsername(user.getUsername());
            userDetail.setRole(user.getRoles().toString());
            userDetail.setEmail(user.getEmail());
            userDetail.setFirstName(user.getFirstName());
            return userDetail;
        }).findFirst();
    }

    public List<GetVendorsResponse> getAllVendors() {
        List<User> vendors = userRepository.findAllByIsVendor("True");
        System.out.println(vendors);

        return vendors.stream().map(user -> {
            GetVendorsResponse vendorsResponse = new GetVendorsResponse();
            vendorsResponse.setId(user.getId());
            vendorsResponse.setFirstName(user.getFirstName());
            vendorsResponse.setLastName(user.getLastName());
            vendorsResponse.setUsername(user.getUsername());
            vendorsResponse.setMobile(user.getMobile());
            vendorsResponse.setVendorCompany(user.getVendorCompany());
            vendorsResponse.setEmail(user.getEmail());
            vendorsResponse.setTerritory(user.getTerritory());
            vendorsResponse.setCompanyLogoUrl(user.getCompanyLogoUrl());
            vendorsResponse.setProfilePictureUrl(user.getProfilePictureUrl());
            return vendorsResponse;
        }).collect(Collectors.toList());
    }


    @Transactional
    public ResponseEntity<String> updateUserInformation(MultipartFile companyLogo,
                                                        MultipartFile profilePicture,
                                                        String userId,
                                                        String vendorCompany,
                                                        String territory,
                                                        String mobile,
                                                        String email,
                                                        String username,
                                                        String lastName,
                                                        String firstName) {
        try {

            Map<?, ?> uploadCompanyLogo = cloudinary.uploader().upload(companyLogo.getBytes(), ObjectUtils.emptyMap());
            String companyLogoUrl = (String) uploadCompanyLogo.get("secure_url");

            Map<?, ?> uploadProfilePicture = cloudinary.uploader().upload(profilePicture.getBytes(), ObjectUtils.emptyMap());
            String profilePictureUrl = (String) uploadProfilePicture.get("secure_url");

            User foundUser = userRepository.getReferenceById(Long.valueOf(userId));


            User userUpdatedInfo = User.builder()
                    .id(Long.valueOf(userId))
                    .firstName(firstName)
                    .lastName(lastName)
                    .mobile(mobile)
                    .email(email)
                    .username(username)
                    .build();

            if (Objects.equals(foundUser.getIsVendor(), "True")) {
                userUpdatedInfo.setIsVendor(foundUser.getIsVendor());
                userUpdatedInfo.setRoles(foundUser.getRoles());
                userUpdatedInfo.setDateOfBirth(foundUser.getDateOfBirth());
                userUpdatedInfo.setVendorCompany(vendorCompany);
                userUpdatedInfo.setCompanyLogoUrl(companyLogoUrl);
                userUpdatedInfo.setProfilePictureUrl(profilePictureUrl);
                userUpdatedInfo.setTerritory(territory);
                userUpdatedInfo.setPassword(foundUser.getPassword());
            } else {
                userUpdatedInfo.setIsVendor("False");
                userUpdatedInfo.setRoles(foundUser.getRoles());
                userUpdatedInfo.setDateOfBirth(foundUser.getDateOfBirth());
                userUpdatedInfo.setVendorCompany(foundUser.getVendorCompany());
                userUpdatedInfo.setCompanyLogoUrl("Not A Vendor");
                userUpdatedInfo.setProfilePictureUrl("Not A Vendor");
                userUpdatedInfo.setTerritory(foundUser.getTerritory());
            }


            User savedUserUpdatedInfo = userRepository.save(userUpdatedInfo);

            return ResponseEntity.status(HttpStatus.OK).body(savedUserUpdatedInfo.getFirstName() + " has updated profile information");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update new user details.");
        }
    }
}
