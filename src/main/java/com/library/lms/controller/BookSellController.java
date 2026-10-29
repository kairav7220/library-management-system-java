package com.library.lms.controller;

import com.library.lms.entity.BookSell;
import com.library.lms.service.BookSellService;
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
public class BookSellController {

    private final BookSellService bookSellService;
    private final DropdownService dropdownService;
    private final FlashService flashService;

    public BookSellController(BookSellService bookSellService, DropdownService dropdownService,
                              FlashService flashService) {
        this.bookSellService = bookSellService;
        this.dropdownService = dropdownService;
        this.flashService = flashService;
    }

    @GetMapping("/book_sell")
    public String list(Model model) {
        model.addAttribute("sells", bookSellService.list());
        return "book_sell";
    }

    @GetMapping("/book_sell/add")
    public String addForm(Model model) {
        model.addAttribute("books", dropdownService.books());
        model.addAttribute("members", dropdownService.members());
        return "add_book_sell";
    }

    @PostMapping("/book_sell/add")
    public String add(@RequestParam("book_id") String bookId,
                      @RequestParam(value = "book_price", required = false) BigDecimal price,
                      @RequestParam("mem_id") String memId) {
        bookSellService.sell(bookId, memId, price, "Cash", null);
        flashService.add("success", "Sell record added.");
        return "redirect:/book_sell";
    }

    @GetMapping("/book_sell/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        BookSell sell = bookSellService.getByRowNum(rowNum);
        model.addAttribute("sell", sell);
        model.addAttribute("books", dropdownService.books());
        model.addAttribute("members", dropdownService.members());
        model.addAttribute("rowNum", rowNum);
        return "edit_book_sell";
    }

    @PostMapping("/book_sell/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("book_id") String bookId,
                       @RequestParam(value = "book_name", required = false) String bookName,
                       @RequestParam(value = "book_price", required = false) BigDecimal price,
                       @RequestParam("mem_id") String memId) {
        bookSellService.update(rowNum, bookId, bookName, price, memId);
        flashService.add("success", "Sell record updated.");
        return "redirect:/book_sell";
    }
}
