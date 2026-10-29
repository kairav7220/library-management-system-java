package com.library.lms.service;

import com.library.lms.entity.BookCategory;
import com.library.lms.entity.BookGenre;
import com.library.lms.repository.BookCategoryRepository;
import com.library.lms.repository.BookGenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final BookCategoryRepository categoryRepository;
    private final BookGenreRepository genreRepository;
    private final IdGenerator idGenerator;

    public CatalogService(BookCategoryRepository categoryRepository, BookGenreRepository genreRepository,
                          IdGenerator idGenerator) {
        this.categoryRepository = categoryRepository;
        this.genreRepository = genreRepository;
        this.idGenerator = idGenerator;
    }

    public List<BookCategory> listCategories() {
        return categoryRepository.findByStatus(0);
    }

    public List<BookGenre> listGenres() {
        return genreRepository.findByStatus(0);
    }

    public BookCategory getCategory(Integer rowNum) {
        return categoryRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
    }

    public BookGenre getGenre(Integer rowNum) {
        return genreRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found."));
    }

    @Transactional
    public BookCategory createCategory(String catName, String description, String bookNames) {
        if (catName == null || catName.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        BookCategory category = new BookCategory();
        category.setCatId(idGenerator.nextId("CAT", categoryRepository.findAll(), BookCategory::getRowNum));
        category.setCatName(catName.trim());
        category.setDescription(description);
        category.setBookNames(bookNames);
        category.setStatus(0);
        return categoryRepository.save(category);
    }

    @Transactional
    public BookCategory updateCategory(Integer rowNum, String catName, String description, String bookNames) {
        BookCategory category = getCategory(rowNum);
        if (catName == null || catName.isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        category.setCatName(catName.trim());
        category.setDescription(description);
        category.setBookNames(bookNames);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer rowNum) {
        BookCategory category = getCategory(rowNum);
        category.setStatus(1);
        categoryRepository.save(category);
    }

    @Transactional
    public BookGenre createGenre(String genreTitle, String bookNames) {
        if (genreTitle == null || genreTitle.isBlank()) {
            throw new IllegalArgumentException("Genre title is required.");
        }
        BookGenre genre = new BookGenre();
        genre.setGenreId(idGenerator.nextId("GENRE", genreRepository.findAll(), BookGenre::getRowNum));
        genre.setGenreTitle(genreTitle.trim());
        genre.setBookNames(bookNames);
        genre.setStatus(0);
        return genreRepository.save(genre);
    }

    @Transactional
    public BookGenre updateGenre(Integer rowNum, String genreTitle, String bookNames) {
        BookGenre genre = getGenre(rowNum);
        if (genreTitle == null || genreTitle.isBlank()) {
            throw new IllegalArgumentException("Genre title is required.");
        }
        genre.setGenreTitle(genreTitle.trim());
        genre.setBookNames(bookNames);
        return genreRepository.save(genre);
    }

    @Transactional
    public void deleteGenre(Integer rowNum) {
        BookGenre genre = getGenre(rowNum);
        genre.setStatus(1);
        genreRepository.save(genre);
    }
}
