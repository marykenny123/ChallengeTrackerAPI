package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeResponse;
import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.repositories.ChallengeRepository;
import com.femcoders.ChallengeTrackerAPI.repositories.UserRepository;
import com.femcoders.ChallengeTrackerAPI.security.UserDetail;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.channels.IllegalChannelGroupException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeMapperImpl challengeMapperImpl;
    private final UserRepository userRepository;

    private void validateUser(UserDetail userDetails)  {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalArgumentException("User information is missing or invalid");
        }
    }

    private void checkOwnership(Challenge challenge, UserDetail userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }
        if (!userDetails.getUsername().equals(challenge.getUser().getUsername())) {
            throw new AccessDeniedException("You are not authorized to perform this action on this challenge.");
        }
    }

    public List<ChallengeResponse> getAllChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();
        return challenges.stream()
                .map(challenge -> challengeMapperImpl.entityToDto(challenge))
                .toList();
    }

    public List<ChallengeResponse> getChallengesStartingWithCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetail)) {
            return getAllChallenges();
        }

        UserDetail currentUserDetail = (UserDetail) authentication.getPrincipal();
        Long currentUserId = currentUserDetail.getId();

        List<ChallengeResponse> userOwnedChallenges = getChallengesByUserId(currentUserId);

        List<Challenge> allChallengeEntities = challengeRepository.findAll();

        List<ChallengeResponse> otherUsersChallenges = allChallengeEntities.stream()
                .filter(challenge -> !challenge.getUser().getId().equals(currentUserId))
                .map(challenge -> challengeMapperImpl.entityToDto(challenge))
                .toList();

        List<ChallengeResponse> finalOrderedList = new ArrayList<>();
        finalOrderedList.addAll(userOwnedChallenges);
        finalOrderedList.addAll(otherUsersChallenges);

        return finalOrderedList;
    }

    public ChallengeResponse getChallengeById(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No challenge found with id " + id));
        return challengeMapperImpl.entityToDto(challenge);
    }

    public List<ChallengeResponse> getChallengesByUserId(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No user found with id " + id));
        List<Challenge> challenges = challengeRepository.findAllByUser(user);
        return challenges.stream()
                .map(challenge -> challengeMapperImpl.entityToDto(challenge))
                .toList();
    }

}
