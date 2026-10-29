package com.library.lms.repository;

import com.library.lms.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Integer> {

    List<Log> findAllByOrderByRowNumDesc();
}
