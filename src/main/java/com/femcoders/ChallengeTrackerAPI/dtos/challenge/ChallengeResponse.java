package com.femcoders.ChallengeTrackerAPI.dtos.challenge;

import com.femcoders.ChallengeTrackerAPI.models.Classification;
import com.femcoders.ChallengeTrackerAPI.models.Status;

public record ChallengeResponse(
        Long id,
        String title,
        String description,
        Status status,
        Classification classification,
        int difficultyLevel,
        String prize,
        String username
) {
}
