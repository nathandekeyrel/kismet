package com.github.nathandekeyrel.kismet.profile;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ProfileEditForm {
    private String bio;
    private Map<PromptType, String> answers = new LinkedHashMap<>();
}
