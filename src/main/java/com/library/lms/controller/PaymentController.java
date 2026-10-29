package com.library.lms.controller;

import com.library.lms.service.PaymentService;
import com.library.lms.web.DropdownService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final DropdownService dropdownService;
    private final FlashService flashService;

    public PaymentController(PaymentService paymentService, DropdownService dropdownService,
                             FlashService flashService) {
        this.paymentService = paymentService;
        this.dropdownService = dropdownService;
        this.flashService = flashService;
    }

    @GetMapping("/payments")
    public String list(Model model) {
        model.addAttribute("payments", paymentService.list());
        return "payments";
    }

    @GetMapping("/payments/add")
    public String addForm(Model model) {
        model.addAttribute("members", dropdownService.members());
        model.addAttribute("employees", dropdownService.employees());
        return "add_payment";
    }

    @PostMapping("/payments/add")
    public String add(@RequestParam("payment_amount") BigDecimal amount,
                      @RequestParam(value = "payment_type", required = false) String paymentType,
                      @RequestParam(value = "payment_mode", required = false) String paymentMode,
                      @RequestParam(value = "paid_by", required = false) String paidBy,
                      @RequestParam(value = "recieved_by", required = false) String recievedBy) {
        paymentService.create(paidBy, recievedBy, amount, paymentType, paymentMode, null);
        flashService.add("success", "Payment added.");
        return "redirect:/payments";
    }
}
