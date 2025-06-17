package com.fitconnect.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Lob
    private String description;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "target_muscle_group", length = 50)
    private String targetMuscleGroup;
}
