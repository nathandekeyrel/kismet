package com.github.nathandekeyrel.kismet;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class FriendshipController {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipController(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/friends")
    public String showFriendsPage(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Friendship> pendingRequests = friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING);
        List<Friendship> acceptedFriends = friendshipRepository.findAcceptedFriendship(currentUser);

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }


}
