package com.sparta.nexusteam.security;

import com.sparta.nexusteam.employee.entity.Employee;
import com.sparta.nexusteam.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final EmployeeRepository employeeRepository;

	
	@Override
	public UserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
		Employee employee = employeeRepository.findByAccountId(accountId);

		return new UserDetailsImpl(employee);
	}
}
