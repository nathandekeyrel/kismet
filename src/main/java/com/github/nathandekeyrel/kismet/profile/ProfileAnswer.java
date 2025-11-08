package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.common.Model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "profile_answers", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "prompt_type"}))
public class ProfileAnswer extends Model {
    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptType promptType;

    @Column(nullable = false, length = 500)
    private String answerText;
}
