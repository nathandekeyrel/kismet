package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchActionRepository extends JpaRepository<MatchAction, Long> {
    Optional<MatchAction> findByUserAndTarget(User user1, User user2);
}
