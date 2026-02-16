package com.et.SudburyCityPlatform.repository.program;

import com.et.SudburyCityPlatform.models.program.Program;
import com.et.SudburyCityPlatform.models.program.ProgramEnrollment;
import com.et.SudburyCityPlatform.models.program.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramEnrollmentRepository
        extends JpaRepository<ProgramEnrollment, Long> {

    boolean existsByProgramAndUser(Program program, User user);
}
