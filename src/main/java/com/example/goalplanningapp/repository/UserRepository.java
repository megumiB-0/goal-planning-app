package com.example.goalplanningapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	// Emailで検索する
	public User findByEmail(String email);

	// 退会済みユーザーを取得しない
	Optional<User> findByEmailAndDeletedAtIsNull(String email);
	
	// 管理者用：全権取得（退会済み含め）
	List<User> findAllByOrderByIdAsc();
	
	// キーワード（名前 or email）部分一致、状態指定あり
    Page<User> findByEnabledTrueAndNameContainingOrEnabledTrueAndEmailContaining(
        String nameKeyword,
        String emailKeyword,
        Pageable pageable
    );

    Page<User> findByEnabledFalseAndNameContainingOrEnabledFalseAndEmailContaining(
        String nameKeyword,
        String emailKeyword,
        Pageable pageable
    );

    // 全件（状態指定なし）でキーワード検索
    Page<User> findByNameContainingOrEmailContaining(String nameKeyword, String emailKeyword, Pageable pageable);

}
