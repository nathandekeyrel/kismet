package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileAnswerRepository profileAnswerRepository;

    @Mock
    private PromptSectionRepository promptSectionRepository;

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private Prompt prompt;
    private ProfileAnswer profileAnswer;
    private PromptSection section;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        section = new PromptSection();
        section.setId(1L);
        section.setTitle("About Me");

        prompt = new Prompt();
        prompt.setId(1L);
        prompt.setText("What's your hobby?");
        prompt.setSection(section);

        profileAnswer = new ProfileAnswer();
        profileAnswer.setId(1L);
        profileAnswer.setUser(user);
        profileAnswer.setPrompt(prompt);
        profileAnswer.setAnswerText("Coding");
    }

    @Test
    void whenGettingByUser_thenReturnsUserAnswers() {
        when(profileAnswerRepository.findByUser(user))
                .thenReturn(List.of(profileAnswer));

        List<ProfileAnswer> result = profileService.getByUser(user);

        assertEquals(1, result.size());
        assertEquals(profileAnswer, result.getFirst());
        verify(profileAnswerRepository).findByUser(user);
    }

    @Test
    void whenGettingByUser_withNoAnswers_thenReturnsEmptyList() {
        when(profileAnswerRepository.findByUser(user))
                .thenReturn(List.of());

        List<ProfileAnswer> result = profileService.getByUser(user);

        assertTrue(result.isEmpty());
        verify(profileAnswerRepository).findByUser(user);
    }

    @Test
    void whenSavingAnswer_thenCallsRepository() {
        profileService.save(profileAnswer);

        verify(profileAnswerRepository).save(profileAnswer);
    }

    @Test
    void whenGettingPrompt_thenReturnsPrompt() {
        when(promptRepository.findById(1L))
                .thenReturn(Optional.of(prompt));

        Prompt result = profileService.getPrompt(1L);

        assertEquals(prompt, result);
        assertEquals("What's your hobby?", result.getText());
        verify(promptRepository).findById(1L);
    }

    @Test
    void whenGettingPrompt_withInvalidId_thenThrowsException() {
        when(promptRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> profileService.getPrompt(999L));
        verify(promptRepository).findById(999L);
    }

    @Test
    void whenGettingProfileAnswer_andExists_thenReturnsExisting() {
        when(profileAnswerRepository.findByUserAndPrompt(user, prompt))
                .thenReturn(Optional.of(profileAnswer));

        ProfileAnswer result = profileService.getProfileAnswer(user, prompt);

        assertEquals(profileAnswer, result);
        assertEquals("Coding", result.getAnswerText());
        verify(profileAnswerRepository).findByUserAndPrompt(user, prompt);
    }

    @Test
    void whenGettingProfileAnswer_andDoesNotExist_thenReturnsNew() {
        when(profileAnswerRepository.findByUserAndPrompt(user, prompt))
                .thenReturn(Optional.empty());

        ProfileAnswer result = profileService.getProfileAnswer(user, prompt);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getAnswerText());
        verify(profileAnswerRepository).findByUserAndPrompt(user, prompt);
    }

    @Test
    void whenGettingAllSections_thenReturnsAllSections() {
        PromptSection section2 = new PromptSection();
        section2.setId(2L);
        section2.setTitle("My Interests");

        when(promptSectionRepository.findAll())
                .thenReturn(List.of(section, section2));

        List<PromptSection> result = profileService.getAll();

        assertEquals(2, result.size());
        assertEquals("About Me", result.get(0).getTitle());
        assertEquals("My Interests", result.get(1).getTitle());
        verify(promptSectionRepository).findAll();
    }

    @Test
    void whenGettingAllSections_withNoSections_thenReturnsEmptyList() {
        when(promptSectionRepository.findAll())
                .thenReturn(List.of());

        List<PromptSection> result = profileService.getAll();

        assertTrue(result.isEmpty());
        verify(promptSectionRepository).findAll();
    }

    @Test
    void whenSavingMultipleAnswers_thenCallsRepositoryForEach() {
        ProfileAnswer answer2 = new ProfileAnswer();
        answer2.setUser(user);
        answer2.setAnswerText("Another answer");

        profileService.save(profileAnswer);
        profileService.save(answer2);

        verify(profileAnswerRepository, times(2)).save(any(ProfileAnswer.class));
    }
}