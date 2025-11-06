package com.github.nathandekeyrel.kismet.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    void whenGettingUser_withValidEmail_thenReturnsUser() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getUser("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void whenGettingUser_withInvalidEmail_thenThrowsException() {
        when(userRepository.findByEmail("invalid@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUser("invalid@example.com"));
        verify(userRepository).findByEmail("invalid@example.com");
    }

    @Test
    void whenGettingUser_withNullEmail_thenThrowsException() {
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUser(null));
        verify(userRepository).findByEmail(null);
    }

    @Test
    void whenSavingUser_thenCallsRepository() {
        when(userRepository.save(user)).thenReturn(user);

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void whenSavingUser_thenRepositoryReturnsUser() {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");

        when(userRepository.save(user)).thenReturn(savedUser);

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void whenSavingMultipleUsers_thenCallsRepositoryForEach() {
        User user2 = new User();
        user2.setEmail("another@example.com");

        userService.save(user);
        userService.save(user2);

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void whenGettingUser_multipleTimesWithSameEmail_thenCallsRepositoryEachTime() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        userService.getUser("test@example.com");
        userService.getUser("test@example.com");
        userService.getUser("test@example.com");

        verify(userRepository, times(3)).findByEmail("test@example.com");
    }
}