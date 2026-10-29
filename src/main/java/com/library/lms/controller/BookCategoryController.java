package com.library.lms.controller;

import com.library.lms.entity.BookCategory;
import com.library.lms.service.CatalogService;
import com.library.lms.web.FlashService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookCategoryController {

    private final CatalogService catalogService;
    private final FlashService flashService;

    public BookCategoryController(CatalogService catalogService, FlashService flashService) {
        this.catalogService = catalogService;
        this.flashService = flashService;
    }

    @GetMapping("/book_cat")
    public String list(Model model) {
        model.addAttribute("categories", catalogService.listCategories());
        return "book_category";
    }

    @GetMapping("/book_cat/add")
    public String addForm() {
        return "add_book_category";
    }

    @PostMapping("/book_cat/add")
    public String add(@RequestParam("cat_name") String catName,
                      @RequestParam(value = "description", required = false) String description) {
        catalogService.createCategory(catName, description, null);
        flashService.add("success", "Category added.");
        return "redirect:/book_cat";
    }

    @GetMapping("/book_cat/edit/{rowNum}")
    public String editForm(@PathVariable("rowNum") Integer rowNum, Model model) {
        BookCategory category = catalogService.getCategory(rowNum);
        model.addAttribute("category", category);
        model.addAttribute("rowNum", rowNum);
        return "edit_book_category";
    }

    @PostMapping("/book_cat/edit/{rowNum}")
    public String edit(@PathVariable("rowNum") Integer rowNum,
                       @RequestParam("cat_name") String catName,
                       @RequestParam(value = "description", required = false) String description) {
        BookCategory existing = catalogService.getCategory(rowNum);
        catalogService.updateCategory(rowNum, catName, description, existing.getBookNames());
        flashService.add("success", "Category updated.");
        return "redirect:/book_cat";
    }

    @PostMapping("/book_cat/delete/{rowNum}")
    public String delete(@PathVariable("rowNum") Integer rowNum) {
        catalogService.deleteCategory(rowNum);
        flashService.add("success", "Category deleted.");
        return "redirect:/book_cat";
    }
}
