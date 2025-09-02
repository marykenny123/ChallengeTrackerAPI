package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.role.RoleMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.role.RoleResponse;
import com.femcoders.ChallengeTrackerAPI.exceptions.EntityNotFoundException;
import com.femcoders.ChallengeTrackerAPI.models.Role;
import com.femcoders.ChallengeTrackerAPI.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapperImpl roleMapperImpl;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> roleMapperImpl.entityToDto(role))
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Role.class.getSimpleName(), id));
        return roleMapperImpl.entityToDto(role);
    }
}
