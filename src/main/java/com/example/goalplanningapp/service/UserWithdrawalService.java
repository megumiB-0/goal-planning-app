package com.example.goalplanningapp.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.UserRepository;

@Service
public class UserWithdrawalService {
	
	// DI
	private final UserRepository userRepository;
	public UserWithdrawalService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void withdraw(User user) {
		user.setDeletedAt(LocalDateTime.now());
		user.setEnabled(false);
		userRepository.save(user);
	}
	

}
