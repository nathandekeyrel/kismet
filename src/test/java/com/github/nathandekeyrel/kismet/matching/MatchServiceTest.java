package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void findPotentialMatch_shouldCallRepositoryAndReturnUser() {
        User currentUser = new User();
        currentUser.setId(1L);

        User expectedMatch = new User();
        expectedMatch.setId(2L);

        when(userRepository.findRandomUserNotInteractedWith(1L)).thenReturn(Optional.of(expectedMatch));

        Optional<User> actualMatch = matchService.findPotentialMatch(currentUser);

        assertTrue(actualMatch.isPresent());
        assertEquals(expectedMatch, actualMatch.get());

        verify(userRepository, times(1)).findRandomUserNotInteractedWith(1L);
    }
}
