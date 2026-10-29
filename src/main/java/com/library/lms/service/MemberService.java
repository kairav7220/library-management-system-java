package com.library.lms.service;

import com.library.lms.entity.Member;
import com.library.lms.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, IdGenerator idGenerator,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Member> listActive() {
        return memberRepository.findByStatus(0);
    }

    public Optional<Member> getByRowNum(Integer rowNum) {
        return memberRepository.findById(rowNum);
    }

    public Optional<Member> getById(String memId) {
        return memberRepository.findByMemId(memId);
    }

    public String memRefForUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return memberRepository.findByUserId(userId.trim())
                .filter(m -> m.getStatus() == null || m.getStatus() == 0)
                .map(Member::getMemId)
                .orElse(null);
    }

    public Member requireByRef(String memberRef) {
        if (memberRef == null || memberRef.isBlank()) {
            throw new IllegalArgumentException("Member reference is required.");
        }
        String ref = memberRef.trim();
        Optional<Member> byId = memberRepository.findByMemId(ref);
        if (byId.isPresent()) {
            return byId.get();
        }
        String q = ref.toLowerCase(Locale.ROOT);
        List<Member> matches = listActive().stream()
                .filter(m -> m.getName() != null && m.getName().toLowerCase(Locale.ROOT).contains(q))
                .toList();
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple members match '" + ref + "'. Use the MEM_ id.");
        }
        throw new IllegalArgumentException("No member found for '" + ref + "'.");
    }

    public List<Member> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return listActive().stream()
                .filter(m -> contains(m.getName(), q) || contains(m.getEmail(), q)
                        || contains(m.getPhone(), q) || contains(m.getMemId(), q))
                .toList();
    }

    @Transactional
    public Member create(String name, String rawPassword, String email, String phone,
                         String permanentAddress, String temporaryAddress) {
        Validators.requireNonBlank("Name", name);
        Validators.requireEmail("Email", email);
        Member member = new Member();
        member.setMemId(idGenerator.nextId("MEM", memberRepository.findAll(), Member::getRowNum));
        member.setName(name.trim());
        member.setUserId(null);
        member.setPassword(rawPassword == null || rawPassword.isBlank()
                ? null : passwordEncoder.encode(rawPassword));
        member.setEmail(email);
        member.setPhone(phone);
        member.setUserRowNum("");
        member.setPermanentAddress(permanentAddress);
        member.setTemporaryAddress(temporaryAddress);
        member.setStatus(0);
        return memberRepository.save(member);
    }

    @Transactional
    public Member update(Integer rowNum, String name, String rawPassword, String email, String phone,
                         String permanentAddress, String temporaryAddress) {
        Member member = memberRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        Validators.requireNonBlank("Name", name);
        member.setName(name.trim());
        if (rawPassword != null && !rawPassword.isBlank()) {
            member.setPassword(passwordEncoder.encode(rawPassword));
        }
        member.setEmail(email);
        member.setPhone(phone);
        member.setPermanentAddress(permanentAddress);
        member.setTemporaryAddress(temporaryAddress);
        return memberRepository.save(member);
    }

    @Transactional
    public void softDelete(Integer rowNum) {
        Member member = memberRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        member.setStatus(1);
        memberRepository.save(member);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
