package com.fitconnect.model.dto;

import com.fitconnect.model.entity.Gender;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private Gender gender;
    private LocalDate birthDate;
    private Integer fitnessGoalId;
    private LocationDto currentLocation;

    @Data
    public static class LocationDto {
        private double latitude;
        private double longitude;
    }
}
