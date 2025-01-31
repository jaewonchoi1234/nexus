package com.sparta.nexusteam.employee.repository;

import com.sparta.nexusteam.employee.entity.Company;
import com.sparta.nexusteam.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

    List<Employee> findByDepartmentNameAndCompany(String departmentName, Company company);

    @Query("SELECT e FROM Employee e WHERE e.userName LIKE %:username% AND e.company = :company")
    List<Employee> findByUserNameAndCompany(String username, Company company);

    List<Employee> findAllByCompany(Company company);
}
