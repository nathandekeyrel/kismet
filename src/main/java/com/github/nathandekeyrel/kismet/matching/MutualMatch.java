package com.github.nathandekeyrel.kismet.matching;

import com.github.nathandekeyrel.kismet.common.Model;
import com.github.nathandekeyrel.kismet.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "mutual_matches")
public class MutualMatch extends Model {
    @ManyToOne
    @JoinColumn(name = "user_one_id", nullable = false)
    @ToString.Exclude
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user_two_id", nullable = false)
    @ToString.Exclude
    private User user2;
}
