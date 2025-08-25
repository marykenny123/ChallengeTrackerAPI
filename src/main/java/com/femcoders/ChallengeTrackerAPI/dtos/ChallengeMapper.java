package com.femcoders.ChallengeTrackerAPI.dtos;

import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;

public interface ChallengeMapper {
    Challenge dtoToEntity(ChallengeRequest dto, User user);
    ChallengeResponse entityToDto(Challenge challenge);
}
