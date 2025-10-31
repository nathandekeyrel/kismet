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

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void showMatchDeck_whenNoMatchFound_thenShowMMessage() throws Exception {
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

}
