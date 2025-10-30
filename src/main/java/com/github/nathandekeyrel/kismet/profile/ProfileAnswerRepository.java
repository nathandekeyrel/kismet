package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileAnswerRepository extends JpaRepository<ProfileAnswer, Long> {
    List<ProfileAnswer> findByUser(User user);
    Optional<ProfileAnswer> findByUserAndPrompt(User currentUser, Prompt prompt);
}
