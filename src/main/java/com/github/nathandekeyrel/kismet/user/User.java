package com.github.nathandekeyrel.kismet.user;

import com.github.nathandekeyrel.kismet.common.Model;
import com.github.nathandekeyrel.kismet.friendship.Friendship;
import com.github.nathandekeyrel.kismet.profile.Profile;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User extends Model {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToMany(mappedBy = "requester")
    private Set<Friendship> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "addressee")
    private Set<Friendship> receivedFriendRequests = new HashSet<>();
}
