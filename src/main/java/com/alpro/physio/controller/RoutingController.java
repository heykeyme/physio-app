package com.alpro.physio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoutingController {

    @GetMapping("/")
    public String loginPage() {
        return "login";  
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "pages/admin/admin";
    }

    @GetMapping("/management")
    public String managementPage() {
        return "pages/management/management";
    }

    @GetMapping("/participants")
    public String participantsPage() {
        return "pages/participants/participants";
    }

    @GetMapping("/trainers")
    public String trainersPage() {
        return "pages/trainers/trainers";
    }
}