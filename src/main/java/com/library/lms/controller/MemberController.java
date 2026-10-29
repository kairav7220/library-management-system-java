package com.library.lms.controller;

import com.library.lms.entity.Member;
import com.library.lms.service.MemberService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final FlashService flashService;

    public MemberController(MemberService memberService, FlashService flashService) {
        this.memberService = memberService;
        this.flashService = flashService;
    }

    @GetMapping("/members")
    public String list(Model model) {
        model.addAttribute("members", memberService.listActive());
        return "members";
    }

    @GetMapping("/members/add")
    public String addForm() {
        return "add_member";
    }

    @PostMapping("/members/add")
    public String add(@RequestParam("name") String name,
                      @RequestParam(value = "email", required = false) String email,
                      @RequestParam(value = "phone", required = false) String phone,
                      @RequestParam(value = "permanent_address", required = false) String permanentAddress,
                      @RequestParam(value = "temporary_address", required = false) String temporaryAddress) {
        memberService.create(name, null, email, phone, permanentAddress, temporaryAddress);
        flashService.add("success", "Member added.");
        return "redirect:/members";
    }

    @GetMapping("/members/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        Member member = memberService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        model.addAttribute("member", member);
        model.addAttribute("rowNum", rowNum);
        return "edit_member";
    }

    @PostMapping("/members/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("name") String name,
                       @RequestParam(value = "password", required = false) String password,
                       @RequestParam(value = "email", required = false) String email,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "permanent_address", required = false) String permanentAddress,
                       @RequestParam(value = "temporary_address", required = false) String temporaryAddress) {
        memberService.update(rowNum, name, password, email, phone, permanentAddress, temporaryAddress);
        flashService.add("success", "Member updated.");
        return "redirect:/members";
    }

    @PostMapping("/members/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        memberService.softDelete(rowNum);
        flashService.add("success", "Member deleted.");
        return "redirect:/members";
    }
}
