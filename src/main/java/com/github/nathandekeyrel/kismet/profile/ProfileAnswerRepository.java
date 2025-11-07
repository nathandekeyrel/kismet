package com.github.nathandekeyrel.kismet.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileAnswerRepository extends JpaRepository<ProfileAnswer, Long> {
    List<ProfileAnswer> findByProfile(Profile profile);

    Optional<ProfileAnswer> findByProfileAndPromptType(Profile profile, PromptType promptType);
}
