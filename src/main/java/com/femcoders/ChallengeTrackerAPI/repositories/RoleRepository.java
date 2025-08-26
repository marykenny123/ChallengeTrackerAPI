package com.femcoders.ChallengeTrackerAPI.repositories;

import com.femcoders.ChallengeTrackerAPI.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
