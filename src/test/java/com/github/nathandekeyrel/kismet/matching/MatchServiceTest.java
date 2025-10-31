package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

        when(userRepository.findRandomUserNotInteractedWith(1L)).thenReturn(Optional.of(expectedMatch));

        Optional<User> actualMatch = matchService.findPotentialMatch(currentUser);

        assertTrue(actualMatch.isPresent());
        assertEquals(expectedMatch, actualMatch.get());

        verify(userRepository, times(1)).findRandomUserNotInteractedWith(1L);
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
}
