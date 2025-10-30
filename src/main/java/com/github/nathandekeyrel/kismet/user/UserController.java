package com.github.nathandekeyrel.kismet.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    // TODO: this will be moved to MatchController when I get that implemented
    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }

}
