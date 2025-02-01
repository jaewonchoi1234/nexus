package com.sparta.nexusteam.employee.dto;

import com.sparta.nexusteam.employee.entity.Department;
import com.sparta.nexusteam.employee.entity.Employee;
import com.sparta.nexusteam.employee.entity.Position;
import com.sparta.nexusteam.employee.entity.UserRole;
import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String userName;
    private String email;
    private String phoneNumber;
    private Position position;
    private String departmentName;
    private Double wage;
    private UserRole role;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.userName = employee.getUserName();
        this.email = employee.getEmail();
        this.phoneNumber = employee.getPhoneNumber();
        this.position = employee.getPosition();
        this.departmentName = employee.getDepartment().getName();
        this.wage = employee.getWage();
        this.role = employee.getRole();
    }
}
