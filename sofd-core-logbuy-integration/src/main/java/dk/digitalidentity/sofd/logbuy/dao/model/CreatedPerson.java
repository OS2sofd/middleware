package dk.digitalidentity.sofd.logbuy.dao.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import dk.digitalidentity.sofd.logbuy.dao.model.enums.Status;
import org.hibernate.annotations.CreationTimestamp;

import dk.digitalidentity.sofd.logbuy.dao.model.enums.Gender;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Setter
public class CreatedPerson {
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String uuid;

	@Column
	private String firstName;

	@Column
	private String surName;

	@Column
	private String email;

	@Column
	private String salaryNumber;

	@Enumerated(EnumType.STRING)
	@Column
	private Gender gender;

	@Enumerated(EnumType.STRING)
	@Column
	private Status status;

	@Column
	@CreationTimestamp
	private LocalDateTime created;

	@Column
	@UpdateTimestamp
	private LocalDateTime changed;

}