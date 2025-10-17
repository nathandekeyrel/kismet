package com.github.nathandekeyrel.kismet;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final PromptSectionRepository promptSectionRepository;
    private final ProfileAnswerRepository profileAnswerRepository;
    private final PromptRepository promptRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, PromptSectionRepository promptSectionRepository, ProfileAnswerRepository profileAnswerRepository, PromptRepository promptRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.promptSectionRepository = promptSectionRepository;
        this.profileAnswerRepository = profileAnswerRepository;
        this.promptRepository = promptRepository;
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(User user, Model model) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ProfileAnswer> answers = profileAnswerRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("answers", answers);

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showProfileEdit(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<PromptSection> sections = promptSectionRepository.findAll();
        model.addAttribute("sections", sections);

        List<ProfileAnswer> existingAnswers = profileAnswerRepository.findByUser(user);

        ProfileEditForm form = new ProfileEditForm();
        form.setBio(user.getBio());

        for (ProfileAnswer answer : existingAnswers) {
            form.getAnswers().put(answer.getPrompt().getId(), answer.getAnswerText());
        }

        model.addAttribute("profileForm", form);

        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String processProfileEdit(User user, @ModelAttribute("profileForm") ProfileEditForm profileForm, Principal principal) {
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        currentUser.setBio(user.getBio());
        userRepository.save(currentUser);

        for (Map.Entry<Long, String> entry : profileForm.getAnswers().entrySet()) {
            Long promptId = entry.getKey();
            String answerText = entry.getValue();

            Prompt prompt = promptRepository.findById(promptId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid prompt ID"));

            ProfileAnswer answer = profileAnswerRepository.findByUserAndPrompt(currentUser, prompt)
                    .orElse(new ProfileAnswer());

            answer.setUser(currentUser);
            answer.setPrompt(prompt);
            answer.setAnswerText(answerText);

            profileAnswerRepository.save(answer);
        }

        return "redirect:/profile";
    }
}
