package com.github.nathandekeyrel.kismet.user;

import com.github.nathandekeyrel.kismet.profile.Profile;
import com.github.nathandekeyrel.kismet.profile.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, ProfileService profileService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(User user, Model model) {
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profileService.saveProfile(profile);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }
}