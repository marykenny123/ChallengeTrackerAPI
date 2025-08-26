package com.femcoders.ChallengeTrackerAPI.dtos.role;

import com.femcoders.ChallengeTrackerAPI.models.Role;

public interface RoleMapper {
    Role dtoToEntity(RoleRequest request);
    RoleResponse entityToDto(Role role);
}
