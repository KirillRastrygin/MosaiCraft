package ru.rsreu.MosaiCraft.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.MosaiCraft.services.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.allUsers());
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("mosaicCount", userService.countMosaics());
        model.addAttribute("templateCount", userService.countTemplates());
        model.addAttribute("albumCount", userService.countAlbums());
        return "admin";
    }

    @PostMapping
    public String manageUser(@RequestParam Long userId, @RequestParam String action) {
        switch (action) {
            case "delete" -> userService.deleteUser(userId);
            case "changeRole" -> userService.changeRole(userId);
        }
        return "redirect:/admin";
    }

    @GetMapping("/gt/{userId}")
    public String gtUser(@PathVariable Long userId, Model model) {
        model.addAttribute("allUsers", userService.usergtList(userId));
        return "admin";
    }
}