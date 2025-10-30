package com.github.nathandekeyrel.kismet.profile;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ProfileEditForm {
    private String bio;

    private Map<Long, String> answers = new HashMap<>();
}
