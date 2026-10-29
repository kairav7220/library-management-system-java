package com.library.lms.service;

import com.library.lms.entity.Book;
import com.library.lms.entity.BookIssue;
import com.library.lms.entity.Member;
import com.library.lms.repository.BookIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class IssueService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BookIssueRepository bookIssueRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final EmployeeService employeeService;
    private final IdGenerator idGenerator;
    private final FineCalculator fineCalculator;

    public IssueService(BookIssueRepository bookIssueRepository, BookService bookService,
                        MemberService memberService, EmployeeService employeeService,
                        IdGenerator idGenerator, FineCalculator fineCalculator) {
        this.bookIssueRepository = bookIssueRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.employeeService = employeeService;
        this.idGenerator = idGenerator;
        this.fineCalculator = fineCalculator;
    }

    public List<BookIssue> list() {
        return bookIssueRepository.findAll();
    }

    public List<BookIssue> overdue() {
        return bookIssueRepository.findByReturnedDateIsNull().stream()
                .filter(issue -> fineCalculator.isOverdue(issue.getIssuedDate()))
                .toList();
    }

    public List<BookIssue> byMember(String memberRef) {
        Member member = memberService.requireByRef(memberRef);
        return bookIssueRepository.findByIssuedTo(member.getMemId());
    }

    public Optional<BookIssue> getByTransactionId(String transactionId) {
        return bookIssueRepository.findByTransactionId(transactionId);
    }

    public Optional<BookIssue> getByRowNum(Integer rowNum) {
        return bookIssueRepository.findById(rowNum);
    }

    public Optional<BookIssue> getOpenIssueForBook(String bookId) {
        return bookIssueRepository.findOpenIssueByBook(bookId);
    }

    @Transactional
    public BookIssue issue(String bookRef, String memberRef, String employeeRef) {
        return issue(bookRef, memberRef, employeeRef, null);
    }

    @Transactional
    public BookIssue issue(String bookRef, String memberRef, String employeeRef, String issuedDate) {
        Book book = resolveBook(bookRef);
        Member member = memberService.requireByRef(memberRef);
        if (member.getStatus() != null && member.getStatus() == 1) {
            throw new IllegalArgumentException("Member " + member.getMemId() + " is inactive.");
        }
        if (bookIssueRepository.findOpenIssueByBook(book.getBookId()).isPresent()) {
            throw new IllegalArgumentException("Book " + book.getBookId() + " is already issued.");
        }
        String employeeId = resolveEmployeeId(employeeRef);
        String today = LocalDate.now().format(DATE);
        BookIssue issue = new BookIssue();
        issue.setTransactionId(idGenerator.nextId("TXN", bookIssueRepository.findAll(), BookIssue::getRowNum));
        issue.setTransactionDate(today);
        issue.setTimestamp(LocalDateTime.now().format(TIMESTAMP));
        issue.setBookId(book.getBookId());
        issue.setIssuedDate(issuedDate == null || issuedDate.isBlank() ? today : issuedDate.trim());
        issue.setIssuedTo(member.getMemId());
        issue.setRecievedBy(employeeId);
        return bookIssueRepository.save(issue);
    }

    @Transactional
    public BookIssue returnBook(String bookOrTxnRef, String returnedDate) {
        if (bookOrTxnRef == null || bookOrTxnRef.isBlank()) {
            throw new IllegalArgumentException("Book or transaction reference is required.");
        }
        String ref = bookOrTxnRef.trim();
        BookIssue issue = bookIssueRepository.findByTransactionId(ref)
                .or(() -> bookIssueRepository.findOpenIssueByBook(ref))
                .or(() -> resolveByNameOrOpenIssue(ref))
                .orElseThrow(() -> new IllegalArgumentException("No open issue found for '" + ref + "'."));
        if (issue.getReturnedDate() != null && !issue.getReturnedDate().isBlank()) {
            throw new IllegalArgumentException("Issue " + issue.getTransactionId() + " is already returned.");
        }
        issue.setReturnedDate(returnedDate == null || returnedDate.isBlank()
                ? LocalDate.now().format(DATE) : returnedDate.trim());
        return bookIssueRepository.save(issue);
    }

    @Transactional
    public BookIssue returnByRowNum(Integer rowNum, String employeeRef) {
        BookIssue issue = bookIssueRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found."));
        if (issue.getReturnedDate() != null && !issue.getReturnedDate().isBlank()) {
            throw new IllegalArgumentException("Issue " + issue.getTransactionId() + " is already returned.");
        }
        issue.setReturnedDate(LocalDate.now().format(DATE));
        String employeeId = resolveEmployeeId(employeeRef);
        if (employeeId != null) {
            issue.setRecievedBy(employeeId);
        }
        return bookIssueRepository.save(issue);
    }

    public int fineOf(BookIssue issue) {
        if (issue.getReturnedDate() != null && !issue.getReturnedDate().isBlank()) {
            return fineCalculator.fineFor(issue.getIssuedDate(), issue.getReturnedDate());
        }
        return fineCalculator.isOverdue(issue.getIssuedDate())
                ? fineCalculator.fineFor(issue.getIssuedDate(), null) : 0;
    }

    private Optional<BookIssue> resolveByNameOrOpenIssue(String ref) {
        Book book = bookService.search(ref).stream().findFirst().orElse(null);
        if (book != null) {
            return bookIssueRepository.findOpenIssueByBook(book.getBookId());
        }
        return Optional.empty();
    }

    private Book resolveBook(String bookRef) {
        if (bookRef == null || bookRef.isBlank()) {
            throw new IllegalArgumentException("Book reference is required.");
        }
        String ref = bookRef.trim();
        Optional<Book> byId = bookService.getById(ref);
        if (byId.isPresent()) {
            return byId.get();
        }
        List<Book> matches = bookService.search(ref);
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple books match '" + ref + "'. Use the BOOK_ id.");
        }
        throw new IllegalArgumentException("No book found for '" + ref + "'.");
    }

    private String resolveEmployeeId(String employeeRef) {
        if (employeeRef == null || employeeRef.isBlank()) {
            return null;
        }
        String ref = employeeRef.trim().toLowerCase(Locale.ROOT);
        return employeeService.listActive().stream()
                .filter(e -> e.getEmpId().toLowerCase(Locale.ROOT).equals(ref)
                        || e.getName().toLowerCase(Locale.ROOT).contains(ref))
                .findFirst()
                .map(e -> e.getEmpId())
                .orElseThrow(() -> new IllegalArgumentException("No employee found for '" + employeeRef + "'."));
    }
}
