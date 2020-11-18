package com.studyclass.event;

import com.studyclass.domain.Account;
import com.studyclass.domain.Enrollment;
import com.studyclass.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
