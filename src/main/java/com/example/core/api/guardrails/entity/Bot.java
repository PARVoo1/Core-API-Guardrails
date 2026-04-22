package com.example.core.api.guardrails.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bots")
@Data
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
    @SequenceGenerator(name = "author_seq", sequenceName = "author_global_sequence", allocationSize = 1)
    private Long id;

    private String name;
    @Column(name = "persona_description")
    private String personaDescription;
}
