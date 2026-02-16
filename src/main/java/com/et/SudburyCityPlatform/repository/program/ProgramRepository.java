package com.et.SudburyCityPlatform.repository.program;

import com.et.SudburyCityPlatform.models.program.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
}
