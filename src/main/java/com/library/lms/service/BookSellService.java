package com.library.lms.service;

import com.library.lms.entity.Book;
import com.library.lms.entity.BookSell;
import com.library.lms.entity.Payment;
import com.library.lms.repository.BookSellRepository;
import com.library.lms.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BookSellService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BookSellRepository bookSellRepository;
    private final PaymentRepository paymentRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final IdGenerator idGenerator;

    public BookSellService(BookSellRepository bookSellRepository, PaymentRepository paymentRepository,
                           BookService bookService, MemberService memberService,
                           IdGenerator idGenerator) {
        this.bookSellRepository = bookSellRepository;
        this.paymentRepository = paymentRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.idGenerator = idGenerator;
    }

    public List<BookSell> list() {
        return bookSellRepository.findAll();
    }

    public BookSell getByRowNum(Integer rowNum) {
        return bookSellRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Sell record not found."));
    }

    @Transactional
    public BookSell update(Integer rowNum, String bookId, String bookName, BigDecimal price, String memId) {
        BookSell sell = bookSellRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Sell record not found."));
        Validators.requireNonBlank("Book", bookId);
        Validators.requireNonBlank("Member", memId);
        Validators.requireNonNegative("Price", price);
        sell.setBookId(bookId);
        sell.setBookName(bookName);
        sell.setBookPrice(price);
        sell.setMemId(memId);
        return bookSellRepository.save(sell);
    }

    @Transactional
    public BookSell sell(String bookRef, String memberRef, BigDecimal price, String paymentMode,
                         String orderDate) {
        Book book = bookService.search(bookRef).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No book found for '" + bookRef + "'."));
        var member = memberService.requireByRef(memberRef);
        BigDecimal amount = price != null ? price : book.getBookPrice();
        Validators.requireNonNegative("Price", amount);
        String date = orderDate == null || orderDate.isBlank()
                ? LocalDate.now().format(DATE) : orderDate.trim();
        String now = LocalDateTime.now().format(TIMESTAMP);

        BookSell sell = new BookSell();
        sell.setOrderId(idGenerator.nextId("ORDER", bookSellRepository.findAll(), BookSell::getRowNum));
        sell.setOrderDate(date);
        sell.setTimestamp(now);
        sell.setBookId(book.getBookId());
        sell.setBookName(book.getBookName());
        sell.setBookPrice(amount);
        sell.setMemId(member.getMemId());
        BookSell savedSell = bookSellRepository.save(sell);

        Payment payment = new Payment();
        payment.setTransactionId(idGenerator.nextId("TXN", paymentRepository.findAll(), Payment::getRowNum));
        payment.setTransactionDate(date);
        payment.setTimestamp(now);
        payment.setPaymentAmount(amount);
        payment.setPaymentType("Book Purchase");
        payment.setPaymentMode(paymentMode == null || paymentMode.isBlank() ? "Cash" : paymentMode);
        payment.setPaymentStatus("Completed");
        payment.setPaidBy(member.getMemId());
        payment.setRecievedBy(null);
        payment.setUserRowNum("");
        paymentRepository.save(payment);

        return savedSell;
    }
}
