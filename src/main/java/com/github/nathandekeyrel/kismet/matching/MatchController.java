package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.profile.ProfileService;
import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserService;
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

    private final UserService userService;
    private final MatchService matchService;
    private final ProfileService profileService;

    public MatchController(UserService userService, MatchService matchService, ProfileService profileService) {
        this.userService = userService;
        this.matchService = matchService;
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String showMatchDeck(Model model, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        Optional<User> potentialMatch = matchService.findPotentialMatch(currentUser);
        if (potentialMatch.isPresent()) {
            User match = potentialMatch.get();
            match.setProfile(profileService.getByUser(match));
            model.addAttribute("potentialMatch", match);
        }

        return "home";
    }

    @PostMapping("/home/like")
    public String likeUser(@RequestParam Long targetId, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        User targetUser = userService.getUserById(targetId);

        matchService.recordAction(currentUser, targetUser, ActionType.LIKE);

        return "redirect:/home";
    }

    @PostMapping("/home/pass")
    public String passUser(@RequestParam Long targetId, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        User targetUser = userService.getUserById(targetId);

        matchService.recordAction(currentUser, targetUser, ActionType.PASS);

        return "redirect:/home";
    }

    @GetMapping("/matches")
    public String showMatchesPage(Model model, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        List<User> matchedUsers = matchService.getMatchedUsersFor(currentUser);
        model.addAttribute("matches", matchedUsers);

        return "matches";
    }

}