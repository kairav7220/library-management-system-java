package com.library.lms.service;

import com.library.lms.entity.Log;
import com.library.lms.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void record(String action) {
        Log log = new Log();
        log.setTimestamp(LocalDateTime.now().format(FORMATTER));
        log.setAction(action);
        logRepository.save(log);
    }

    public List<Log> list() {
        return logRepository.findAllByOrderByRowNumDesc();
    }
}
