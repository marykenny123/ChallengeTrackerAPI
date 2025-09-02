package com.femcoders.ChallengeTrackerAPI.dtos.challenge;

import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapperImpl implements ChallengeMapper {
    @Override
    public Challenge dtoToEntity(ChallengeRequest dto, User user) {
        return Challenge.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .classification(dto.classification())
                .difficultyLevel(dto.difficultyLevel())
                .prize(dto.prize())
                .user(user)
                .build();
    }

    @Override
    public ChallengeResponse entityToDto(Challenge challenge) {
        String username = (challenge.getUser() != null) ? challenge.getUser().getUsername() : null;
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getStatus(),
                challenge.getClassification(),
                challenge.getDifficultyLevel(),
                challenge.getPrize(),
                username
        );
    }
}
