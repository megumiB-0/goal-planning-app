package com.example.goalplanningapp.entity;

import java.security.Timestamp;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_records")
public class LearningRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	

	@ManyToOne
	@JoinColumn(name = "goal_id")
	private Goal goal;
	
	@Column(name = "learning_day")
	private Date learningDay;
	
	@Column(name = "start_time")
	private Timestamp startTime;
	
	@Column(name = "end_time")
	private Timestamp endTime;
	
	@Column(name = "learning_time")
	private Integer learningTime;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
	
	@Column(name = "deleted_at", insertable = false, updatable = false)
	private Timestamp deleted;

}
