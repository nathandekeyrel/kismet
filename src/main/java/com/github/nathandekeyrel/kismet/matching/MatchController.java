package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MatchController {

    private final UserRepository userRepository;
    private final MatchService matchService;

    public MatchController(UserRepository userRepository, MatchService matchService) {
        this.userRepository = userRepository;
        this.matchService = matchService;
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String showMatchDeck(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<User> potentialMatch = matchService.findPotentialMatch(currentUser);
        potentialMatch.ifPresent(user -> model.addAttribute("potentialMatch", user));

        return "home";
    }

    @PostMapping("/home/like")
    public String likeUser(@RequestParam Long targetId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        matchService.recordAction(currentUser, targetUser, ActionType.LIKE);

        return "redirect:/home";
    }

    @PostMapping("/home/pass")
    public String passUser(@RequestParam Long targetId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        matchService.recordAction(currentUser, targetUser, ActionType.PASS);

        return "redirect:/home";
    }

    @GetMapping("/matches")
    public String showMatchesPage(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<User> matchedUsers = matchService.getMatchedUsersFor(currentUser);
        model.addAttribute("matches", matchedUsers);

        return "matches";
    }

}
