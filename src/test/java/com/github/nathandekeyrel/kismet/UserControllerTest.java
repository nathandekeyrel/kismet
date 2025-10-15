package com.github.nathandekeyrel.kismet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void whenAuthenticated_thenReturnsHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Welcome to Kismet!")));
    }

    @Test
    void whenUnauthenticated_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void whenRegisteringNewUser_thenSavesUserAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                .param("email", "newuser@example.com")
                .param("password", "12345")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenRegisteringDuplicateUser_thenReturnsRegisterPageWithError() throws Exception {
        String duplicateEmail = "test@example.com";
        when(userRepository.findByEmail(duplicateEmail)).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/register")
                        .param("email", duplicateEmail)
                        .param("password", "12345")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void whenAuthenticated_thenReturnsProfilePage() throws Exception {
        String mockUserEmail = "user";
        User mockUser = new User();
        mockUser.setEmail(mockUserEmail);

        when(userRepository.findByEmail(mockUserEmail)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void whenUnauthenticated_thenRedirectsFromProfileToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}