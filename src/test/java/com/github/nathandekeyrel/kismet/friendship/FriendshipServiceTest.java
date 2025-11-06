package com.github.nathandekeyrel.kismet.friendship;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendshipService friendshipService;

    private User requester;
    private User addressee;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setEmail("requester@test.com");

        addressee = new User();
        addressee.setId(2L);
        addressee.setEmail("addressee@test.com");

        friendship = new Friendship();
        friendship.setId(99L);
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
    }

    @Test
    void whenAcceptingFriendRequest_asAddressee_thenStatusIsAccepted() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(friendship));

        friendshipService.acceptFriendRequest(99L, addressee);

        verify(friendshipRepository).save(friendship);
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
    }

    @Test
    void whenAcceptingFriendRequest_asNonAddressee_thenNoChange() {
        User randomUser = new User();
        randomUser.setId(3L);
        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(friendship));

        friendshipService.acceptFriendRequest(99L, randomUser);

        verify(friendshipRepository, never()).save(any());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
    }

    @Test
    void whenAcceptingFriendRequest_withInvalidId_thenThrowsException() {
        when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            friendshipService.acceptFriendRequest(999L, addressee);
        });
    }

    @Test
    void whenDecliningFriendRequest_asAddressee_thenFriendshipIsDeleted() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(friendship));

        friendshipService.declineFriendRequest(99L, addressee);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void whenDecliningFriendRequest_asNonAddressee_thenNoChange() {
        User randomUser = new User();
        randomUser.setId(3L);
        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(friendship));

        friendshipService.declineFriendRequest(99L, randomUser);

        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void whenDecliningFriendRequest_withInvalidId_thenThrowsException() {
        when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            friendshipService.declineFriendRequest(999L, addressee);
        });
    }

    @Test
    void whenGettingFriendRequests_thenReturnsPendingRequests() {
        List<Friendship> pendingRequests = List.of(friendship);
        when(friendshipRepository.findByAddresseeAndStatus(addressee, FriendshipStatus.PENDING))
                .thenReturn(pendingRequests);

        List<Friendship> result = friendshipService.getFriendRequests(addressee);

        assertEquals(1, result.size());
        assertEquals(friendship, result.get(0));
        verify(friendshipRepository).findByAddresseeAndStatus(addressee, FriendshipStatus.PENDING);
    }

    @Test
    void whenGettingFriends_thenReturnsAcceptedFriendships() {
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        List<Friendship> acceptedFriends = List.of(friendship);
        when(friendshipRepository.findAcceptedFriendships(requester))
                .thenReturn(acceptedFriends);

        List<Friendship> result = friendshipService.getFriends(requester);

        assertEquals(1, result.size());
        assertEquals(friendship, result.get(0));
        verify(friendshipRepository).findAcceptedFriendships(requester);
    }

    @Test
    void whenSearchingUsers_thenReturnsMatchingUsersExcludingCurrentUser() {
        User user1 = new User();
        user1.setId(10L);
        user1.setFirstName("John");
        user1.setLastName("Doe");

        User user2 = new User();
        user2.setId(11L);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");

        when(userService.getByFirstOrLastName("doe"))
                .thenReturn(List.of(requester, user1, user2));

        List<User> result = friendshipService.searchUsers("doe", requester);

        assertEquals(2, result.size());
        assertFalse(result.contains(requester));
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userService).getByFirstOrLastName("doe");
    }

    @Test
    void whenAddingFriend_withNoExistingFriendship_thenCreatesPendingRequest() {
        when(friendshipRepository.findFriendshipBetween(requester, addressee))
                .thenReturn(Optional.empty());

        String result = friendshipService.addFriend(requester, addressee);

        assertEquals("sent", result);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void whenAddingFriend_withPendingRequestFromTarget_thenAcceptsRequest() {
        when(friendshipRepository.findFriendshipBetween(requester, addressee))
                .thenReturn(Optional.of(friendship));

        String result = friendshipService.addFriend(requester, addressee);

        assertEquals("already_exists", result);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void whenAddingFriend_withPendingRequestFromTargetToCurrentUser_thenAcceptsRequest() {
        when(friendshipRepository.findFriendshipBetween(addressee, requester))
                .thenReturn(Optional.of(friendship));

        String result = friendshipService.addFriend(addressee, requester);

        assertEquals("accepted", result);
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void whenAddingFriend_withExistingAcceptedFriendship_thenReturnsAlreadyExists() {
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findFriendshipBetween(requester, addressee))
                .thenReturn(Optional.of(friendship));

        String result = friendshipService.addFriend(requester, addressee);

        assertEquals("already_exists", result);
        verify(friendshipRepository, never()).save(any());
    }
}