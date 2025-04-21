package ru.rsreu.MosaiCraft.entities;

import jakarta.persistence.*;

@Entity
@Table
public class UserTamplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;
}
