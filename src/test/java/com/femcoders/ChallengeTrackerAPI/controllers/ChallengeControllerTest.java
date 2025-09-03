package com.femcoders.ChallengeTrackerAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeRequest;
import com.femcoders.ChallengeTrackerAPI.models.Classification;
import com.femcoders.ChallengeTrackerAPI.models.Role;
import com.femcoders.ChallengeTrackerAPI.models.Status;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.security.UserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Collections;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ChallengeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String asJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private ResultActions performGetRequest(String url) throws Exception {
        return mockMvc.perform(get(url)
                .with(user("testuser").roles("USER"))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPostRequest(String url, Object body, UserDetail userDetail) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(body))
                .with(user(userDetail))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPutRequest(String url, Object body, UserDetail userDetail) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(body))
                .with(user(userDetail))
                .accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPutRequestUnauthenticated(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(body))
                .accept(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("Get / challenges")
    class getAllChallengeTest {

        @Test
        @DisplayName("should return all challenges with status 200 OK and correct content type")
        void getAllChallenges_returnsListOfChallenges() throws Exception {
            performGetRequest("/challenges")
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].title", is("Read more")))
                    .andExpect(jsonPath("$[0].description", is("Read one novel each month for 12 months")))
                    .andExpect(jsonPath("$[0].status", is(Status.PENDING.toString())))
                    .andExpect(jsonPath("$[0].classification", is(Classification.PERSONAL_DEVELOPMENT.toString())))
                    .andExpect(jsonPath("$[0].difficultyLevel", is(3)))
                    .andExpect(jsonPath("$[0].prize", is("Special Spa day treatment")));
        }


        @Test
        @DisplayName("Should return challenges with expected structure and data types")
        void getAllChallenges_returnsCorrectStructureAndTypes() throws Exception {
            performGetRequest("/challenges")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].title").isString())
                    .andExpect(jsonPath("$[0].description").isString())
                    .andExpect(jsonPath("$[0].status").isString())
                    .andExpect(jsonPath("$[0].classification").isString())
                    .andExpect(jsonPath("$[0].difficultyLevel").isNumber())
                    .andExpect(jsonPath("$[0].prize").isString());
        }
    }


    @Nested
    @DisplayName("GET /challenges/{id}")
    class GetChallengeByIdTests {
        private final Long EXISTING_CHALLENGE_ID = 1L;
        private final Long NON_EXISTING_CHALLENGE_ID = 99L;

        @Test
        @DisplayName("Should return the challenge by ID with status 200 OK")
        void getChallengeById_returnsChallenge_whenIdExists() throws Exception {
            performGetRequest("/challenges/" + EXISTING_CHALLENGE_ID)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(EXISTING_CHALLENGE_ID.intValue())))
                    .andExpect(jsonPath("$.title", is("Read more")))
                    .andExpect(jsonPath("$.description", is("Read one novel each month for 12 months")));

        }

        @Test
        @DisplayName("Should return 4041 Not Found when challenge ID does not exist")
        void getChallengeById_returnsNotFound_whenIdDoesNotExist() throws Exception {
            performGetRequest("/challenges/" + NON_EXISTING_CHALLENGE_ID)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /challenges/user/{userId}")
    class GetChallengesByUserIdTests {
        private Long USER_WITH_CHALLENGE_ID = 1L;
        private final Long USER_WITHOUT_CHALLENGE_ID = 3L;
        private final Long NON_EXISTENT_USER_ID = 99L;

        @Test
        @DisplayName("Should return a list of challenges for an existing user with challenges")
        void getChallengesByUserId_returnsEmptyList_whenUserExistsButNoDestinations() throws Exception {
            performGetRequest("/challenges/user/" + USER_WITH_CHALLENGE_ID)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$.[0].username", is("Mary")));
        }

        @Test
        @DisplayName("Should return an empty list when user exists but has no challenges")
        void getChallengeByUserId_returnsEmptyList_whenUserExistsButNoChallenges() throws Exception {
            performGetRequest("/challenges/user/" + USER_WITHOUT_CHALLENGE_ID)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", empty()));
        }

        @Test
        @DisplayName("Should return 404 Not Found when user ID does not exist")
        void getChallengesByUserId_returnsNotFound_whenUserDoesNotExist() throws Exception {
            performGetRequest("/challenges/user/" + NON_EXISTENT_USER_ID)
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("POST /challenges")
    class AddChallengeTests {

        private static final String ROLE_USER_NAME = "ROLE_USER";
        private static final String ROLE_ADMIN_NAME = "ROLE_ADMIN";

        private User userEntityUserRole;
        private UserDetail userDetailUserRole;

        private User userEntityAdminRole;
        private UserDetail userDetailAdminRole;

        private User userEntityNonExistent;
        private UserDetail userDetailNonExistent;

        private ChallengeRequest validChallengeRequest;

        private final String EXISTING_USERNAME_USER = "Carmen";
            private final String EXISTING_USERNAME_ADMIN = "Mary";
            private final String NON_EXISTENT_USERNAME = "usertest";

            private Role createRole(String roleName) {
                Role role = new Role();
                role.setRoleName(roleName);
                return role;
            }

            @BeforeEach
            void setuo() {
                validChallengeRequest = new ChallengeRequest(
                        "Eat more fruit and veg",
                        "Eat one salad and 3 pieces of fruit every day for a month",
                        Status.PENDING,
                        Classification.HEALTH_AND_WELLBEING,
                        2,
                        "Trip to the theatre with Sara"
                );

                Role userRole = createRole(ROLE_USER_NAME);

                userEntityUserRole = User.builder()
                        .id(2L)
                        .username(EXISTING_USERNAME_USER)
                        .password("any_encoded_password")
                        .roles(Collections.singletonList(userRole))
                        .build();
                userDetailUserRole = new UserDetail(userEntityUserRole);

                Role adminRole = createRole(ROLE_ADMIN_NAME);

                userEntityAdminRole = User.builder()
                        .id(1L)
                        .username(EXISTING_USERNAME_ADMIN)
                        .password("any_encoded_password")
                        .roles(Collections.singletonList(adminRole))
                        .build();
                userDetailAdminRole = new UserDetail(userEntityAdminRole);

                Role nonExistentUserRole = createRole(ROLE_USER_NAME);

                userEntityNonExistent = User.builder()
                        .id(99L)
                        .username(NON_EXISTENT_USERNAME)
                        .password("any_encoded_password")
                        .roles(Collections.singletonList(nonExistentUserRole))
                        .build();
                userDetailNonExistent = new UserDetail(userEntityNonExistent);
            }

            @Test
            @DisplayName("Should create a new challenge when authenticated as USER with valid data (201 Created)")
            void addChallenge_createsNewDestination_whenUserAuthenticatedAndValid() throws Exception {
                performPostRequest("/challenges", validChallengeRequest, userDetailUserRole)
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.title").isString())
                        .andExpect(jsonPath("$.description").isString())
                        .andExpect(jsonPath("$.status").isString())
                        .andExpect(jsonPath("$.classification").isString())
                        .andExpect(jsonPath("$.difficultyLevel").isNumber())
                        .andExpect(jsonPath("$.username", is(EXISTING_USERNAME_USER)));

                mockMvc.perform(get("/challenges")
                            .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(6)));
            }

            @Test
            @DisplayName("Should return 400 Bad Request when request body is invalid (e.g. empty description)")
            void addChallenge_returnBadRequest_whenInvalidData() throws Exception {
                ChallengeRequest invalidRequest = new ChallengeRequest(
                      "Stop biting my nails",
                      "",
                      Status.PENDING,
                        Classification.PERSONAL_DEVELOPMENT,
                        3,
                        "Get a manicure"
                );

                performPostRequest("/challenges", invalidRequest, userDetailUserRole)
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.details.description", is("A brief description is required")));
            }

            @Test
            @DisplayName("Should return 404 Not Found when authenticated user does not exist in DB")
            void addChallenge_returnsNotFound_whenAuthenticatedUserDoesNotExistInDB() throws Exception {
                performPostRequest("/challenges", validChallengeRequest, userDetailNonExistent)
                        .andExpect(status().isNotFound());
            }
    }

    @Nested
    @DisplayName("PUT /challenges/{id}")
    class UpdateChallengeTests {

        private final Long CHALLENGE_ID_OWNED_BY_USER_CARMEN = 2L;
        private final Long CHALLENGE_ID_OWNED_BY_USER_BRIAN = 4L;
        private final Long NON_EXISTENT_CHALLENGE_ID = 99L;

        private UserDetail userDetailUserCarmen;
        private UserDetail userDetailAdminMary;
        private UserDetail userDetailUserBrian;

        private ChallengeRequest validUpdateRequest;
        private ChallengeRequest invalidUpdateRequest;

        private Role createRole(String roleName) {
            Role role = new Role();
            role.setRoleName(roleName);
            return role;
        }

        @BeforeEach
        void setup() {
            User userCarmenEntity = User.builder()
                    .id(2L)
                    .username("Carmen")
                    .password("any_encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            userDetailUserCarmen = new UserDetail(userCarmenEntity);

            User userMaryEntity = User.builder()
                    .id(1L)
                    .username("Mary")
                    .password("any_encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_ADMIN")))
                    .build();
            userDetailAdminMary = new UserDetail(userMaryEntity);

            User userBrianEntity = User.builder()
                    .id(4L)
                    .username("Brian")
                    .password("any_encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            userDetailUserBrian = new UserDetail(userBrianEntity);

            validUpdateRequest = new ChallengeRequest(
                    "Walk more",
                    "Go for a 20 min walk every day",
                    Status.IN_PROGRESS,
                    Classification.HEALTH_AND_WELLBEING,
                    2,
                    "Get a massage as a prize to myself"
            );

            invalidUpdateRequest = new ChallengeRequest(
                    "",
                    "Go for a 20 min walk every day",
                    Status.IN_PROGRESS,
                    Classification.HEALTH_AND_WELLBEING,
                    2,
                    "Get a massage as a prize to myself"
            );
        }

        @Test
        @DisplayName("Should delete challenge successfully when authenticated as owner (200 OK)")
        void updateChallenge_vyOwner_returnsOk() throws Exception {
            Long targetChallengeId = CHALLENGE_ID_OWNED_BY_USER_CARMEN;

            performPutRequest("/challenges/" + targetChallengeId, validUpdateRequest, userDetailUserCarmen)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(targetChallengeId.intValue())))
                    .andExpect(jsonPath("$.title", is(validUpdateRequest.title())))
                    .andExpect(jsonPath("$.description", is(validUpdateRequest.description())))
                    .andExpect(jsonPath("$.status", is(validUpdateRequest.status().toString())))
                    .andExpect(jsonPath("$.classification", is(validUpdateRequest.classification().toString())))
                    .andExpect(jsonPath("$.difficultyLevel", is(validUpdateRequest.difficultyLevel())))
                    .andExpect(jsonPath("$.prize", is(validUpdateRequest.prize())))
                    .andExpect(jsonPath("$.username", is(userDetailUserCarmen.getUsername())));

            mockMvc.perform(get("/challenges/" + targetChallengeId)
                    .with(user(userDetailUserCarmen))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is(validUpdateRequest.title())))
                    .andExpect(jsonPath("$.description", is(validUpdateRequest.description())));
        }

        @Test
        @DisplayName("Should update challenge successfully when authenticated as admin (200 OK)")
        void updateChallenge_byAdmin_returnsOk() throws Exception {
            Long targetChallengeId = CHALLENGE_ID_OWNED_BY_USER_BRIAN;

            performPutRequest("/challenges/" + targetChallengeId, validUpdateRequest, userDetailAdminMary)
                    .andExpect(jsonPath("$.id", is(targetChallengeId.intValue())))
                    .andExpect(jsonPath("$.title", is(validUpdateRequest.title())))
                    .andExpect(jsonPath("$.description", is(validUpdateRequest.description())))
                    .andExpect(jsonPath("$.status", is(validUpdateRequest.status().toString())))
                    .andExpect(jsonPath("$.classification", is(validUpdateRequest.classification().toString())))
                    .andExpect(jsonPath("$.difficultyLevel", is(validUpdateRequest.difficultyLevel())))
                    .andExpect(jsonPath("$.prize", is(validUpdateRequest.prize())))
                    .andExpect(jsonPath("$.username", is(userDetailUserBrian.getUsername())));

            mockMvc.perform(get("/challenges/" + targetChallengeId)
                    .with(user(userDetailAdminMary))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is(validUpdateRequest.title())))
                    .andExpect(jsonPath("$.description", is(validUpdateRequest.description())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request vody is invalid (e.g., empty title)")
        void updateChallenge_returnsBadRequest_whenInvalidData() throws Exception {
            Long targetChallengeId = CHALLENGE_ID_OWNED_BY_USER_BRIAN;

            performPutRequest("/challenges/" + targetChallengeId, validUpdateRequest, userDetailUserCarmen)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message", is("You are not authorized to perform this action on this challenge.")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when challenge ID does not exist")
        void updateChallenge_idNotFound_returnsNotFound() throws Exception {
            Long targetChallengeId = NON_EXISTENT_CHALLENGE_ID;

            performPutRequest("/challenges/" + targetChallengeId, validUpdateRequest, userDetailUserCarmen)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Challenge not found with id " + targetChallengeId)));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when not authenticated")
        void updateChallenge_unauthenticated_returnsUnauthorized() throws Exception {
            Long targetChallengeId = CHALLENGE_ID_OWNED_BY_USER_CARMEN;

            performPutRequestUnauthenticated("/challenges/" + targetChallengeId, validUpdateRequest)
                    .andExpect(status().isUnauthorized());
        }
    }

}
