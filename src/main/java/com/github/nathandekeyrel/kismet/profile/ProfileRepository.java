package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
}
