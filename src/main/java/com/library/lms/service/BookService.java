package com.library.lms.service;

import com.library.lms.entity.Book;
import com.library.lms.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final IdGenerator idGenerator;

    public BookService(BookRepository bookRepository, IdGenerator idGenerator) {
        this.bookRepository = bookRepository;
        this.idGenerator = idGenerator;
    }

    public List<Book> listActive() {
        return bookRepository.findByStatus(0);
    }

    public Optional<Book> getByRowNum(Integer rowNum) {
        return bookRepository.findById(rowNum);
    }

    public Optional<Book> getById(String bookId) {
        return bookRepository.findByBookId(bookId);
    }

    public List<Book> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return listActive().stream()
                .filter(b -> contains(b.getBookName(), q) || contains(b.getBookAuthor(), q)
                        || contains(b.getBookCat(), q) || contains(b.getBookGenre(), q)
                        || contains(b.getBookId(), q))
                .toList();
    }

    @Transactional
    public Book create(String bookName, String bookAuthor, BigDecimal bookPrice,
                       String bookCat, String bookGenre, String edition, String publication) {
        Validators.requireNonNegative("Price", bookPrice);
        if (bookName == null || bookName.isBlank()) {
            throw new IllegalArgumentException("Book name is required.");
        }
        Book book = new Book();
        book.setBookId(idGenerator.nextId("BOOK", bookRepository.findAll(), Book::getRowNum));
        book.setBookName(bookName.trim());
        book.setBookAuthor(bookAuthor);
        book.setBookPrice(bookPrice);
        book.setBookCat(bookCat);
        book.setBookGenre(bookGenre);
        book.setEdition(edition);
        book.setPublication(publication);
        book.setStatus(0);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Integer rowNum, String bookName, String bookAuthor, BigDecimal bookPrice,
                       String bookCat, String bookGenre, String edition, String publication) {
        Book book = bookRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        if (bookName == null || bookName.isBlank()) {
            throw new IllegalArgumentException("Book name is required.");
        }
        book.setBookName(bookName.trim());
        book.setBookAuthor(bookAuthor);
        book.setBookPrice(bookPrice);
        book.setBookCat(bookCat);
        book.setBookGenre(bookGenre);
        book.setEdition(edition);
        book.setPublication(publication);
        return bookRepository.save(book);
    }

    @Transactional
    public void softDelete(Integer rowNum) {
        Book book = bookRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        book.setStatus(1);
        bookRepository.save(book);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
