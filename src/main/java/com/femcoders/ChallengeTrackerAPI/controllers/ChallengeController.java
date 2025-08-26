package com.femcoders.ChallengeTrackerAPI.controllers;

import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeResponse;
import com.femcoders.ChallengeTrackerAPI.services.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;

    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<ChallengeResponse> orderedChallenges = challengeService.getChallengesStartingWithCurrentUser();
        return ResponseEntity.ok(orderedChallenges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id) {
        ChallengeResponse challenge = challengeService.getChallengeById(id);
        return ResponseEntity.ok(challenge);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChallengeResponse>> getChallengesByUserId(@PathVariable Long userId) {
        List<ChallengeResponse> challenges = challengeService.getChallengesByUserId(userId);
        return ResponseEntity.ok(challenges);
    }

}
