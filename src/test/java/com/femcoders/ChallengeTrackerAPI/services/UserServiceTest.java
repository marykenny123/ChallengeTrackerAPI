package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.user.UserMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserRequest;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserResponse;
import com.femcoders.ChallengeTrackerAPI.models.Role;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.repositories.RoleRepository;
import com.femcoders.ChallengeTrackerAPI.repositories.UserRepository;
import com.femcoders.ChallengeTrackerAPI.security.UserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    UserMapperImpl userMapperImpl;

    @Mock
    RoleRepository roleRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    User testUser, testUser2;
    UserResponse testUserResponse, testUserResponse2;

    private User adminUser;
    private User normalUser;
    private User anotherNormalUser;

    private UserDetail adminUserDetail;
    private UserDetail normalUserDetail;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(10L).roleName("ROLE_USER").build();
        adminRole = Role.builder().id(11L).roleName("ROLE_ADMIN").build();

        testUser = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .password("encoded_password")
                .roles(new ArrayList<>(List.of(userRole)))
                .build();

        testUser2 = User.builder()
                .id(2L)
                .username("testUser2")
                .email("test2@example.com")
                .password("encoded_password")
                .roles(new ArrayList<>(List.of(adminRole)))
                .build();

        testUserResponse = new UserResponse(
                1L,
                "testuser",
                "test@example.com",
                List.of(),
                List.of("ROLE_USER")
        );

        testUserResponse = new UserResponse(
                2L,
                "testuser2",
                "test2@example.com",
                List.of(),
                List.of("ROLE_ADMIN")
        );

        normalUser = User.builder()
                .id(100L)
                .username("admin_user_test")
                .email("admin_user@test.com")
                .password("encodedAdminPassword")
                .roles(new ArrayList<>(List.of(userRole)))
                .build();

        anotherNormalUser = User.builder()
                .id(101L)
                .username("normal_user_test")
                .email("normal_user@test.com")
                .password("encodedNormalUserPassword")
                .roles(new ArrayList<>(List.of(userRole)))
                .build();

        adminUser = User.builder()
                .id(102L)
                .username("another_normal_user_test")
                .email("another_user@test.com")
                .password("encodedAnotherNormalPassword")
                .roles(new ArrayList<>(List.of(userRole)))
                .build();

        adminUserDetail = new UserDetail(adminUser);
        normalUserDetail = new UserDetail(normalUser);
    }

    @Nested
    @DisplayName("Register User")
    class RegisterUser {
        @Test
        void shouldRegisterNewUserSuccessfully() {
            UserRequest request = new UserRequest(
                    "newuser",
                    "user@example.com",
                    "NewPassword12345."
            );
            Role defaultRole = new Role(1L, "ROLE_USER", new ArrayList<>());

            User newUserMocked = User.builder()
                    .id(null)
                    .username(request.username())
                    .email(request.email())
                    .password(request.password())
                    .roles(new ArrayList<>(List.of(defaultRole)))
                    .build();

            given(roleRepository.findByRoleNameIgnoreCase("ROLE_USER")).willReturn(Optional.of(defaultRole));
            given(userRepository.existsByEmail(ArgumentMatchers.any(String.class))).willReturn(false);

            given(userMapperImpl.dtoToEntity(
                    ArgumentMatchers.any(UserRequest.class),
                    ArgumentMatchers.any(List.class),
                    ArgumentMatchers.any(List.class)
            )).willReturn(newUserMocked);

            given(userRepository.existsByUsername(ArgumentMatchers.any(String.class))).willReturn(false);

            UserResponse expectedUserResponse = new UserResponse (
                    1L,
                    newUserMocked.getUsername(),
                    newUserMocked.getEmail(),
                    Collections.emptyList(),
                    List.of("ROLE_USER")
            );
            given(userMapperImpl.entityToDto(ArgumentMatchers.any(User.class))).willReturn(expectedUserResponse);

            UserResponse response = userService.registerUser(request);

            assertThat(response).isNotNull();
            assertThat(response.username()).isEqualTo(newUserMocked.getUsername());
            assertThat(response.email()).isEqualTo(newUserMocked.getEmail());
            assertThat(response.roles()).contains("ROLE_USER");

            verify(userRepository, times(1)).existsByUsername(request.username());
            verify(userRepository, times(1)).existsByEmail(request.email());
            verify(roleRepository, times(1)).findByRoleNameIgnoreCase("ROLE_USER");
            verify(userMapperImpl, times(1)).dtoToEntity(
                    org.mockito.ArgumentMatchers.any(UserRequest.class),
                    org.mockito.ArgumentMatchers.any(List.class),
                    org.mockito.ArgumentMatchers.any(List.class)
            );
            verify(passwordEncoder, times(1)).encode(request.password());
            verify(userRepository, times(1)).save(org.mockito.ArgumentMatchers.any(User.class));
            verify(userMapperImpl, times(1)).entityToDto(org.mockito.ArgumentMatchers.any(User.class));
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameAlreadyExists() {
            UserRequest request = new UserRequest(
                    "existinguser",
                    "newuser@example.com",
                    "newPassword123#"
            );

            given(userRepository.existsByUsername(request.username())).willReturn(true);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);

            });

            assertThat(exception.getMessage()).contains("Username is already taken.");
            verify(userRepository, times(1)).existsByUsername(request.username());
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            UserRequest request = new UserRequest (
                    "newuser",
                    "existing@example.com",
                    "newPassword123#"
            );

            given(userRepository.existsByUsername(request.username())).willReturn(false);
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            assertThat(exception.getMessage()).contains("Email is already registered");
            verify(userRepository, times(1)).existsByUsername(request.username());
            verify(userRepository, times(1)).existsByEmail(request.email());
            verify(roleRepository, never()).findByRoleNameIgnoreCase(org.mockito.ArgumentMatchers.anyString());
        }

    }

}
