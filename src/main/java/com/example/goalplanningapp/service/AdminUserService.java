package com.example.goalplanningapp.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.goalplanningapp.entity.User;
import com.example.goalplanningapp.repository.UserRepository;

@Service
public class AdminUserService {
	//DI
	private final UserRepository userRepository;
	
	public AdminUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	//全ユーザー表示
	public List<User> findAllUsers(){
		return userRepository.findAllByOrderByIdAsc();
	}
	
    // ユーザー検索結果ページ表示
    public Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        if (keyword == null) keyword = "";

        if ("active".equals(status)) {
            return userRepository.findByEnabledTrueAndNameContainingOrEnabledTrueAndEmailContaining(keyword, keyword, pageable);
        } else if ("deleted".equals(status)) {
            return userRepository.findByEnabledFalseAndNameContainingOrEnabledFalseAndEmailContaining(keyword, keyword, pageable);
        } else {
            return userRepository.findByNameContainingOrEmailContaining(keyword, keyword, pageable);
        }
    }
	
	//論理復活
	@Transactional
	public void restore(Integer id) {
		User user = userRepository.findById(id).orElseThrow();
		user.setEnabled(true);
		user.setDeletedAt(null);
	}

}
