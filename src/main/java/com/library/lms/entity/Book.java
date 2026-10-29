package com.library.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_num")
    private Integer rowNum;

    @Column(name = "book_id", nullable = false, unique = true, length = 20)
    private String bookId;

    @Column(name = "book_name", nullable = false, length = 255)
    private String bookName;

    @Column(name = "book_author", length = 255)
    private String bookAuthor;

    @Column(name = "book_price", precision = 10, scale = 2)
    private BigDecimal bookPrice;

    @Column(name = "book_cat", length = 100)
    private String bookCat;

    @Column(name = "book_genre", length = 100)
    private String bookGenre;

    @Column(name = "edition", length = 50)
    private String edition;

    @Column(name = "publication", length = 255)
    private String publication;

    @Column(name = "status")
    private Integer status;

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public BigDecimal getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(BigDecimal bookPrice) {
        this.bookPrice = bookPrice;
    }

    public String getBookCat() {
        return bookCat;
    }

    public void setBookCat(String bookCat) {
        this.bookCat = bookCat;
    }

    public String getBookGenre() {
        return bookGenre;
    }

    public void setBookGenre(String bookGenre) {
        this.bookGenre = bookGenre;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
