package com.library.lms.repository;

import com.library.lms.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByMemId(String memId);

    Optional<Member> findByUserId(String userId);

    List<Member> findByStatus(Integer status);
}
