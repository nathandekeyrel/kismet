package com.github.nathandekeyrel.kismet.user;

import com.github.nathandekeyrel.kismet.profile.ProfileAnswerRepository;
import com.github.nathandekeyrel.kismet.profile.PromptRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProfileAnswerRepository profileAnswerRepository;

    @MockitoBean
    private PromptRepository promptRepository;

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
}
