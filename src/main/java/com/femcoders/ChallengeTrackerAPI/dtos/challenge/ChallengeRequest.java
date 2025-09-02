package com.femcoders.ChallengeTrackerAPI.dtos.challenge;

import com.femcoders.ChallengeTrackerAPI.models.Classification;
import com.femcoders.ChallengeTrackerAPI.models.Status;
import jakarta.validation.constraints.*;

public record ChallengeRequest(
        @NotBlank(message = "Challenge Title is required")
        @Size(max = 70, message = "Title must be less than 70 characters")
        String title,

        @NotBlank(message = "A brief description is required")
        @Size(max = 150, message = "Description must be less than 150 characters")
        String description,

        @NotNull(message = "Choose from one of the options for status")
        Status status,

        @NotNull(message = "Choose from one of the options for classification")
        Classification classification,

        @NotNull(message ="Choose a difficulty level between 1 and 5, 1 = least difficulult for you and 5 = most difficult level")
        @Min(value = 1, message = "Difficulty level cannot be lower than 1")
        @Max(value = 5, message = "Difficulty leven cannot be higher than 5")
        int difficultyLevel,

        @NotBlank(message = "You must enter a prize to give yourself when you accomplish your challenge")
        @Size(max = 150, message = "Description must be less than 150 characters")
        String prize
) {
}
