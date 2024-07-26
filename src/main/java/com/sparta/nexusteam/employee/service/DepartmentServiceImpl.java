package com.sparta.nexusteam.employee.service;

import com.sparta.nexusteam.employee.entity.Department;
import com.sparta.nexusteam.employee.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다");
        }
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다 " + id));

        if (!department.getName().equals(departmentDetails.getName()) &&
                departmentRepository.existsByName(departmentDetails.getName())) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다");
        }

        department.updateName(departmentDetails.getName());

        departmentRepository.save(department);

        return department;
    }

    @Override
    public Long deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다 " + id));

        departmentRepository.delete(department);

        return department.getId();
    }
}
