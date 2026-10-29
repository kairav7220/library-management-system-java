package com.library.lms.controller;

import com.library.lms.entity.BookIssue;
import com.library.lms.service.IssueService;
import com.library.lms.web.DropdownService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookIssueController {

    private final IssueService issueService;
    private final DropdownService dropdownService;
    private final FlashService flashService;

    public BookIssueController(IssueService issueService, DropdownService dropdownService,
                               FlashService flashService) {
        this.issueService = issueService;
        this.dropdownService = dropdownService;
        this.flashService = flashService;
    }

    @GetMapping("/book_issue")
    public String list(Model model) {
        model.addAttribute("issues", issueService.list());
        return "book_issue";
    }

    @GetMapping("/book_issue/add")
    public String addForm(Model model) {
        model.addAttribute("books", dropdownService.books());
        model.addAttribute("members", dropdownService.members());
        model.addAttribute("employees", dropdownService.employees());
        return "add_book_issue";
    }

    @PostMapping("/book_issue/add")
    public String add(@RequestParam("book_id") String bookId,
                      @RequestParam("issued_to") String issuedTo,
                      @RequestParam(value = "issued_date", required = false) String issuedDate,
                      @RequestParam(value = "recieved_by", required = false) String recievedBy) {
        issueService.issue(bookId, issuedTo, recievedBy, issuedDate);
        flashService.add("success", "Book issued.");
        return "redirect:/book_issue";
    }

    @GetMapping("/book_issue/return/{rowNum}")
    public String returnForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        BookIssue issue = issueService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found."));
        if (issue.getReturnedDate() != null && !issue.getReturnedDate().isBlank()) {
            throw new IllegalArgumentException("Issue " + issue.getTransactionId() + " is already returned.");
        }
        model.addAttribute("issue", issue);
        model.addAttribute("employees", dropdownService.employees());
        model.addAttribute("rowNum", rowNum);
        return "return_book_issue";
    }

    @PostMapping("/book_issue/return/{rowNum}")
    public String returnBook(@PathVariable("rowNum") Integer rowNum,
                             @RequestParam(value = "recieved_by", required = false) String recievedBy) {
        issueService.returnByRowNum(rowNum, recievedBy);
        flashService.add("success", "Book returned.");
        return "redirect:/book_issue";
    }
}
