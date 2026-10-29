package com.library.lms.web;

import com.library.lms.repository.BookRepository;
import com.library.lms.repository.EmployeeRepository;
import com.library.lms.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DropdownService {

    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;

    public DropdownService(MemberRepository memberRepository, EmployeeRepository employeeRepository,
                           BookRepository bookRepository) {
        this.memberRepository = memberRepository;
        this.employeeRepository = employeeRepository;
        this.bookRepository = bookRepository;
    }

    public List<SelectOption> members() {
        return memberRepository.findByStatus(0).stream()
                .map(m -> new SelectOption(m.getMemId(), m.getMemId() + " — " + m.getName()))
                .toList();
    }

    public List<SelectOption> employees() {
        return employeeRepository.findByStatus(0).stream()
                .map(e -> new SelectOption(e.getEmpId(), e.getEmpId() + " — " + e.getName()))
                .toList();
    }

    public List<SelectOption> books() {
        return bookRepository.findByStatus(0).stream()
                .map(b -> new SelectOption(b.getBookId(), b.getBookId() + " — " + b.getBookName(),
                        b.getBookName(), b.getBookPrice() == null ? "" : b.getBookPrice().toPlainString()))
                .toList();
    }
}
