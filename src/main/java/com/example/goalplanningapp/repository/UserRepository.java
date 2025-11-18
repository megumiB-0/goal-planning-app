package com.example.goalplanningapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.goalplanningapp.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
