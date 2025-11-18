package com.example.goalplanningapp.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender,Integer>{
	
	@Override
	public Integer convertToDatabaseColumn(Gender gender) {
		return gender == null ? null : gender.getCode();
	}
	
	@Override
	public Gender convertToEntityAttribute(Integer dbData) {
		return dbData == null ? Gender.OTHER : Gender.fromCode(dbData);
	}

}
