package github.oftx.backgroundcamera.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    // NEW: Field to store the hashed device-specific authentication token
    @Column(length = 100)
    private String authTokenHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // NEW: Getter and Setter for the auth token hash
    public String getAuthTokenHash() { return authTokenHash; }
    public void setAuthTokenHash(String authTokenHash) { this.authTokenHash = authTokenHash; }
}