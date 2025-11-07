package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.common.Model;
import com.github.nathandekeyrel.kismet.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "profiles")
public class Profile extends Model {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String bio;

    @OneToMany(mappedBy = "profile")
    private List<ProfileAnswer> answers = new ArrayList<>();
}
