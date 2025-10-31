package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class MatchController {

    private final UserRepository userRepository;
    private final MatchService matchService;

    public MatchController(UserRepository userRepository, MatchService matchService) {
        this.userRepository = userRepository;
        this.matchService = matchService;
    }

    @GetMapping("/home")
    public String showMatchDeck(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<User> potentialMatch = matchService.findPotentialMatch(currentUser);
        potentialMatch.ifPresent(user -> model.addAttribute("potentialMatch", user));

        return "home";
    }

}
