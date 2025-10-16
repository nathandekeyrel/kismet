package com.github.nathandekeyrel.kismet;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "prompts")
public class Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private PromptSection section;
}
