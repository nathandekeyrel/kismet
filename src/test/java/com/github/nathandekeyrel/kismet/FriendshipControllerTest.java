package com.github.nathandekeyrel.kismet;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FriendshipRepository friendshipRepository;

    @Test
    @WithMockUser
    void whenAuthenticated_thenReturnsFriendsPage() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(mockUser));
        when(friendshipRepository.findByAddresseeAndStatus(any(User.class), any(FriendshipStatus.class)))
                .thenReturn(Collections.emptyList());
        when(friendshipRepository.findAcceptedFriendships(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(content().string(containsString("My Friends")));
    }

    @Test
    void whenUnauthenticated_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/friends"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    // test search
    @Test
    @WithMockUser("current")
    void whenSearch_thenDisplaysList() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");
        currentUser.setFirstName("Current");
        currentUser.setLastName("User");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("target");
        targetUser.setFirstName("Target");
        targetUser.setLastName("User");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("target", "target"))
                .thenReturn(Collections.singletonList(targetUser));

        when(friendshipRepository.findByAddresseeAndStatus(any(User.class), any(FriendshipStatus.class)))
                .thenReturn(Collections.emptyList());
        when(friendshipRepository.findAcceptedFriendships(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/friends/search").param("query", "target"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("searchResults"))
                .andExpect(content().string(containsString("Target User")));
    }

    @Test
    @WithMockUser("current")
    void whenSendingFriendRequest_thenCreatesPendingFriendship() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("target");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        when(friendshipRepository.findFriendshipBetween(currentUser, targetUser)).thenReturn(Optional.empty());

        mockMvc.perform(post("/friends/add")
                        .param("addresseeId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends?success=sent"));

        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();

        assertNotNull(savedFriendship);
        assertEquals(currentUser.getId(), savedFriendship.getRequester().getId());
        assertEquals(targetUser.getId(), savedFriendship.getAddressee().getId());
        assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }

    @Test
    @WithMockUser("addressee")
    void whenAcceptingFriendRequest_thenStatusIsAccepted() throws Exception {
        User requesterUser = new User();
        requesterUser.setId(1L);
        requesterUser.setEmail("requester");

        User addresseeUser = new User();
        addresseeUser.setId(2L);
        addresseeUser.setEmail("addressee");

        Friendship existingFriendship = new Friendship();
        existingFriendship.setId(99L);
        existingFriendship.setRequester(requesterUser);
        existingFriendship.setAddressee(addresseeUser);
        existingFriendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(existingFriendship));
        when(userRepository.findByEmail("addressee")).thenReturn(Optional.of(addresseeUser));

        mockMvc.perform(post("/friends/accept")
                        .param("friendshipId", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();

        assertEquals(FriendshipStatus.ACCEPTED, savedFriendship.getStatus());
        assertEquals(99L, savedFriendship.getId());
    }

    @Test
    @WithMockUser("addressee")
    void whenDecliningFriendRequest_thenFriendshipIsDeleted() throws Exception {
        User requesterUser = new User();
        requesterUser.setId(1L);
        requesterUser.setEmail("requester");

        User addresseeUser = new User();
        addresseeUser.setId(2L);
        addresseeUser.setEmail("addressee");

        Friendship existingFriendship = new Friendship();
        existingFriendship.setId(99L);
        existingFriendship.setRequester(requesterUser);
        existingFriendship.setAddressee(addresseeUser);
        existingFriendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(99L)).thenReturn(Optional.of(existingFriendship));
        when(userRepository.findByEmail("addressee")).thenReturn(Optional.of(addresseeUser));

        mockMvc.perform(post("/friends/decline")
                        .param("friendshipId", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendshipRepository).delete(existingFriendship);
    }

    @Test
    @WithMockUser("user2")
    void whenAddingFriend_andPendingRequestExistsFromTarget_thenAutoAcceptsRequest() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2");

        Friendship existingRequest = new Friendship();
        existingRequest.setId(99L);
        existingRequest.setRequester(user1);
        existingRequest.setAddressee(user2);
        existingRequest.setStatus(FriendshipStatus.PENDING);

        when(userRepository.findByEmail("user2")).thenReturn(Optional.of(user2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        when(friendshipRepository.findFriendshipBetween(user2, user1)).thenReturn(Optional.of(existingRequest));

        mockMvc.perform(post("/friends/add")
                        .param("addresseeId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends?success=accepted"));

        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();

        assertEquals(FriendshipStatus.ACCEPTED, savedFriendship.getStatus());
        assertEquals(99L, savedFriendship.getId());
    }
}
