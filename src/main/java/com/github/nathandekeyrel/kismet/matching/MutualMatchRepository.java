package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MutualMatchRepository extends JpaRepository<MutualMatch, Long> {
    List<MutualMatch> findByUser1OrUser2(User user1, User user2);

    @Query("SELECT m FROM MutualMatch m WHERE (m.user1 = :user1 AND m.user2 = :user2) OR (m.user1 = :user2 AND m.user2 = :user1)")
    Optional<MutualMatch> findMatchBetween(@Param("user1") User user1, @Param("user2") User user2);
}
