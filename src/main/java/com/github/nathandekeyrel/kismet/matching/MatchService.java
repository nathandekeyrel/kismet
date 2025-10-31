package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MatchService {

    private final UserRepository userRepository;

    public MatchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findPotentialMatch(User currentUser) {
        return userRepository.findRandomUserNotInteractedWith(currentUser.getId());
    }

}
