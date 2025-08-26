package com.femcoders.ChallengeTrackerAPI.dtos.user;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        List<String> challenges,
        List<String> roles
) {
}
