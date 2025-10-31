package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MatchActionRepository matchActionRepository;

    @MockitoBean
    private MutualMatchRepository mutualMatchRepository;

    @Test
    @WithMockUser
    void showMatchDeck_whenMatchFound_thenShowMatch() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setFirstName("target");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(matchService.findPotentialMatch(currentUser)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("potentialMatch"))
                .andExpect(content().string(containsString("target")));
    }

    @Test
    @WithMockUser
    void showMatchDeck_whenNoMatchFound_thenShowMessage() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(matchService.findPotentialMatch(currentUser)).thenReturn(Optional.empty());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeDoesNotExist("potentialMatch"))
                .andExpect(content().string(containsString("No new people right now...")));
    }

    @Test
    void whenUnauthenticated_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/matches"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "user")
    void likeUser_whenAuthenticated_thenRecordsLikeAndRedirects() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        User targetUser = new User();
        targetUser.setId(2L);

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(post("/home/like")
                        .param("targetId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(matchService, times(1)).recordAction(currentUser, targetUser, ActionType.LIKE);
    }

    @Test
    @WithMockUser(username = "user")
    void passUser_whenAuthenticated_thenRecordsPassAndRedirects() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        User targetUser = new User();
        targetUser.setId(2L);

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(post("/home/pass")
                        .param("targetId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(matchService, times(1)).recordAction(currentUser, targetUser, ActionType.PASS);
    }

    @Test
    @WithMockUser(username = "user")
    void showMatchesPage_whenUserHasMatches_thenDisplaysMatches() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        User match1 = new User();
        match1.setId(2L);
        match1.setFirstName("Alice");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(matchService.getMatchedUsersFor(currentUser)).thenReturn(List.of(match1));

        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andExpect(view().name("matches"))
                .andExpect(model().attributeExists("matches"))
                .andExpect(content().string(containsString("Alice")));
    }

    @Test
    @WithMockUser
    void showMatchesPage_whenUserHasNoMatches_thenDisplaysMessage() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user");

        when(userRepository.findByEmail("user")).thenReturn(Optional.of(currentUser));
        when(matchService.getMatchedUsersFor(currentUser)).thenReturn(List.of());

        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andExpect(view().name("matches"))
                .andExpect(content().string(containsString("You have no matches...")));
    }
}
