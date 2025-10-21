package com.github.nathandekeyrel.kismet;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column
    private String bio;

    @OneToMany(mappedBy = "requester")
    private Set<Friendship> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "addressee")
    private Set<Friendship> receivedFriendRequests = new HashSet<>();
}
