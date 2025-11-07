package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import com.github.nathandekeyrel.kismet.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final ProfileService profileService;

    public ProfileController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);
        Profile profile = profileService.getByUser(currentUser);
        List<ProfileAnswer> answers = profileService.getAnswersByProfile(profile);

        model.addAttribute("user", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("answers", answers);

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showProfileEdit(Model model, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);
        Profile profile = profileService.getByUser(currentUser);

        model.addAttribute("promptTypes", PromptType.values());
        model.addAttribute("profileForm", profileService.buildEditForm(profile));

        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String processProfileEdit(@ModelAttribute("profileForm") ProfileEditForm profileForm, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);
        Profile profile = profileService.getByUser(currentUser);

        profileService.updateProfile(profile, profileForm.getBio(), profileForm.getAnswers());

        return "redirect:/profile";
    }
}
