package org.example.tnal_prochum.model;



import jakarta.persistence.*;

@Entity
@Table(name = "admin_config")
public class AdminConfig {

    @Id
    private Long id = 1L; // only one row ever

    private String adminChatId;

    public AdminConfig() {}

    public AdminConfig(String adminChatId) {
        this.adminChatId = adminChatId;
    }

    public Long getId() { return id; }
    public String getAdminChatId() { return adminChatId; }
    public void setAdminChatId(String adminChatId) { this.adminChatId = adminChatId; }
}
