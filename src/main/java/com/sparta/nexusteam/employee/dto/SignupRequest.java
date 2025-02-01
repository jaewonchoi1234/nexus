package com.sparta.nexusteam.employee.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "아이디는 빈칸이 될 수 없습니다")
    private String accountId;

    @NotBlank(message = "비밀번호는 빈칸이 될 수 없습니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[a-z\\d@$!%*?&]{8,}$", message = "영어 소문자, 숫자, 특수기호를 포함한 8자이상을 입력해주세요")
    private String password;

    @NotBlank(message = "이름은 빈칸이 될 수 없습니다")
    private String userName;

    @NotBlank(message = "이메일은 빈칸이 될 수 없습니다")
    @Email(message = "이메일형식을 지켜주세요")
    private String email;

    @NotBlank(message = "전화번호는 빈칸이 될 수 없습니다")
    @Pattern(regexp = "^(01[016789])-?(\\d{3,4})-?(\\d{4})$", message = "유효한 휴대전화번호 양식을 작성해주세요")
    private String phoneNumber;

    @NotBlank(message = "주소는 빈칸이 될 수 없습니다")
    private String address;

    @NotBlank(message = "회사명은 빈칸이 될 수 없습니다")
    private String companyName;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
}