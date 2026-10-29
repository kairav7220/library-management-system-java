package com.library.lms.controller;

import com.library.lms.entity.User;
import com.library.lms.service.UserService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final FlashService flashService;

    public UserController(UserService userService, FlashService flashService) {
        this.userService = userService;
        this.flashService = flashService;
    }

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userService.listActive());
        return "index";
    }

    @GetMapping("/users/add")
    public String addForm() {
        return "add_user";
    }

    @PostMapping("/users/add")
    public String add(@RequestParam("user_type") String userType,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam(value = "email", required = false) String email,
                      @RequestParam(value = "phone", required = false) String phone) {
        userService.create(userType, username, password, email, phone);
        flashService.add("success", "User added.");
        return "redirect:/users";
    }

    @GetMapping("/users/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        User user = userService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        model.addAttribute("user", user);
        model.addAttribute("rowNum", rowNum);
        return "form";
    }

    @PostMapping("/users/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam(value = "password", required = false) String password,
                       @RequestParam(value = "phone", required = false) String phone) {
        User existing = userService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        userService.update(rowNum, existing.getUserType(), existing.getUsername(),
                password, existing.getEmail(), phone);
        flashService.add("success", "User updated.");
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        userService.softDelete(rowNum);
        flashService.add("success", "User deleted.");
        return "redirect:/users";
    }
}
