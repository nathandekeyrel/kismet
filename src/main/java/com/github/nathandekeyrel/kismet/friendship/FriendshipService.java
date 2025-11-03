package com.github.nathandekeyrel.kismet.friendship;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public List<Friendship> getFriendRequests(User currentUser) {
        return friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING);
    }

    public List<Friendship> getFriends(User currentUser) {
        return friendshipRepository.findAcceptedFriendships(currentUser);
    }

    public void acceptFriendRequest(Long friendshipId, User currentUser)  {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid friendship ID"));

        if (friendship.getAddressee().getId().equals(currentUser.getId())) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        }
    }

    public void declineFriendRequest(Long friendshipId, User currentUser)  {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid friendship ID"));

        if (friendship.getAddressee().getId().equals(currentUser.getId())) {
            friendshipRepository.delete(friendship);
        }
    }

    public List<User> searchUsers(String query, User currentUser) {
        List<User> results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);

        return results.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .toList();
    }

    public String addFriend(User currentUser, User targetUser) {
        Optional<Friendship> existingFriendshipOpt = friendshipRepository.findFriendshipBetween(currentUser, targetUser);

        if (existingFriendshipOpt.isPresent()) {
            Friendship existingFriendship = existingFriendshipOpt.get();

            if (existingFriendship.getStatus().equals(FriendshipStatus.PENDING) &&
                    existingFriendship.getAddressee().getId().equals(currentUser.getId())) {

                existingFriendship.setStatus(FriendshipStatus.ACCEPTED);
                friendshipRepository.save(existingFriendship);

                return "accepted";
            }
            return "already_exists";
        } else {
            Friendship newRequest = new Friendship();
            newRequest.setRequester(currentUser);
            newRequest.setAddressee(targetUser);
            newRequest.setStatus(FriendshipStatus.PENDING);
            friendshipRepository.save(newRequest);

            return "sent";
        }
    }
}
