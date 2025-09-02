package com.femcoders.ChallengeTrackerAPI.controllers;

import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeRequest;
import com.femcoders.ChallengeTrackerAPI.dtos.challenge.ChallengeResponse;
import com.femcoders.ChallengeTrackerAPI.security.UserDetail;
import com.femcoders.ChallengeTrackerAPI.services.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
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

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ChallengeResponse> addChallenge(@RequestBody @Valid ChallengeRequest request, @AuthenticationPrincipal UserDetail userDetail) {
        ChallengeResponse response = challengeService.addChallenge(request, userDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
