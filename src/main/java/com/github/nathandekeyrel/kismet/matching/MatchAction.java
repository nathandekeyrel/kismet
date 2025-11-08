package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.common.Model;
import com.github.nathandekeyrel.kismet.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "match_actions")
public class MatchAction extends Model {
    @ManyToOne
    @JoinColumn(name = "actor_id",  nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "target_id",  nullable = false)
    @ToString.Exclude
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;
}
