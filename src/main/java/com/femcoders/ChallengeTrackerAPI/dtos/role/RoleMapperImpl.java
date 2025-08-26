package com.femcoders.ChallengeTrackerAPI.dtos.role;

import com.femcoders.ChallengeTrackerAPI.models.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapperImpl implements RoleMapper{

    @Override
    public Role dtoToEntity(RoleRequest request) {
        if (request == null) return null;

        return Role.builder()
                .roleName(request.roleName())
                .build();
    }

    @Overridepublic RoleResponse entityToDto(Role role) {
        if (role == null) return null;

        return new RoleResponse(
                role.getId(),
                role.getRoleName()
        );
    }
}
