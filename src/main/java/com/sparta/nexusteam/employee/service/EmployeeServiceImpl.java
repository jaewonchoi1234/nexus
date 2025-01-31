package com.sparta.nexusteam.employee.service;

import com.sparta.nexusteam.employee.dto.*;
import com.sparta.nexusteam.employee.entity.*;
import com.sparta.nexusteam.employee.repository.CompanyRepository;
import com.sparta.nexusteam.employee.repository.DepartmentRepository;
import com.sparta.nexusteam.employee.repository.EmployeeRepository;
import com.sparta.nexusteam.employee.repository.InvitationRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final InvitationRepository invitationRepository;

    private final CompanyRepository companyRepository;

    private final DepartmentRepository departmentRepository;

    private final JavaMailSender mailSender;

    private final PasswordEncoder passwordEncoder;

    @Override
    public SignupResponse signup(SignupRequest request) {
        if (employeeRepository.existsByAccountId(request.getAccountId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
                if(companyRepository.existsByName(request.getCompanyName())) {
            throw new IllegalArgumentException("이미 존재하는 회사입니다.");
        }

        Company company = new Company(request.getCompanyName());
        companyRepository.save(company);

        Department department = new Department("발령대기", company);
        departmentRepository.save(department);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Employee employee = new Employee(request,encodedPassword, Position.CHAIRMAN ,UserRole.ADMIN, company, department);

        employeeRepository.save(employee);

        return new SignupResponse(employee);
    }

    @Override
    public Long logout(Employee employee, HttpServletResponse response) {
        Long userId = employee.getId();
        employee.updateRefreshToken(null);

        employeeRepository.save(employee);

        return userId;
    }

    @Override
    public String inviteEmployee(String email, Employee employee) {
        if(UserRole.USER.equals(employee.getRole())){
            throw new AccessDeniedException("권한이 없습니다");
        }

        String token = UUID.randomUUID().toString();
        Invitation invitation = new Invitation(email, token, new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), employee.getCompany().getName());

        invitationRepository.save(invitation);

        return sendInvitationEmail(email, token);
    }

    @Override
    public SignupResponse setNewEmployee(String token, InviteSignupRequest invitesignupRequest) {
        Invitation invitation = invitationRepository.findByToken(token);
        if (invitation == null) {
            throw new RuntimeException ("유효하지 않은 token");
        }

        Company company = companyRepository.findByName(invitation.getCompanyName());
        Department department = departmentRepository.findById(1L).orElseThrow(RuntimeException::new);
        String encodedPassword = passwordEncoder.encode(invitesignupRequest.getPassword());
        Employee employee = new Employee(invitesignupRequest,encodedPassword, Position.WORKER ,UserRole.USER, company, department);

        employeeRepository.save(employee);

        return new SignupResponse(employee);
    }

    @Override
    /*@Cacheable(value = "allEmployees", key = "#employeeDetail.company.id")*/
    public List<EmployeeResponse> getAllEmployees(Employee employeeDetail) {
        List<Employee> employees = employeeRepository.findAllByCompany(employeeDetail.getCompany());
        List<EmployeeResponse> responseList = new ArrayList<>();

        for(Employee employee: employees){
            EmployeeResponse employeeResponse = new EmployeeResponse(employee);
            responseList.add(employeeResponse);
        }

        return responseList;
    }

    @Override
    /*@Cacheable(value = "allEmployeesByDepartment", key = "#departmentName + '_' + #employeeDetail.company.id")*/
    public List<EmployeeResponse> getEmployeesByDepartment(String departmentName, Employee employeeDetail) {
        List<Employee> employees = employeeRepository.findByDepartmentNameAndCompany(departmentName, employeeDetail.getCompany());
        List<EmployeeResponse> responseList = new ArrayList<>();

        for(Employee employee: employees){
            EmployeeResponse employeeResponse = new EmployeeResponse(employee);
            responseList.add(employeeResponse);
        }

        return responseList;
    }

    @Override
    /*@Cacheable(value = "allEmployeesByUserName", key = "#userName + '_' + #employeeDetail.company.id")*/
    public List<EmployeeResponse> getEmployeesByUserName(String userName, Employee employeeDetail) {
        List<Employee> employees = employeeRepository.findByUserNameAndCompany(userName, employeeDetail.getCompany());
        List<EmployeeResponse> responseList = new ArrayList<>();

        for(Employee employee: employees){
            EmployeeResponse employeeResponse = new EmployeeResponse(employee);
            responseList.add(employeeResponse);
        }

        return responseList;
    }

    @Override
    /*@Cacheable(value = "employee", key = "#id")*/
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근로자 입니다." + id));

        return new EmployeeResponse(employee);
    }

    @Override
    /*@Caching(
            put = @CachePut(value = "employee", key = "#id"),
            evict = {
                    @CacheEvict(value = "allEmployees", allEntries = true),
                    @CacheEvict(value = "allEmployeesByDepartment", allEntries = true),
                    @CacheEvict(value = "allEmployeesByUserName", allEntries = true)
            }
    )*/
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request, Employee employeeDetail) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근로자 입니다." + id));

        if(UserRole.USER.equals(employeeDetail.getRole())){
            throw new AccessDeniedException("권한이 없습니다");
        }

        // 열거형과 객체 매핑
        Position position = Position.valueOf(request.getPosition());

        UserRole role = null;
        if(UserRole.ADMIN.equals(employeeDetail.getRole())) {
            role = UserRole.valueOf(request.getRole());
        } else if(UserRole.MANAGER.equals(employeeDetail.getRole())) {
            role = employee.getRole();
        }

        // 부서 매핑 - 부서 이름으로 Department 객체 조회
        Department department = departmentRepository.findByNameAndCompany(request.getDepartment(), employeeDetail.getCompany());

        employee.updateProfile(request, position, department, request.getWage() ,role);

        employeeRepository.save(employee);

        return new EmployeeResponse(employee);
    }

    @Override
    /*@Caching(
            evict = {
                    @CacheEvict(value = "employee", key = "#id"),
                    @CacheEvict(value = "allEmployees", allEntries = true),
                    @CacheEvict(value = "allEmployeesByDepartment", allEntries = true),
                    @CacheEvict(value = "allEmployeesByUserName", allEntries = true)
            }
    )*/
    public Long deleteEmployee(Long id, Employee employeeDetail) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근로자 입니다." + id));

        if(!UserRole.MANAGER.equals(employeeDetail.getRole())){
            throw new AccessDeniedException("권한이 없습니다");
        }

        employeeRepository.delete(employee);

        return employee.getId();
    }

    @Override
    public String getEmployeeRole(Employee employee) {
        UserRole role = employee.getRole();
        return switch (role) {
            case ADMIN -> "ADMIN";
            case MANAGER -> "MANAGER";
            case USER -> "USER";
        };
    }

    private String sendInvitationEmail(String email, String token) {
        String subject = "Employee Invitation";
        // 등록 양식 페이지 URL (예: 사용자 등록 페이지)
        String confirmationUrl = "http://yourdomain.com/employee/registerEmployee?token=" + token;
        String message = "<html>" +
                "<body>" +
                "<h2>Welcome!</h2>" +
                "<p>Please click the link below to register:</p>" +
                "<a href='" + confirmationUrl + "'>Register Here</a>" +
                "</body>" +
                "</html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true); // HTML 형식 메시지
            mailSender.send(mimeMessage);
            return "Invitation email sent successfully.";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
