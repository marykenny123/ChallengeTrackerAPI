package com.femcoders.ChallengeTrackerAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.ChallengeTrackerAPI.models.Classification;
import com.femcoders.ChallengeTrackerAPI.models.Status;
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
}
