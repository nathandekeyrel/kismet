package com.github.nathandekeyrel.kismet.friendship;

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
public class FriendshipController {

    private final UserRepository userRepository;
    private final FriendshipService friendshipService;

    public FriendshipController(UserRepository userRepository, FriendshipService friendshipService) {
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/friends")
    public String showFriendsPage(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Friendship> pendingRequests = friendshipService.getFriendRequests(currentUser);
        List<Friendship> acceptedFriends = friendshipService.getFriends(currentUser);

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        friendshipService.acceptFriendRequest(friendshipId, currentUser);

        return "redirect:/friends";
    }

    @PostMapping("/friends/decline")
    public String declineFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        friendshipService.declineFriendRequest(friendshipId, currentUser);

        return "redirect:/friends";
    }

    @GetMapping("/friends/search")
    public String searchFriends(@RequestParam("query") String query, Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<User> searchResults = friendshipService.searchUsers(query, currentUser);

        model.addAttribute("searchResults", searchResults);
        List<Friendship> pendingRequests = friendshipService.getFriendRequests(currentUser);
        List<Friendship> acceptedFriends = friendshipService.getFriends(currentUser);

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }

    @PostMapping("/friends/add")
    public String addFriend(@RequestParam("addresseeId") Long addresseeId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        User targetUser = userRepository.findById(addresseeId)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        String result = friendshipService.addFriend(currentUser, targetUser);

        if (result.equals("already_exists")) {
            return "redirect:/friends?error=" + result;
        }
        return "redirect:/friends?success=" + result;
    }
}
