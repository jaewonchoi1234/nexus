package com.sparta.nexusteam.employee.dto;

import lombok.Data;

@Data
public class LoginRequest {
	private String accountId;
	private String password;
}
