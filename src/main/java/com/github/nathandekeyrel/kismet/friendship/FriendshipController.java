package com.github.nathandekeyrel.kismet.friendship;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import com.github.nathandekeyrel.kismet.user.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class FriendshipController {

    private final UserService userService;
    private final FriendshipService friendshipService;

    public FriendshipController(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/friends")
    public String showFriendsPage(Model model, Principal principal) {
        String email = principal.getName();
        User currentUser = userService.getUser(email);

        List<Friendship> pendingRequests = friendshipService.getFriendRequests(currentUser);
        List<Friendship> acceptedFriends = friendshipService.getFriends(currentUser);

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        String email = principal.getName();
        User currentUser = userService.getUser(email);

        friendshipService.acceptFriendRequest(friendshipId, currentUser);

        return "redirect:/friends";
    }

    @PostMapping("/friends/decline")
    public String declineFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        String email = principal.getName();
        User currentUser = userService.getUser(email);

        friendshipService.declineFriendRequest(friendshipId, currentUser);

        return "redirect:/friends";
    }

    @GetMapping("/friends/search")
    public String searchFriends(@RequestParam("query") String query, Model model, Principal principal) {
        String email = principal.getName();
        User currentUser = userService.getUser(email);

        List<User> searchResults = friendshipService.searchUsers(query, currentUser);
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
        String email = principal.getName();
        User currentUser = userService.getUser(email);

        User targetUser = userService.getUserById(addresseeId);

        String result = friendshipService.addFriend(currentUser, targetUser);

        if (result.equals("already_exists")) {
            return "redirect:/friends?error=" + result;
        }
        return "redirect:/friends?success=" + result;
    }
}
