package com.github.nathandekeyrel.kismet.profile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "prompt_section")
public class PromptSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @OneToMany(mappedBy = "section")
    @ToString.Exclude
    private List<Prompt> prompts = new ArrayList<>();
}
