package com.library.lms.ai;

import com.library.lms.entity.Book;
import com.library.lms.entity.BookIssue;
import com.library.lms.entity.Member;
import com.library.lms.entity.Subscription;
import com.library.lms.service.BookService;
import com.library.lms.service.IssueService;
import com.library.lms.service.MemberService;
import com.library.lms.service.StatsService;
import com.library.lms.service.SubscriptionService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class LibraryTools {

    private final BookService bookService;
    private final MemberService memberService;
    private final IssueService issueService;
    private final SubscriptionService subscriptionService;
    private final StatsService statsService;

    public LibraryTools(BookService bookService, MemberService memberService,
                        IssueService issueService, SubscriptionService subscriptionService,
                        StatsService statsService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.issueService = issueService;
        this.subscriptionService = subscriptionService;
        this.statsService = statsService;
    }

    @Tool("Search active books by title, author, category, genre or BOOK id. "
            + "Returns one line per book with id, name, author, price, category, genre, edition, publication.")
    public String searchBooks(@P("Search text, e.g. 'DBMS', 'Orwell' or 'BOOK_2'") String query) {
        List<Book> books = bookService.search(nullSafeQuery(query));
        if (books.isEmpty()) {
            return "No active books matched '" + query + "'.";
        }
        return books.stream()
                .map(b -> b.getBookId() + " | " + b.getBookName() + " | by " + dash(b.getBookAuthor())
                        + " | Rs." + b.getBookPrice() + " | cat: " + dash(b.getBookCat())
                        + " | genre: " + dash(b.getBookGenre())
                        + " | edition: " + dash(b.getEdition())
                        + " | " + dash(b.getPublication()))
                .collect(Collectors.joining("\n"));
    }

    @Tool("Get full details of one book (by BOOK id or title) including who holds it right now.")
    public String getBook(@P("BOOK id or (approximate) book title") String bookRef) {
        Book book = resolveBook(bookRef);
        if (book == null) {
            return "No book found for '" + bookRef + "'.";
        }
        return issueService.getOpenIssueForBook(book.getBookId())
                .map(open -> book.getBookId() + " — " + book.getBookName() + " by " + dash(book.getBookAuthor())
                        + ", Rs." + book.getBookPrice() + ", category " + dash(book.getBookCat())
                        + ", genre " + dash(book.getBookGenre()) + ", edition " + dash(book.getEdition())
                        + ", " + dash(book.getPublication())
                        + " | currently issued to " + open.getIssuedTo()
                        + " (" + open.getIssuedDate() + ")")
                .orElseGet(() -> book.getBookId() + " — " + book.getBookName() + " by " + dash(book.getBookAuthor())
                        + ", Rs." + book.getBookPrice() + ", category " + dash(book.getBookCat())
                        + ", genre " + dash(book.getBookGenre()) + ", edition " + dash(book.getEdition())
                        + ", " + dash(book.getPublication()) + " | available");
    }

    @Tool("Search active members by name, email, phone or MEM id. "
            + "Returns one line per member with id, name, email, phone.")
    public String searchMembers(@P("Search text, e.g. 'Amit' or 'MEM_1'") String query) {
        List<Member> members = memberService.search(nullSafeQuery(query));
        if (members.isEmpty()) {
            return "No active members matched '" + query + "'.";
        }
        return members.stream()
                .map(m -> m.getMemId() + " | " + m.getName() + " | " + dash(m.getEmail())
                        + " | " + dash(m.getPhone()))
                .collect(Collectors.joining("\n"));
    }

    @Tool("List the books currently issued to the logged-in library member. "
            + "Only meaningful for member logins; for staff, ask the member reference instead.")
    public String getMyIssuedBooks() {
        String memberRef = ChatContext.memberRef();
        if (memberRef == null || memberRef.isBlank()) {
            return "No member profile is bound to this login. "
                    + "Ask the member for their MEM id and use getIssuesForMember instead.";
        }
        return getIssuesForMember(memberRef);
    }

    @Tool("Staff view: all issue records (open and closed) for one member, given a MEM id or name.")
    public String getIssuesForMember(@P("Member MEM id or name") String memberRef) {
        List<BookIssue> issues = issueService.byMember(nullSafeQuery(memberRef));
        if (issues.isEmpty()) {
            return "No issue records found for '" + memberRef + "'.";
        }
        return issues.stream()
                .map(i -> i.getTransactionId() + " | " + i.getBookId()
                        + " | issued " + dash(i.getIssuedDate())
                        + " | returned " + dash(i.getReturnedDate())
                        + (issueService.fineOf(i) > 0 ? " | fine Rs." + issueService.fineOf(i) : ""))
                .collect(Collectors.joining("\n"));
    }

    @Tool("Check the overdue fine for a member (MEM id or name) or a book (BOOK id or name). "
            + "Loan period is 14 days; the fine is Rs.10 per extra day.")
    public String checkFine(@P("Member reference (MEM id / name) or book reference (BOOK id / title)") String ref) {
        Member member = tryMember(nullSafeQuery(ref));
        if (member != null) {
            List<BookIssue> open = issueService.byMember(member.getMemId()).stream()
                    .filter(i -> i.getReturnedDate() == null || i.getReturnedDate().isBlank())
                    .toList();
            int total = open.stream().mapToInt(issueService::fineOf).sum();
            if (open.isEmpty()) {
                return member.getMemId() + " (" + member.getName() + ") has no open issues — no fine due.";
            }
            String details = open.stream()
                    .map(i -> i.getTransactionId() + " | " + i.getBookId()
                            + " | issued " + dash(i.getIssuedDate())
                            + " | overdue by " + overdueDaysOf(i) + " days | fine Rs." + issueService.fineOf(i))
                    .collect(Collectors.joining("\n"));
            return "Total overdue fine for " + member.getMemId() + " (" + member.getName() + "): Rs." + total
                    + "\n" + details;
        }
        BookIssue open = resolveOpenIssueByBookRef(nullSafeQuery(ref));
        if (open == null) {
            return "No open issue found for '" + ref + "' — no fine due.";
        }
        int fine = issueService.fineOf(open);
        if (fine <= 0) {
            return open.getTransactionId() + " (" + open.getBookId() + ", issued " + open.getIssuedDate()
                    + ") is not overdue — no fine due.";
        }
        return open.getTransactionId() + " (" + open.getBookId() + ", member " + open.getIssuedTo()
                + ", issued " + open.getIssuedDate() + ") is overdue — fine due: Rs." + fine;
    }

    @Tool("List all currently overdue open issues across the library, ordered by oldest first.")
    public String getOverdueBooks() {
        List<BookIssue> overdue = issueService.overdue();
        if (overdue.isEmpty()) {
            return "No overdue books right now.";
        }
        return overdue.stream()
                .map(i -> i.getTransactionId() + " | " + i.getBookId()
                        + " | member " + i.getIssuedTo()
                        + " | issued " + dash(i.getIssuedDate())
                        + " | overdue by " + overdueDaysOf(i) + " days | fine Rs." + issueService.fineOf(i))
                .collect(Collectors.joining("\n"));
    }

    @Tool("Library overview: active books, active members, books currently out, overdue books, and total revenue.")
    public String getLibraryStats() {
        return "Active books: " + statsService.totalBooks()
                + "\nActive members: " + statsService.totalMembers()
                + "\nBooks currently out: " + statsService.issuedCount()
                + "\nOverdue books: " + statsService.overdueCount()
                + "\nRevenue collected: Rs." + statsService.revenue();
    }

    @Tool("List the subscription plans (Monthly / Quarterly / Yearly etc.) with pricing as charged in the library.")
    public String getSubscriptionPlans() {
        Map<String, String> plans = subscriptionService.plansPricing();
        if (plans.isEmpty()) {
            return "No subscription plans recorded yet.";
        }
        return plans.entrySet().stream()
                .map(e -> e.getKey() + " — Rs." + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Tool("Issue a book to a member. Validates availability first. Returns the transaction id. "
            + "Loan period is 14 days. Do NOT invent ids — resolve names to ids with searchBooks/searchMembers first.")
    public String issueBook(@P("Book reference: BOOK id or title") String bookRef,
                            @P("Member reference: MEM id or name") String memRef) {
        BookIssue issue = issueService.issue(bookRef, memRef, null, null);
        return "Issued " + issue.getBookId() + " to " + issue.getIssuedTo()
                + " as " + issue.getTransactionId() + " on " + issue.getIssuedDate()
                + ". Return within 14 days (no fine until then).";
    }

    @Tool("Return an issued book and report the fine, if any. "
            + "Provide the transaction id, the book reference (BOOK id or title), or the member reference holding the book.")
    public String returnBook(@P("Transaction id, BOOK id/title, or member reference holding the book") String ref,
                             @P("Return date as yyyy-MM-dd; omit or pass null for today") String returnedDate) {
        BookIssue issue = issueService.returnBook(ref, returnedDate);
        int fine = issueService.fineOf(issue);
        return "Returned " + issue.getTransactionId() + " (" + issue.getBookId() + ") on " + issue.getReturnedDate()
                + (fine > 0 ? ". Overdue fine due: Rs." + fine + " — collect it from the Payments page."
                            : ". No fine due.");
    }

    @Tool("Register a new library member (creates the member record; a login account is created separately by admins on the Users page).")
    public String registerMember(@P("Full name") String name,
                                 @P("Email address") String email,
                                 @P("Phone number") String phone) {
        Member member = memberService.create(name, null, email, phone, null, null);
        return "Registered member " + member.getMemId() + " — " + member.getName()
                + ". Login accounts are created by an admin on the Users page.";
    }

    private Member tryMember(String ref) {
        try {
            return memberService.requireByRef(ref);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Book resolveBook(String bookRef) {
        if (bookRef == null || bookRef.isBlank()) {
            return null;
        }
        String ref = bookRef.trim();
        return bookService.getById(ref)
                .or(() -> bookService.search(ref).stream().findFirst())
                .orElse(null);
    }

    private BookIssue resolveOpenIssueByBookRef(String ref) {
        return resolveBook(ref) == null ? null
                : issueService.getOpenIssueForBook(resolveBook(ref).getBookId()).orElse(null);
    }

    private long overdueDaysOf(BookIssue issue) {
        java.time.LocalDate issued = parseIso(dash(issue.getIssuedDate()));
        java.time.LocalDate end = parseIso(issue.getReturnedDate());
        if (issued == null) {
            return 0;
        }
        java.time.LocalDate today = end == null ? java.time.LocalDate.now() : end;
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(issued, today) - 14);
    }

    private java.time.LocalDate parseIso(String value) {
        if (value == null || value.isBlank() || "—".equals(value.trim())) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String nullSafeQuery(String value) {
        return value == null ? "" : value.trim();
    }

    private String dash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
