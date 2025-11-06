package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProfileService profileService;

    @Test
    @WithMockUser(username = "user@test.com")
    void whenAuthenticated_thenReturnsProfilePage() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        when(userService.getUser("user@test.com")).thenReturn(mockUser);
        when(profileService.getByUser(mockUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("answers"));

        verify(userService).getUser("user@test.com");
        verify(profileService).getByUser(mockUser);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenAuthenticated_thenReturnsProfileEditPage() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@test.com");
        mockUser.setBio("Existing bio");

        PromptSection section = new PromptSection();
        section.setId(1L);
        section.setTitle("About Me");

        Prompt prompt = new Prompt();
        prompt.setId(1L);
        prompt.setText("What's your hobby?");

        ProfileAnswer existingAnswer = new ProfileAnswer();
        existingAnswer.setPrompt(prompt);
        existingAnswer.setAnswerText("Coding");

        when(userService.getUser("user@test.com")).thenReturn(mockUser);
        when(profileService.getAll()).thenReturn(List.of(section));
        when(profileService.getByUser(mockUser)).thenReturn(List.of(existingAnswer));

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("sections"))
                .andExpect(model().attributeExists("profileForm"));

        verify(userService).getUser("user@test.com");
        verify(profileService).getAll();
        verify(profileService).getByUser(mockUser);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenEditingProfile_thenUpdatesUserAndRedirects() throws Exception {
        String newBio = "This is my new, updated bio.";
        String promptAnswer = "Yes";

        User originalUser = new User();
        originalUser.setEmail("user@test.com");

        Prompt originalPrompt = new Prompt();
        originalPrompt.setId(101L);
        originalPrompt.setText("What was the last prompt you made?");

        ProfileAnswer answer = new ProfileAnswer();

        when(userService.getUser("user@test.com")).thenReturn(originalUser);
        when(profileService.getPrompt(101L)).thenReturn(originalPrompt);
        when(profileService.getProfileAnswer(originalUser, originalPrompt)).thenReturn(answer);

        mockMvc.perform(post("/profile/edit")
                        .param("bio", newBio)
                        .param("answers[101]", promptAnswer)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(newBio, savedUser.getBio());

        ArgumentCaptor<ProfileAnswer> profileAnswerCaptor = ArgumentCaptor.forClass(ProfileAnswer.class);
        verify(profileService).save(profileAnswerCaptor.capture());
        ProfileAnswer savedProfileAnswer = profileAnswerCaptor.getValue();

        assertEquals(promptAnswer, savedProfileAnswer.getAnswerText());
        assertEquals(originalUser, savedProfileAnswer.getUser());
        assertEquals(originalPrompt, savedProfileAnswer.getPrompt());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenViewingProfile_thenDisplaysAnswers() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");
        user.setFirstName("Jane");

        Prompt prompt = new Prompt();
        prompt.setText("My favorite hobby is...");

        ProfileAnswer answer = new ProfileAnswer();
        answer.setPrompt(prompt);
        answer.setAnswerText("Rock climbing");

        when(userService.getUser("user@test.com")).thenReturn(user);
        when(profileService.getByUser(user)).thenReturn(List.of(answer));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(content().string(containsString("Jane")))
                .andExpect(content().string(containsString("Rock climbing")));
    }
}