package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MatchActionRepository matchActionRepository;

    @Mock
    private MutualMatchRepository mutualMatchRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void findPotentialMatch_shouldCallRepositoryAndReturnUser() {
        User currentUser = new User();
        currentUser.setId(1L);

        User expectedMatch = new User();
        expectedMatch.setId(2L);

        when(userService.getRandomUser(currentUser)).thenReturn(Optional.of(expectedMatch));

        Optional<User> actualMatch = matchService.findPotentialMatch(currentUser);

        assertTrue(actualMatch.isPresent());
        assertEquals(expectedMatch, actualMatch.get());

        verify(userService, times(1)).getRandomUser(currentUser);
    }

    @Test
    void recordAction_whenActionIsPass_thenSavesPassAction() {
        User actor = new User();
        actor.setId(1L);
        User target = new User();
        target.setId(2L);

        ArgumentCaptor<MatchAction> matchActionCaptor = ArgumentCaptor.forClass(MatchAction.class);

        matchService.recordAction(actor, target, ActionType.PASS);

        verify(matchActionRepository, times(1)).save(matchActionCaptor.capture());

        MatchAction savedAction = matchActionCaptor.getValue();
        assertEquals(actor, savedAction.getUser());
        assertEquals(target, savedAction.getTarget());
        assertEquals(ActionType.PASS, savedAction.getAction());

        verify(mutualMatchRepository, never()).save(any(MutualMatch.class));
    }

    @Test
    void recordAction_whenActionIsLikeAndNoMutualMatch_thenSavesLikeAndChecks() {
        User actor = new User();
        actor.setId(1L);
        User target = new User();
        target.setId(2L);

        ArgumentCaptor<MatchAction> matchActionCaptor = ArgumentCaptor.forClass(MatchAction.class);

        when(matchActionRepository.findByUserAndTarget(target, actor)).thenReturn(Optional.empty());

        matchService.recordAction(actor, target, ActionType.LIKE);

        verify(matchActionRepository, times(1)).save(matchActionCaptor.capture());
        assertEquals(ActionType.LIKE, matchActionCaptor.getValue().getAction());

        verify(matchActionRepository, times(1)).findByUserAndTarget(target, actor);

        verify(mutualMatchRepository, never()).save(any(MutualMatch.class));
    }

    @Test
    void recordAction_whenActionIsLikeAndMutualMatchExists_thenSavesLikeAndCreatesMutualMatch() {
        User actor = new User();
        actor.setId(1L);
        User target = new User();
        target.setId(2L);

        ArgumentCaptor<MutualMatch> mutualMatchCaptor = ArgumentCaptor.forClass(MutualMatch.class);

        MatchAction otherUsersLikeAction = new MatchAction();
        otherUsersLikeAction.setUser(target);
        otherUsersLikeAction.setTarget(actor);
        otherUsersLikeAction.setAction(ActionType.LIKE);
        when(matchActionRepository.findByUserAndTarget(target, actor)).thenReturn(Optional.of(otherUsersLikeAction));

        matchService.recordAction(actor, target, ActionType.LIKE);

        verify(matchActionRepository, times(1)).findByUserAndTarget(target, actor);

        verify(mutualMatchRepository, times(1)).save(mutualMatchCaptor.capture());

        MutualMatch savedMutualMatch = mutualMatchCaptor.getValue();
        assertEquals(actor, savedMutualMatch.getUser1());
        assertEquals(target, savedMutualMatch.getUser2());
    }

    @Test
    void getMatchedUsersFor_shouldReturnOtherUsersFromMatches() {
        User currentUser = new User();
        currentUser.setId(1L);

        User match1 = new User();
        match1.setId(2L);
        match1.setFirstName("Alice");

        User match2 = new User();
        match2.setId(3L);
        match2.setFirstName("Bob");

        MutualMatch mutualMatch1 = new MutualMatch();
        mutualMatch1.setUser1(currentUser);
        mutualMatch1.setUser2(match1);

        MutualMatch mutualMatch2 = new MutualMatch();
        mutualMatch2.setUser1(match2);
        mutualMatch2.setUser2(currentUser);

        when(mutualMatchRepository.findByUser1OrUser2(currentUser, currentUser))
                .thenReturn(List.of(mutualMatch1, mutualMatch2));

        List<User> result = matchService.getMatchedUsersFor(currentUser);

        assertEquals(2, result.size());
        assertTrue(result.contains(match1));
        assertTrue(result.contains(match2));
        assertFalse(result.contains(currentUser));
    }

    @Test
    void getMatchedUsersFor_whenNoMatches_shouldReturnEmptyList() {
        User currentUser = new User();
        currentUser.setId(1L);

        when(mutualMatchRepository.findByUser1OrUser2(currentUser, currentUser))
                .thenReturn(List.of());

        List<User> result = matchService.getMatchedUsersFor(currentUser);

        assertTrue(result.isEmpty());
    }
}