package com.github.nathandekeyrel.kismet.friendship;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendshipController.class)
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FriendshipService friendshipService;

    @Test
    @WithMockUser
    void whenAuthenticated_thenReturnsFriendsPage() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(mockUser));
        when(friendshipService.getFriendRequests(mockUser)).thenReturn(Collections.emptyList());
        when(friendshipService.getFriends(mockUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("pendingRequests"))
                .andExpect(model().attributeExists("acceptedFriends"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(content().string(containsString("My Friends")));
    }

    @Test
    @WithMockUser("current")
    void whenSearchingFriends_thenDisplaysResults() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");
        currentUser.setFirstName("Current");
        currentUser.setLastName("User");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setFirstName("Target");
        targetUser.setLastName("User");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(friendshipService.searchUsers("target", currentUser))
                .thenReturn(List.of(targetUser));
        when(friendshipService.getFriendRequests(currentUser)).thenReturn(Collections.emptyList());
        when(friendshipService.getFriends(currentUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/friends/search").param("query", "target"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("searchResults"))
                .andExpect(model().attributeExists("pendingRequests"))
                .andExpect(model().attributeExists("acceptedFriends"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(content().string(containsString("Target User")));

        verify(friendshipService).searchUsers("target", currentUser);
    }

    @Test
    @WithMockUser("current")
    void whenAddingFriend_thenRedirectsWithSuccess() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("target");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(friendshipService.addFriend(currentUser, targetUser)).thenReturn("sent");

        mockMvc.perform(post("/friends/add")
                        .param("addresseeId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends?success=sent"));

        verify(friendshipService).addFriend(currentUser, targetUser);
    }

    @Test
    @WithMockUser("current")
    void whenAddingFriend_andAutoAccepts_thenRedirectsWithAccepted() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("target");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(friendshipService.addFriend(currentUser, targetUser)).thenReturn("accepted");

        mockMvc.perform(post("/friends/add")
                        .param("addresseeId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends?success=accepted"));

        verify(friendshipService).addFriend(currentUser, targetUser);
    }

    @Test
    @WithMockUser("current")
    void whenAddingFriend_andAlreadyExists_thenRedirectsWithError() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("target");

        when(userRepository.findByEmail("current")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(friendshipService.addFriend(currentUser, targetUser)).thenReturn("already_exists");

        mockMvc.perform(post("/friends/add")
                        .param("addresseeId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends?error=already_exists"));

        verify(friendshipService).addFriend(currentUser, targetUser);
    }

    @Test
    @WithMockUser("addressee")
    void whenAcceptingFriendRequest_thenRedirectsToFriends() throws Exception {
        User addresseeUser = new User();
        addresseeUser.setId(2L);
        addresseeUser.setEmail("addressee");

        when(userRepository.findByEmail("addressee")).thenReturn(Optional.of(addresseeUser));

        mockMvc.perform(post("/friends/accept")
                        .param("friendshipId", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendshipService).acceptFriendRequest(99L, addresseeUser);
    }

    @Test
    @WithMockUser("addressee")
    void whenDecliningFriendRequest_thenRedirectsToFriends() throws Exception {
        User addresseeUser = new User();
        addresseeUser.setId(2L);
        addresseeUser.setEmail("addressee");

        when(userRepository.findByEmail("addressee")).thenReturn(Optional.of(addresseeUser));

        mockMvc.perform(post("/friends/decline")
                        .param("friendshipId", "99")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/friends"));

        verify(friendshipService).declineFriendRequest(99L, addresseeUser);
    }
}