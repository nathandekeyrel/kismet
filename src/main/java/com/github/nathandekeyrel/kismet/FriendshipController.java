package com.github.nathandekeyrel.kismet;

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
        List<Friendship> acceptedFriends = friendshipRepository.findAcceptedFriendships(currentUser);

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid friendship ID"));

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (friendship.getAddressee().getId().equals(currentUser.getId())) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        }

        return "redirect:/friends";
    }

    @PostMapping("/friends/decline")
    public String declineFriendRequest(@RequestParam("friendshipId") Long friendshipId, Principal principal) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid friendship ID"));

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (friendship.getAddressee().getId().equals(currentUser.getId())) {
            friendshipRepository.delete(friendship);
        }

        return "redirect:/friends";
    }

    @GetMapping("/friends/search")
    public String searchFriends(@RequestParam("query") String query, Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<User> results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);

        List<User> filteredResults = results.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .toList();

        model.addAttribute("searchResults", filteredResults);

        model.addAttribute("pendingRequests", friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING));
        model.addAttribute("acceptedFriends", friendshipRepository.findAcceptedFriendships(currentUser));
        model.addAttribute("currentUser", currentUser);

        return "friends";
    }

    @PostMapping("/friends/add")
    public String addFriend(@RequestParam("addresseeId") Long addresseeId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        User targetUser = userRepository.findById(addresseeId)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        Optional<Friendship> existingFriendshipOpt = friendshipRepository.findFriendshipBetween(currentUser, targetUser);

        if (existingFriendshipOpt.isPresent()) {
            Friendship existingFriendship = existingFriendshipOpt.get();

            if (existingFriendship.getStatus().equals(FriendshipStatus.PENDING) &&
                    existingFriendship.getAddressee().getId().equals(currentUser.getId())) {

                existingFriendship.setStatus(FriendshipStatus.ACCEPTED);
                friendshipRepository.save(existingFriendship);

                return "redirect:/friends?success=accepted";
            }
            return "redirect:/friends?error=already_exists";
        } else {
            Friendship newRequest = new Friendship();
            newRequest.setRequester(currentUser);
            newRequest.setAddressee(targetUser);
            newRequest.setStatus(FriendshipStatus.PENDING);
            friendshipRepository.save(newRequest);

            return "redirect:/friends?success=sent";
        }
    }
}
