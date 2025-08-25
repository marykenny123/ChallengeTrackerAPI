package com.femcoders.ChallengeTrackerAPI.repositories;

import com.femcoders.ChallengeTrackerAPI.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
