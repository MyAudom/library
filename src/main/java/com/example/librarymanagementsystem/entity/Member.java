package com.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;

    @Column(name = "registered_date")
    private LocalDate registeredDate;

    // Constructors
    public Member() {
        this.registeredDate = LocalDate.now(); // Set registration date automatically
    }

    public Member(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(LocalDate registeredDate) { this.registeredDate = registeredDate; }
}