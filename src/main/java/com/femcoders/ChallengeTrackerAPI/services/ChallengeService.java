package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeResponse;
import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.repositories.ChallengeRepository;
import com.femcoders.ChallengeTrackerAPI.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeMapperImpl challengeMapperImpl;
    private final UserRepository userRepository;

    public List<ChallengeResponse> getAllChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();
        return challenges.stream()
                .map(challenge -> challengeMapperImpl.entityToDto(challenge))
                .toList();
    }

    public ChallengeResponse getChallengeById(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No challenge found with id " + id));
        return challengeMapperImpl.entityToDto(challenge);
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

    public List<ChallengeResponse> getChallengesByUserId(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No user found with id " + id));
        List<Challenge> challenges = challengeRepository.findAllByUser(user);
        return challenges.stream()
                .map(challenge -> challengeMapperImpl.entityToDto(challenge))
                .toList();
    }



}
