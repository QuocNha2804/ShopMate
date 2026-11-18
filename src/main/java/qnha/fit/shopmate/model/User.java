package qnha.fit.shopmate.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String code;
    private String username;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String email;
    private String role; // "ADMIN" or "STAFF"
    private String status; // "ACTIVE" or "LOCKED"
    private LocalDateTime createdAt;

    public User() {
    }

    public User(int id, String code, String username, String passwordHash, String fullName,
                String phone, String email, String role, String status, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + code + ")";
    }
}
