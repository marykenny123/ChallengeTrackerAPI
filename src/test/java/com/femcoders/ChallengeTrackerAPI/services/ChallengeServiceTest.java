package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeRequest;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeResponse;
import com.femcoders.ChallengeTrackerAPI.exceptions.EntityNotFoundException;
import com.femcoders.ChallengeTrackerAPI.models.*;
import com.femcoders.ChallengeTrackerAPI.repositories.ChallengeRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService Unit Tests")
public class ChallengeServiceTest {
    @Mock
    ChallengeRepository challengeRepository;

    @Mock
    ChallengeMapperImpl challengeMapperImpl;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ChallengeService challengeService;

    private Role createRole(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        return role;
    }

    @Nested
    @DisplayName("getAllChallenges()")
    class GetAllChallengesTests {

        private User testUser;
        private Challenge testChallenge1;
        private Challenge testChallenge2;
        private ChallengeResponse testChallengeResponse1;
        private ChallengeResponse testChallengeResponse2;

        @BeforeEach
        void setup() {
            testUser = User.builder()
                    .id(1L)
                    .username("usertest")
                    .password("encoded-password123")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();

            testChallenge1 = new Challenge (1L, "Read more", "Read one novel each month for 12 months", Status.PENDING,
                    Classification.PERSONAL_DEVELOPMENT, 3, "Special Spa day treatment", testUser);

            testChallenge2 = Challenge.builder()
                    .id(2L)
                    .title("Eat more fruit and veg")
                    .description("Eat one salad and 3 pieces of fruit every day for a month")
                    .status(Status.PENDING)
                    .classification(Classification.HEALTH_AND_WELLBEING)
                    .difficultyLevel(2)
                    .prize("Trip to the theatre with Sara")
                    .user(testUser)
                    .build();


            testChallengeResponse1 = new ChallengeResponse(
                    testChallenge1.getId(),
                    testChallenge1.getTitle( ),
                    testChallenge1.getDescription(),
                    testChallenge1.getStatus(),
                    testChallenge1.getClassification(),
                    testChallenge1.getDifficultyLevel(),
                    testChallenge1.getPrize(),
                    testUser.getUsername()
            );
            testChallengeResponse2 = new ChallengeResponse(
                    testChallenge2.getId(),
                    testChallenge2.getTitle( ),
                    testChallenge2.getDescription(),
                    testChallenge2.getStatus(),
                    testChallenge2.getClassification(),
                    testChallenge2.getDifficultyLevel(),
                    testChallenge2.getPrize(),
                    testUser.getUsername()
            );

            given(challengeRepository.findAll()).willReturn(List.of(testChallenge1, testChallenge2));
            given(challengeMapperImpl.entityToDto(testChallenge1)).willReturn(testChallengeResponse1);
            given(challengeMapperImpl.entityToDto(testChallenge1)).willReturn(testChallengeResponse2);
        }

        @Test
        @DisplayName("Should return a list of all challenge responses")
        void shouldReturnListOfChallengeResponses() {
            given(challengeRepository.findAll()).willReturn(List.of(testChallenge1, testChallenge2));

            given(challengeMapperImpl.entityToDto(testChallenge1)).willReturn(testChallengeResponse1);
            given(challengeMapperImpl.entityToDto(testChallenge2)).willReturn(testChallengeResponse2);
            List<ChallengeResponse> result = challengeService.getAllChallenges();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testChallengeResponse1, testChallengeResponse2);
            verify(challengeRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getChallengeById(Long id)")
    class GetChallengeByIdTests {
        private User testUser;
        private Challenge testChallenge;
        private ChallengeResponse testChallengeResponse;

        @BeforeEach
        void setup() {
            testUser = User.builder()
                    .id(1L)
                    .username("usertest")
                    .password("encoder_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();

            testChallenge = new Challenge(1L, "Read more", "Read one novel each month for 12 months", Status.PENDING, Classification.PERSONAL_DEVELOPMENT, 3, "Special Spa day treatment", testUser);
            testChallengeResponse = new ChallengeResponse(
                    testChallenge.getId(), testChallenge.getTitle(), testChallenge.getDescription(), testChallenge.getStatus(), testChallenge.getClassification(),
                    testChallenge.getDifficultyLevel(), testChallenge.getPrize(), testUser.getUsername()
            );
        }

        @Test
        @DisplayName("Should return ChallengeResponse given a vañid ID")
        void shouldReturnChallengeResponseGivenAnId() {
            Long challengeId = 1l;

            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(testChallenge));
            given(challengeMapperImpl.entityToDto(testChallenge)).willReturn(testChallengeResponse);

            ChallengeResponse result = challengeService.getChallengeById(challengeId);

            assertThat(result).isEqualTo(testChallengeResponse);
            verify(challengeRepository).findById(challengeId);
            verify(challengeMapperImpl).entityToDto(testChallenge);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Challenge ID is not found")
        void shouldThrowEntityNotFoundException_whenIdNotFound() {
            Long nonExistentId = 99L;
            given(challengeRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> challengeService.getChallengeById(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Challenge not found with id " + nonExistentId);

            verify(challengeRepository).findById(nonExistentId);
        }
    }

}
