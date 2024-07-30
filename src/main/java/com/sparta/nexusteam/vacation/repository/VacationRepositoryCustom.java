package com.sparta.nexusteam.vacation.repository;

import com.sparta.nexusteam.vacation.entity.ApprovalStatus;
import com.sparta.nexusteam.vacation.entity.Vacation;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationRepositoryCustom {
    List<Vacation> findByCompanyIdAndApprovalStatus(Long companyId, ApprovalStatus approvalStatus);
}