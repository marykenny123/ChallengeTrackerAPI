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
                    .andExpect(jsonPath("$", hasSize(4)))
                    .andExpect(jsonPath("$[0].title", is("Read more")))
                    .andExpect(jsonPath("$[0].description", is("Read more")))
                    .andExpect(jsonPath("$[0].status", is(Status.PENDING.toString())))
                    .andExpect(jsonPath("$[0].classification", is(Classification.PERSONAL_DEVELOPMENT.toString())))
                    .andExpect(jsonPath("$[0].difficultyLevel", is(3)))
                    .andExpect(jsonPath("$[0].prize", is("Special Spa day treatment")));
        }
    }    }

//