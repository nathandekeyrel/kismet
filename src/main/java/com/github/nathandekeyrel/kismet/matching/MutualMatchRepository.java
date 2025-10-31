package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MutualMatchRepository extends JpaRepository<MutualMatch, Long> {
    List<MutualMatch> findByUser1OrUser2(User user1, User user2);
}
