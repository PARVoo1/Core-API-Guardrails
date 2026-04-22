package com.example.core.api.guardrails.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
    @SequenceGenerator(name = "author_seq", sequenceName = "author_global_sequence", allocationSize = 1)
    private Long id;
    private String userName;
    @Column(name ="is_premium")
    private boolean isPremium;
}
