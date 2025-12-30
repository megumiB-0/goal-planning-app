package com.example.goalplanningapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.goalplanningapp.entity.Role;
import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.form.SignupForm;
import com.example.goalplanningapp.form.UserEditForm;
import com.example.goalplanningapp.repository.RoleRepository;
import com.example.goalplanningapp.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository,RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public User createUser(SignupForm signupForm) {
		User user = new User();
		Role role =roleRepository.findByName("ROLE_GENERAL");
		
		user.setName(signupForm.getName());
		user.setDateOfBirth(signupForm.getDateOfBirth());
		user.setGender(signupForm.getGender());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(true);
		
		return userRepository.save(user);
		
		}
	
	// メールアドレスが登録すみかどうかチェックする
	public boolean isEmailRegistrated(String email) {
		User user = userRepository.findByEmail(email);
		return user !=null;
	}
	// パスワードとパスワード（確認用）が一致するかチェックする
	public boolean isSamePassword(String password, String passwordCongirmation) {
		return password.equals(passwordCongirmation);
	}
	
	// 編集・セーブ
	@Transactional
	public void updateUser(User user, UserEditForm form) {
		
		// 自分以外で同じメールを使っているかチェック
		if(userRepository.existsByEmailAndIdNot(form.getEmail(), user.getId())) {
			throw new IllegalArgumentException("そのメールアドレスは既に使用されています。");
		}
		user.setName(form.getName());
		user.setDateOfBirth(form.getDateOfBirth());
		user.setGender(form.getGender());
		user.setEmail(form.getEmail());
		
		userRepository.save(user);
		
	}
	
	// 自分以外の誰かがそのメールアドレスを使っていないか？
	public boolean isEmailDuplicateForEdit(String email, Integer userId) {
	    return userRepository.existsByEmailAndIdNot(email, userId);
	}
	
	

}
