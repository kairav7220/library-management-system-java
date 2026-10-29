package com.library.lms.controller;

import com.library.lms.entity.BookGenre;
import com.library.lms.service.CatalogService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookGenreController {

    private final CatalogService catalogService;
    private final FlashService flashService;

    public BookGenreController(CatalogService catalogService, FlashService flashService) {
        this.catalogService = catalogService;
        this.flashService = flashService;
    }

    @GetMapping("/book_genre")
    public String list(Model model) {
        model.addAttribute("genres", catalogService.listGenres());
        return "book_genre";
    }

    @GetMapping("/book_genre/add")
    public String addForm() {
        return "add_book_genre";
    }

    @PostMapping("/book_genre/add")
    public String add(@RequestParam("genre_title") String genreTitle) {
        catalogService.createGenre(genreTitle, null);
        flashService.add("success", "Genre added.");
        return "redirect:/book_genre";
    }

    @GetMapping("/book_genre/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        BookGenre genre = catalogService.getGenre(rowNum);
        model.addAttribute("genre", genre);
        model.addAttribute("rowNum", rowNum);
        return "edit_book_genre";
    }

    @PostMapping("/book_genre/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("genre_title") String genreTitle) {
        BookGenre existing = catalogService.getGenre(rowNum);
        catalogService.updateGenre(rowNum, genreTitle, existing.getBookNames());
        flashService.add("success", "Genre updated.");
        return "redirect:/book_genre";
    }

    @PostMapping("/book_genre/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        catalogService.deleteGenre(rowNum);
        flashService.add("success", "Genre deleted.");
        return "redirect:/book_genre";
    }
}
