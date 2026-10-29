package com.library.lms.service;

import com.library.lms.entity.Payment;
import com.library.lms.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final EmployeeService employeeService;
    private final IdGenerator idGenerator;

    public PaymentService(PaymentRepository paymentRepository, MemberService memberService,
                          EmployeeService employeeService, IdGenerator idGenerator) {
        this.paymentRepository = paymentRepository;
        this.memberService = memberService;
        this.employeeService = employeeService;
        this.idGenerator = idGenerator;
    }

    public List<Payment> list() {
        return paymentRepository.findAll();
    }

    public List<Payment> byMember(String memberRef) {
        return paymentRepository.findByPaidBy(memberService.requireByRef(memberRef).getMemId());
    }

    @Transactional
    public Payment create(String memberRef, String employeeRef, BigDecimal amount,
                          String paymentType, String paymentMode, String paymentStatus) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required.");
        }
        Validators.requireNonNegative("Amount", amount);
        String paidBy = "";
        if (memberRef != null && !memberRef.isBlank()) {
            paidBy = memberService.requireByRef(memberRef).getMemId();
        }
        String employeeId = "";
        if (employeeRef != null && !employeeRef.isBlank()) {
            employeeId = employeeService.getById(employeeRef.trim())
                    .map(e -> e.getEmpId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No employee found for '" + employeeRef + "'."));
        }
        String today = LocalDate.now().format(DATE);
        Payment payment = new Payment();
        payment.setTransactionId(idGenerator.nextId("TXN", paymentRepository.findAll(), Payment::getRowNum));
        payment.setTransactionDate(today);
        payment.setTimestamp(LocalDateTime.now().format(TIMESTAMP));
        payment.setPaymentAmount(amount);
        payment.setPaymentType(paymentType);
        payment.setPaymentMode(paymentMode);
        payment.setPaymentStatus(paymentStatus == null || paymentStatus.isBlank() ? "Completed" : paymentStatus);
        payment.setPaidBy(paidBy);
        payment.setRecievedBy(employeeId);
        payment.setUserRowNum("");
        return paymentRepository.save(payment);
    }
}
