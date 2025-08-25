package com.femcoders.ChallengeTrackerAPI.repositories;

import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
