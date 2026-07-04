package com.alpro.physio.controller.route;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoutingController {

    @GetMapping("/")
    public String loginPage() {
        return "login";  
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "pages/admin/admin";
    }

    @GetMapping("/management")
    public String managementPage() {
        return "pages/management/management";
    }

    @GetMapping("/participant")
    public String participantsPage() {
        return "pages/participants/participants";
    }

    @GetMapping("/trainer")
    public String trainersPage() {
        return "pages/trainers/trainers";
    }
}