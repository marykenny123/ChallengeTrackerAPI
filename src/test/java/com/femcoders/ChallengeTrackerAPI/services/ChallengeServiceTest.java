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
        @DisplayName("Should return ChallengeResponse given a valid ID")
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

    @Nested
    @DisplayName("addChallenge(ChallengeRequest, UserDetail)")
    class AddChallengeTests {
        private User testUser;
        private UserDetail testUserDetail;
        private ChallengeRequest validRequest;
        private Challenge savedChallengeEntity;
        private ChallengeResponse expectedResponse;

        @BeforeEach
        void setup() {
            testUser = User.builder()
                    .id(1L)
                    .username("usertest")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            testUserDetail = new UserDetail(testUser);

            validRequest = new ChallengeRequest("Read more", "Read one novel each month for 12 months", Status.PENDING, Classification.PERSONAL_DEVELOPMENT, 3, "Special Spa day treatment");
            savedChallengeEntity    = Challenge.builder()
                    .id(1L)
                    .title(validRequest.title())
                    .description(validRequest.description())
                    .status(validRequest.status())
                    .classification(validRequest.classification())
                    .difficultyLevel(validRequest.difficultyLevel())
                    .prize((validRequest.prize()))
                    .user(testUser)
                    .build();
            expectedResponse = new ChallengeResponse(
                    savedChallengeEntity.getId(), savedChallengeEntity.getTitle(), savedChallengeEntity.getDescription(), savedChallengeEntity.getStatus(),
                    savedChallengeEntity.getClassification(), savedChallengeEntity.getDifficultyLevel(), savedChallengeEntity.getPrize(),testUser.getUsername()
            );
        }

        @Test
        @DisplayName("Should add a challenge successfully when user is valid")
        void shouldAddChallengeSuccessfully_whenUserIsValid() {
            given(userRepository.findByUsernameIgnoreCase(testUser.getUsername())).willReturn(Optional.of(testUser));
            given(challengeMapperImpl.dtoToEntity(validRequest, testUser)).willReturn(savedChallengeEntity);
            given(challengeRepository.save(savedChallengeEntity)).willReturn(savedChallengeEntity);
            given(challengeMapperImpl.entityToDto(savedChallengeEntity)).willReturn(expectedResponse);

            ChallengeResponse response = challengeService.addChallenge(validRequest, testUserDetail);

            assertThatThrownBy(() -> challengeService.addChallenge(validRequest, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User information is missing or invalid");

            verify(userRepository).findByUsernameIgnoreCase(org.mockito.ArgumentMatchers.anyString());
        }
    }

    @Nested
    @DisplayName("updateChallenge(Long id, ChallengeRequest challengeRequest, UserDetail userDetails")
    class UpdateChallengeTests {
        private User ownerUser;
        private User otherUser;
        private User adminUser;
        private UserDetail ownerUserDetail;
        private UserDetail otherUserDetail;
        private UserDetail adminUserDetail;
        private Challenge ownedChallenge;
        private Challenge otherChallenge;
        private ChallengeRequest updateRequest;
        private Challenge updatedOwnedChallengeEntity;
        private ChallengeResponse expectedResponse;

        @BeforeEach
        void setup() {
            ownerUser = User.builder()
                    .id(1L)
                    .username("ownerUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            ownerUserDetail = new UserDetail(ownerUser);

            otherUser = User.builder()
                    .id(2L)
                    .username("otherUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            otherUserDetail = new UserDetail(otherUser);

            adminUser = User.builder()
                    .id(3L)
                    .username("adminUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_ADMIN")))
                    .build();
            adminUserDetail = new UserDetail(adminUser);

            ownedChallenge = new Challenge(100L, "Stretch my body", "Do 10 mins stretching every morning", Status.IN_PROGRESS, Classification.HEALTH_AND_WELLBEING, 3, "Buy myself a garmin watch", ownerUser);
            otherChallenge = new Challenge(200L, "Do yoga", "Go to yoga class twice every week", Status.IN_PROGRESS, Classification.HEALTH_AND_WELLBEING, 1, "Treat myself to an ice-cream", otherUser);

            updateRequest = new ChallengeRequest("Updated Title", "Updated Description", Status.COMPLETED_SATISFACTORILY, Classification.FINANCES, 5, "Updated Prize");

            updatedOwnedChallengeEntity = Challenge.builder()
                    .id(ownedChallenge.getId())
                    .title(ownedChallenge.getTitle())
                    .description(ownedChallenge.getDescription())
                    .status(ownedChallenge.getStatus())
                    .classification(ownedChallenge.getClassification())
                    .difficultyLevel(ownedChallenge.getDifficultyLevel())
                    .prize(ownedChallenge.getPrize())
                    .user(ownerUser)
                    .build();

            expectedResponse = new ChallengeResponse(
                    updatedOwnedChallengeEntity.getId(),
                    updatedOwnedChallengeEntity.getTitle(),
                    updatedOwnedChallengeEntity.getDescription(),
                    updatedOwnedChallengeEntity.getStatus(),
                    updatedOwnedChallengeEntity.getClassification(),
                    updatedOwnedChallengeEntity.getDifficultyLevel(),
                    updatedOwnedChallengeEntity.getPrize(),
                    ownerUser.getUsername()
            );
        }

        @Test
        @DisplayName("Should uodate challenge successfully when authorized as owner")
        void shouldUpdateChallengeSuccessfullt_whenAuthorizedAsOwner() {
            Long challengeId = ownedChallenge.getId();

            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(ownedChallenge));

            given(challengeRepository.save(ArgumentMatchers.any(Challenge.class))).willReturn(updatedOwnedChallengeEntity);

            given(challengeMapperImpl.entityToDto(updatedOwnedChallengeEntity)).willReturn(expectedResponse);

            ChallengeResponse result = challengeService.updateChallenge(challengeId, updateRequest, ownerUserDetail);

            assertThat(result).isEqualTo(expectedResponse);
            verify(challengeRepository).findById(challengeId);

            verify(challengeRepository).save(ArgumentMatchers.any(Challenge.class));
            verify(challengeMapperImpl).entityToDto(updatedOwnedChallengeEntity);
        }

        @Test
        @DisplayName(("Should update challenge successfully when authorized as admin"))
        void shouldUpdateChallengeSuccessfully_whenAuthorizedAsAdmin() {
            Long challengeId = otherChallenge.getId();
            Challenge updatedOtherChallengeEntity = Challenge.builder()
                    .id(otherChallenge.getId())
                    .title(otherChallenge.getTitle())
                    .description(otherChallenge.getDescription())
                    .status(otherChallenge.getStatus())
                    .classification(otherChallenge.getClassification())
                    .difficultyLevel(otherChallenge.getDifficultyLevel())
                    .prize(otherChallenge.getPrize())
                    .user(otherUser)
                    .build();
            ChallengeResponse expectedAdminResponse = new ChallengeResponse(
                    updatedOwnedChallengeEntity.getId(),
                    updatedOtherChallengeEntity.getTitle(),
                    updatedOtherChallengeEntity.getDescription(),
                    updatedOtherChallengeEntity.getStatus(),
                    updatedOtherChallengeEntity.getClassification(),
                    updatedOtherChallengeEntity.getDifficultyLevel(),
                    updatedOtherChallengeEntity.getPrize(),
                    otherUser.getUsername()
            );

            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(otherChallenge));
            given(challengeRepository.save(ArgumentMatchers.any(Challenge.class))).willReturn(updatedOtherChallengeEntity);
            given(challengeMapperImpl.entityToDto(updatedOtherChallengeEntity)).willReturn(expectedAdminResponse);

            ChallengeResponse result = challengeService.updateChallenge(challengeId, updateRequest, adminUserDetail);

            assertThat(result).isEqualTo(expectedAdminResponse);
            verify(challengeRepository).findById(challengeId);
            verify(challengeRepository).save(ArgumentMatchers.any(Challenge.class));
            verify(challengeMapperImpl).entityToDto(updatedOtherChallengeEntity);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when regular user is not the owner")
        void shouldThrowAccessDeniedException_whenRegularUserIsNotOwner() {
            Long challengeId = otherChallenge.getId();
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(otherChallenge));

            assertThatThrownBy(() -> challengeService.updateChallenge(challengeId, updateRequest, ownerUserDetail))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not authorized to perform this action on this challenge.");

            verify(challengeRepository).findById(challengeId);
            verify(challengeRepository, never()).save(ArgumentMatchers.any(Challenge.class));
            verify(challengeMapperImpl, never()).entityToDto(ArgumentMatchers.any(Challenge.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when UserDetail is missing or invalid")
        void shouldThrowIllegalArgumentException_whenUserDetailsIsInvalid() {
            Long challengeId = ownedChallenge.getId();

            assertThatThrownBy(() -> challengeService.updateChallenge(challengeId, updateRequest, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User information is missing or invalid");

            verify(challengeRepository, never()).findById(ArgumentMatchers.anyLong());
            verify(challengeRepository, never()).save(ArgumentMatchers.any(Challenge.class));
            verify(challengeMapperImpl, never()).entityToDto(ArgumentMatchers.any(Challenge.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Destination ID is not found")
        void shouldThrowEntityNotFoundException_whenChallengeIdNotFound() {
            Long nonExistentId = 999L;
            given(challengeRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> challengeService.updateChallenge(nonExistentId, updateRequest, ownerUserDetail))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Challenge not found with id " + nonExistentId);

            verify(challengeRepository).findById(nonExistentId);
            verify(challengeRepository, never()).save(ArgumentMatchers.any(Challenge.class));
            verify(challengeMapperImpl, never()).entityToDto(ArgumentMatchers.any(Challenge.class));
        }
    }

    @Nested
    @DisplayName("deleteChallenge(Long id, UserDetail userDetails)")
    class DeleteChallengeTests {
        private User ownerUser;
        private User otherUser;
        private User adminUser;
        private UserDetail ownerUserDetail;
        private UserDetail otherUserDetail;
        private UserDetail adminUserDetail;
        private Challenge ownedChallenge;
        private Challenge otherChallenge;

        @BeforeEach
        void setup() {
            ownerUser = User.builder()
                    .id(1L)
                    .username("ownerUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            ownerUserDetail = new UserDetail(ownerUser);

            otherUser = User.builder()
                    .id(1L)
                    .username("otherUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_USER")))
                    .build();
            otherUserDetail = new UserDetail(otherUser);

            adminUser = User.builder()
                    .id(1L)
                    .username("adminUser")
                    .password("encoded_password")
                    .roles(Collections.singletonList(createRole("ROLE_ADMIN")))
                    .build();
            adminUserDetail = new UserDetail(adminUser);

            ownedChallenge = new Challenge(100L, "Stretch my body", "Do 10 mins stretching every morning", Status.IN_PROGRESS, Classification.HEALTH_AND_WELLBEING, 3, "Buy myself a garmin watch", ownerUser);
            otherChallenge = new Challenge(200L, "Do yoga", "Go to yoga class twice every week", Status.IN_PROGRESS, Classification.HEALTH_AND_WELLBEING, 1, "Treat myself to an ice-cream", otherUser);
        }

        @Test
        @DisplayName("Should delete challenge successfully when authorized as owner")
        void shouldDeleteChallengeSuccessfully_whenAuthorizedAsOwner() {
            Long challengeId = ownedChallenge.getId();
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(ownedChallenge));

            String result = challengeService.deleteChallenge(challengeId, ownerUserDetail);

            assertThat(result).isEqualTo("Challenge with id " + challengeId + " has been deleted");
            verify(challengeRepository).findById(challengeId);
            verify(challengeRepository).delete(ownedChallenge);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when regular user is not the owner")
        void shouldThrowAccessDeniedException_whenRegularUserIsNotOwner() {
            Long challengeId = otherChallenge.getId();
            given(challengeRepository.findById(challengeId)).willReturn(Optional.of(otherChallenge));

            assertThatThrownBy(() -> challengeService.deleteChallenge(challengeId, ownerUserDetail))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not authorized to perform this action on this challenge.");

            verify(challengeRepository).findById(challengeId);
            verify(challengeRepository, never()).delete(ArgumentMatchers.any(Challenge.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when UserDetail is missing or invalid")
        void shouldThrowIllegalArgumentException_whenUserDetailsIsInvalid() {
            Long challengeId = ownedChallenge.getId();

            assertThatThrownBy(() -> challengeService.deleteChallenge(challengeId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User information is missing or invalid");

            verify(challengeRepository, never()).findById(ArgumentMatchers.anyLong());
            verify(challengeRepository, never()).delete(ArgumentMatchers.any(Challenge.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Challenge ID is not found")
        void shouldThrowEntityNotFoundException_whenChallengeIdNotFound() {
            Long nonExistentId = 999L;
            given(challengeRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> challengeService.deleteChallenge(nonExistentId, ownerUserDetail))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Challenge not found with id " + nonExistentId);

            verify(challengeRepository).findById(nonExistentId);
            verify(challengeRepository, never()).delete(ArgumentMatchers.any(Challenge.class));
        }
    }
}
