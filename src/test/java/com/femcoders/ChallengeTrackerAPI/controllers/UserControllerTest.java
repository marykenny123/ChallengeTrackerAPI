package com.femcoders.ChallengeTrackerAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.ChallengeTrackerAPI.dtos.user.JwtResponse;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserRequest;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserUpdateRequest;
import com.femcoders.ChallengeTrackerAPI.models.Role;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.repositories.RoleRepository;
import com.femcoders.ChallengeTrackerAPI.repositories.UserRepository;
import com.femcoders.ChallengeTrackerAPI.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("UserController Integration Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String adminJwt;
    private String userJwt;
    private Long adminId;
    private Long userId;
    private Long anotherUserId;

    private final String ADMIN_USERNAME = "adminTestUser";
    private final String ADMIN_EMAIL = "admin@test.com";
    private final String USER_USERNAME = "userTestUser";

    @BeforeEach
    void setup() throws Exception {

        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.findByRoleNameIgnoreCase("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER", null)));
        Role adminRole = roleRepository.findByRoleNameIgnoreCase("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN", null)));

        String ADMIN_PASSWORD = "AdminPassword123!";
        User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .roles(List.of(adminRole))
                .build();
        userRepository.save(adminUser);
        adminId = adminUser.getId();

        String USER_EMAIL = "user@test.com";
        String USER_PASSWORD = "UserPassword123!";
        User normalUser = User.builder()
                .username(USER_USERNAME)
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .roles(List.of(userRole))
                .build();
        userRepository.save(normalUser);
        userId = normalUser.getId();

        adminJwt = performLogin(ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD);
        userJwt = performLogin(USER_USERNAME, USER_EMAIL, USER_PASSWORD);

        String ANOTHER_USERNAME = "anotherTestUser";
        String ANOTHER_EMAIL = "another@test.com";
        String ANOTHER_PASSWORD = "AnotherPassword123!";
        User anotherUser = User.builder()
                .username(ANOTHER_USERNAME)
                .email(ANOTHER_EMAIL)
                .password(passwordEncoder.encode(ANOTHER_PASSWORD))
                .roles(List.of(userRole))
                .build();
        userRepository.save(anotherUser);
        userId = anotherUser.getId();

        User user = new User();
        user.setUsername("Mary");

        Role role = Role.builder()
                .roleName("ROLE_ADMIN")
                .build();

        user.setRoles(List.of(role));
    }

    private String performLogin(String username, String email, String password) throws Exception {
        UserRequest loginRequest = new UserRequest(username, email, password);
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), JwtResponse.class);
        return jwtResponse.token();
    }

    private String asJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
            }
    }

    private ResultActions performGetRequest(String urlTemplate, Object... uriVars) throws Exception {
        return mockMvc.perform(get(urlTemplate, uriVars)
                .header("Authorization", "Bearer " + adminJwt)
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPutRequest(Long id, UserUpdateRequest requestBody, String jwtToken) throws Exception {
        return mockMvc.perform(put("/users/update/{id}", id)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)));
    }


    @Nested
    @DisplayName("Get /users/all")
    class GetAllUsersTest {

        @Test
        @DisplayName("should return all users with status 200 OK and correct content type")
        void getAllUsers_returnsLustOfUsers() throws Exception {
            performGetRequest("/users/all")
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].username", is(ADMIN_USERNAME)))
                    .andExpect(jsonPath("$[0].email", is(ADMIN_EMAIL)))
                    .andExpect(jsonPath("$[1].username", is(USER_USERNAME)));
        }

        @Test
        @DisplayName("Should return users with expected structure and data types")
        void getAllUsers_returnsCorrectStructureAndTypes() throws Exception {
            performGetRequest("/users/all")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].username").isString())
                    .andExpect(jsonPath("$[0].email").isString())
                    .andExpect(jsonPath("$[0].roles").isArray());
        }

        @Test
        @DisplayName("should return 401 Unauthorized if not authenticated")
        void getAllUsers_returnsUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/users/all")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 Forbidden when a normal USER tries to get all users")
        void getAllUsers_asNormalUser_shouldReturnForbidden() throws Exception {
            mockMvc.perform(get("/users/all")
                    .header("Authorization", "Bearer " + userJwt)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Nested
        @DisplayName("GET/ users/username/{username}")
        class GetUserByUsernameTests {

            @Test
            @DisplayName("should return 200 OK and UserResponse for existing username")
            void shouldGetUserByUsernameSuccessfully() throws Exception {

                performGetRequest("/users/username/{username}", ADMIN_USERNAME)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.id").value(adminId))
                        .andExpect(jsonPath("$.username").value(ADMIN_USERNAME))
                        .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                        .andExpect(jsonPath("$.challenges").isArray())
                        .andExpect(jsonPath("$.roles").isArray())
                        .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
            }

            @Test
            @DisplayName("should return 404 Not Found when user does not exist")
            void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

                String usernameDoesNotExist = "nonexistentuser";

                performGetRequest("/users/username/{username}", usernameDoesNotExist)
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("User not found with username " + usernameDoesNotExist));
            }

            @Test
            @DisplayName("should return 401 Unauthorized if not authenticated")
            void getByUsername_returnsUnauthorizedWhenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/users/username/{username}", ADMIN_USERNAME)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized());
            }
        }


}


}
