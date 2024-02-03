package com.rowland.engineering.ecommerce.service;

import com.rowland.engineering.ecommerce.dto.GetVendorsResponse;
import com.rowland.engineering.ecommerce.dto.UserSummary;
import com.rowland.engineering.ecommerce.model.Role;
import com.rowland.engineering.ecommerce.model.RoleName;
import com.rowland.engineering.ecommerce.model.User;
import com.rowland.engineering.ecommerce.repository.UserRepository;
import com.rowland.engineering.ecommerce.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;


    @Test
    @DisplayName("Should fetch basic user information")
    void getBasicUserInfo() {
        // Arrange
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        Role role1 = new Role();
        role1.setName(RoleName.ROLE_USER);

        Set<Role> roles = new HashSet<>();
        roles.add(role1);

        User mockUser = User.builder()
                .id(1L)
                .username("Rowland")
                .roles(roles)
                .email("row@gmail.com")
                .firstName("Rowly")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserSummary> result = userService.getBasicUserInfo(mockUserPrincipal);

        // Assertions
        assertTrue(result.isPresent());
        UserSummary userSummary = result.get();
        assertEquals(mockUser.getId(), userSummary.getId());
        assertEquals(mockUser.getUsername(), userSummary.getUsername());
        assertEquals(mockUser.getFirstName(), userSummary.getFirstName());
        assertEquals(mockUser.getEmail(), userSummary.getEmail());
        assertEquals(mockUser.getRoles().toString(), userSummary.getRole().toString());
        assertTrue(userSummary.getRole().contains("ROLE_USER"));
    }


    @Test
    @DisplayName("Should fetch all users")
    void getAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("rowland");
        user1.setFirstName("Rowland");
        user1.setLastName("Kanu");
        user1.setEmail("Kanurowland92@gmail.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("kanu");
        user2.setFirstName("Kanu");
        user2.setLastName("Flames");
        user2.setEmail("Kanu12@gmail.com");

        List<User> mockUsers = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<User> result = userService.getAllUsers();

        // Assertions
        assertEquals(2, result.size());

        for (int i = 0; i < mockUsers.size(); i++) {
            User expectedUser = mockUsers.get(i);
            User actualUser = result.get(i);

            assertEquals(expectedUser.getId(), actualUser.getId());
            assertEquals(expectedUser.getUsername(), actualUser.getUsername());
            assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
            assertEquals(expectedUser.getLastName(), actualUser.getLastName());
            assertEquals(expectedUser.getEmail(), actualUser.getEmail());

        }
    }


    @Test
    @DisplayName("Should find user by ID")
    void findUserById() {
        // Arrange
        Long userId = 1L;
        String username = "Rowland";
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.findUserById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(username, result.get().getUsername());
    }


    @Test
    @DisplayName("Should return empty optional when user is not found")
    void findUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserById(1L);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("Should fetch all vendors")
    void getAllVendors() {
        // Arrange
        User vendor1 = new User();
        vendor1.setId(1L);
        vendor1.setFirstName("Rowland");
        vendor1.setLastName("Kanu");
        vendor1.setUsername("Flames");
        vendor1.setMobile("08143358911");
        vendor1.setIsVendor("True");

        User vendor2 = new User();
        vendor2.setId(3L);
        vendor2.setFirstName("Samuel");
        vendor2.setLastName("Chi");
        vendor2.setUsername("Sheldon");
        vendor2.setMobile("08143358911");
        vendor2.setIsVendor("True");

        List<User> mockVendors = Arrays.asList(vendor1, vendor2);

        // Act
        when(userRepository.findAllByIsVendor("True")).thenReturn(mockVendors);

        List<GetVendorsResponse> result = userService.getAllVendors();

        // Assert
        assertEquals(2, result.size());

        for (int i = 0; i < mockVendors.size(); i++) {
            User user = mockVendors.get(i);
            GetVendorsResponse vendorsResponse = result.get(i);

            assertEquals(user.getFirstName(), vendorsResponse.getFirstName());
            assertEquals(user.getId(), vendorsResponse.getId());
            assertEquals(user.getMobile(), vendorsResponse.getMobile());
            assertEquals(user.getLastName(), vendorsResponse.getLastName());
            assertEquals(user.getUsername(), vendorsResponse.getUsername());
        }
    }
}