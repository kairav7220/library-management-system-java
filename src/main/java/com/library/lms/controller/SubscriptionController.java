package com.library.lms.controller;

import com.library.lms.entity.Subscription;
import com.library.lms.service.SubscriptionService;
import com.library.lms.web.DropdownService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final DropdownService dropdownService;
    private final FlashService flashService;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  DropdownService dropdownService, FlashService flashService) {
        this.subscriptionService = subscriptionService;
        this.dropdownService = dropdownService;
        this.flashService = flashService;
    }

    @GetMapping("/subscriptions")
    public String list(Model model) {
        model.addAttribute("subscriptions", subscriptionService.listActive());
        return "subscriptions";
    }

    @GetMapping("/subscriptions/add")
    public String addForm(Model model) {
        model.addAttribute("members", dropdownService.members());
        return "add_subscription";
    }

    @PostMapping("/subscriptions/add")
    public String add(@RequestParam("mem_id") String memId,
                      @RequestParam("plan_mode") String planMode,
                      @RequestParam(value = "mem_subscription_amount", required = false) BigDecimal amount,
                      @RequestParam(value = "plan_type", required = false) String planType) {
        subscriptionService.create(memId, planMode, planType, amount, null, null);
        flashService.add("success", "Subscription added.");
        return "redirect:/subscriptions";
    }

    @GetMapping("/subscriptions/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        Subscription subscription = subscriptionService.getByRowNum(rowNum);
        model.addAttribute("subscription", subscription);
        model.addAttribute("members", dropdownService.members());
        model.addAttribute("rowNum", rowNum);
        return "edit_subscription";
    }

    @PostMapping("/subscriptions/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("mem_id") String memId,
                       @RequestParam("plan_mode") String planMode,
                       @RequestParam(value = "mem_subscription_amount", required = false) BigDecimal amount,
                       @RequestParam(value = "plan_type", required = false) String planType,
                       @RequestParam(value = "subscription_status", required = false) Integer subscriptionStatus) {
        Subscription existing = subscriptionService.getByRowNum(rowNum);
        subscriptionService.update(rowNum, existing.getTransactionDate(), existing.getTimestamp(),
                planMode, memId, amount, planType, existing.getPlanStart(), existing.getPlanEnd(),
                subscriptionStatus);
        flashService.add("success", "Subscription updated.");
        return "redirect:/subscriptions";
    }

    @PostMapping("/subscriptions/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        subscriptionService.delete(rowNum);
        flashService.add("success", "Subscription deleted.");
        return "redirect:/subscriptions";
    }
}
