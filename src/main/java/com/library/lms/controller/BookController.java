package com.library.lms.controller;

import com.library.lms.entity.Book;
import com.library.lms.service.BookService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class BookController {

    private final BookService bookService;
    private final FlashService flashService;

    public BookController(BookService bookService, FlashService flashService) {
        this.bookService = bookService;
        this.flashService = flashService;
    }

    @GetMapping("/books")
    public String list(Model model) {
        model.addAttribute("books", bookService.listActive());
        return "books";
    }

    @GetMapping("/books/add")
    public String addForm() {
        return "add_book";
    }

    @PostMapping("/books/add")
    public String add(@RequestParam("book_name") String bookName,
                      @RequestParam(value = "book_author", required = false) String bookAuthor,
                      @RequestParam(value = "book_price", required = false) BigDecimal bookPrice,
                      @RequestParam(value = "book_cat", required = false) String bookCat,
                      @RequestParam(value = "book_genre", required = false) String bookGenre,
                      @RequestParam(value = "edition", required = false) String edition,
                      @RequestParam(value = "publication", required = false) String publication) {
        bookService.create(bookName, bookAuthor, bookPrice, bookCat, bookGenre, edition, publication);
        flashService.add("success", "Book added.");
        return "redirect:/books";
    }

    @GetMapping("/books/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        Book book = bookService.getByRowNum(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        model.addAttribute("book", book);
        model.addAttribute("rowNum", rowNum);
        return "edit_book";
    }

    @PostMapping("/books/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("book_name") String bookName,
                       @RequestParam(value = "book_author", required = false) String bookAuthor,
                       @RequestParam(value = "book_price", required = false) BigDecimal bookPrice,
                       @RequestParam(value = "book_cat", required = false) String bookCat,
                       @RequestParam(value = "book_genre", required = false) String bookGenre,
                       @RequestParam(value = "edition", required = false) String edition,
                       @RequestParam(value = "publication", required = false) String publication) {
        bookService.update(rowNum, bookName, bookAuthor, bookPrice, bookCat, bookGenre, edition, publication);
        flashService.add("success", "Book updated.");
        return "redirect:/books";
    }

    @PostMapping("/books/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        bookService.softDelete(rowNum);
        flashService.add("success", "Book deleted.");
        return "redirect:/books";
    }
}
