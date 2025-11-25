package com.example.goalplanningapp.entity;

import java.security.Timestamp;
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
@Table(name = "goals")
@Data
public class Goal {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "qualification_id", nullable = false)
	private Qualification qualigication;
	
	@Column(name = "start_date")
	private LocalDate startDate;
	
	@Column(name = "goal_date")
	private LocalDate goalDate;
	
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
	
	@Column(name = "ended_at", insertable = false, updatable = false)
	private Timestamp endedAt;
	

}
