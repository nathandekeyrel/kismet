package com.github.nathandekeyrel.kismet.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query(value = "SELECT * FROM users u WHERE u.id != :currentUserId AND u.id NOT IN " +
                   "(SELECT ma.target_id FROM match_actions ma WHERE ma.actor_id = :currentUserId) " +
                   "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<User> findRandomUserNotInteractedWith(@Param("currentUserId") Long currentUserId);

    boolean existsByEmail(String email);
}
