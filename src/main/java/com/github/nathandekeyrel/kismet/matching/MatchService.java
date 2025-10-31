package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private final UserRepository userRepository;
    private final MatchActionRepository matchActionRepository;
    private final MutualMatchRepository mutualMatchRepository;

    public MatchService(UserRepository userRepository, MatchActionRepository matchActionRepository, MutualMatchRepository mutualMatchRepository) {
        this.userRepository = userRepository;
        this.matchActionRepository = matchActionRepository;
        this.mutualMatchRepository = mutualMatchRepository;
    }

    public Optional<User> findPotentialMatch(User currentUser) {
        return userRepository.findRandomUserNotInteractedWith(currentUser.getId());
    }

    @Transactional
    public void recordAction(User actor, User target, ActionType action) {
        MatchAction matchAction = new MatchAction();
        matchAction.setUser(actor);
        matchAction.setTarget(target);
        matchAction.setAction(action);
        matchActionRepository.save(matchAction);

        if (action == ActionType.LIKE) {
            checkForMutualMatch(actor, target);
        }
    }

    private void checkForMutualMatch(User user1, User user2) {
        Optional<MatchAction> otherUsersAction = matchActionRepository.findByUserAndTarget(user2, user1);

        if (otherUsersAction.isPresent() && otherUsersAction.get().getAction() == ActionType.LIKE) {
            Optional<MutualMatch> existingMatch = mutualMatchRepository.findMatchBetween(user1, user2);
            if (existingMatch.isEmpty()) {
                MutualMatch mutualMatch = new MutualMatch();
                mutualMatch.setUser1(user1);
                mutualMatch.setUser2(user2);
                mutualMatchRepository.save(mutualMatch);
            }
        }
    }

    public List<User> getMatchedUsersFor(User currentUser) {
        List<MutualMatch> matches = mutualMatchRepository.findByUser1OrUser2(currentUser, currentUser);

        List<User> filteredMatches = matches.stream()
                .map(match -> match.getUser1().equals(currentUser) ? match.getUser2() : match.getUser1())
                .toList();

        return filteredMatches;
    }

}
