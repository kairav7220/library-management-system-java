package com.library.lms.service;

import com.library.lms.entity.Subscription;
import com.library.lms.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SubscriptionRepository subscriptionRepository;
    private final MemberService memberService;
    private final IdGenerator idGenerator;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, MemberService memberService,
                               IdGenerator idGenerator) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberService = memberService;
        this.idGenerator = idGenerator;
    }

    public List<Subscription> list() {
        return subscriptionRepository.findAll();
    }

    public List<Subscription> listActive() {
        return subscriptionRepository.findBySubscriptionStatus(0);
    }

    public Subscription getByRowNum(Integer rowNum) {
        return subscriptionRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found."));
    }

    public List<Subscription> byMember(String memberRef) {
        return subscriptionRepository.findByMemId(memberService.requireByRef(memberRef).getMemId());
    }

    public Map<String, String> plansPricing() {
        Map<String, TreeSet<String>> byPlan = list().stream()
                .filter(s -> s.getSubscriptionStatus() == null || s.getSubscriptionStatus() == 0)
                .filter(s -> s.getPlanType() != null && !s.getPlanType().isBlank())
                .filter(s -> s.getMemSubscriptionAmount() != null)
                .collect(Collectors.groupingBy(Subscription::getPlanType, TreeMap::new,
                        Collectors.mapping(s -> s.getMemSubscriptionAmount().stripTrailingZeros().toPlainString(),
                                Collectors.toCollection(TreeSet::new))));
        Map<String, String> pricing = new LinkedHashMap<>();
        byPlan.forEach((plan, amounts) -> pricing.put(plan, "Rs." + String.join(" or Rs.", amounts)));
        return pricing;
    }

    @Transactional
    public Subscription create(String memberRef, String planMode, String planType, BigDecimal amount,
                               String planStart, String planEnd) {
        Validators.requireNonBlank("Member", memberRef);
        Validators.requireNonBlank("Plan mode", planMode);
        Validators.requireNonNegative("Amount", amount);
        var member = memberService.requireByRef(memberRef);
        String today = LocalDate.now().format(DATE);
        Subscription subscription = new Subscription();
        subscription.setTransactionId(idGenerator.nextId("TXN", subscriptionRepository.findAll(), Subscription::getRowNum));
        subscription.setTransactionDate(today);
        subscription.setTimestamp(LocalDateTime.now().format(TIMESTAMP));
        subscription.setPlanMode(planMode);
        subscription.setMemId(member.getMemId());
        subscription.setMemSubscriptionAmount(amount);
        subscription.setPlanType(planType);
        subscription.setPlanStart(planStart == null || planStart.isBlank() ? today : planStart.trim());
        subscription.setPlanEnd(planEnd);
        subscription.setSubscriptionStatus(0);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription update(Integer rowNum, String transactionDate, String timestamp, String planMode,
                               String memId, BigDecimal amount, String planType,
                               String planStart, String planEnd, Integer subscriptionStatus) {
        Subscription subscription = subscriptionRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found."));
        Validators.requireNonBlank("Member", memId);
        Validators.requireNonBlank("Plan mode", planMode);
        Validators.requireNonNegative("Amount", amount);
        subscription.setTransactionDate(transactionDate);
        subscription.setTimestamp(timestamp);
        subscription.setPlanMode(planMode);
        subscription.setMemId(memId);
        subscription.setMemSubscriptionAmount(amount);
        subscription.setPlanType(planType);
        subscription.setPlanStart(planStart);
        subscription.setPlanEnd(planEnd);
        subscription.setSubscriptionStatus(subscriptionStatus);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void delete(Integer rowNum) {
        Subscription subscription = subscriptionRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found."));
        subscription.setSubscriptionStatus(1);
        subscriptionRepository.save(subscription);
    }
}
