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
import java.util.Map;

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

        List<ProfileAnswer> answers = profileService.getByUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("answers", answers);

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showProfileEdit(Model model, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        List<PromptSection> sections = profileService.getAll();
        model.addAttribute("sections", sections);

        List<ProfileAnswer> existingAnswers = profileService.getByUser(currentUser);

        ProfileEditForm form = new ProfileEditForm();
        form.setBio(currentUser.getBio());

        for (ProfileAnswer answer : existingAnswers) {
            form.getAnswers().put(answer.getPrompt().getId(), answer.getAnswerText());
        }

        model.addAttribute("profileForm", form);

        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String processProfileEdit(User user, @ModelAttribute("profileForm") ProfileEditForm profileForm, Principal principal) {
        User currentUser = userService.getCurrentUser(principal);

        currentUser.setBio(user.getBio());
        userService.save(currentUser);

        for (Map.Entry<Long, String> entry : profileForm.getAnswers().entrySet()) {
            Long promptId = entry.getKey();
            String answerText = entry.getValue();

            Prompt prompt = profileService.getPrompt(promptId);

            ProfileAnswer answer = profileService.getProfileAnswer(currentUser, prompt);

            answer.setUser(currentUser);
            answer.setPrompt(prompt);
            answer.setAnswerText(answerText);

            profileService.save(answer);
        }

        return "redirect:/profile";
    }
}
