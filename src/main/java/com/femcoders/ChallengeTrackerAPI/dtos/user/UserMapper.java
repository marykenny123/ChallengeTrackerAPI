package com.femcoders.ChallengeTrackerAPI.dtos.user;

import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.models.Role;

import java.util.List;

public interface UserMapper {
    User dtoToEntity(UserRequest dto, List<Challenge> challenges, List<Role> roles);
    UserResponse entityToDto(User user);
}