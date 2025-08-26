package com.femcoders.ChallengeTrackerAPI.dtos.user;

import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public User dtoToEntity(UserRequest dto, List<Challenge> challenges, List<Role> roles) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .challenges(new ArrayList<>(challenges))
                .roles(new ArrayList<>(roles))
                .build();
    }

    @Override
    public UserResponse entityToDto(User user) {
        List<String> challenges = user.getChallenges().stream()
                .map(challenge -> challenge.getTitle())
                .toList();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .toList();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                challenges,
                roles
        );
    }

}
