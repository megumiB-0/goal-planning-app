package com.example.goalplanningapp.entity;

import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "qualifications")
@Data
public class Qualification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "root_qualification_id", nullable = false)
	private Integer rootQualificationId;
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "estimated_minutes", nullable = false)
	private Double estimatedMinutes; //分単位
	
	 // Userと紐づけるが、管理者登録の場合はNULL許可
	@ManyToOne
	@JoinColumn(name = "created_by_user_id", nullable = true)
	private User user;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
	
	@Column(name = "deleted_at")
	private LocalDate deletedAt;

}
