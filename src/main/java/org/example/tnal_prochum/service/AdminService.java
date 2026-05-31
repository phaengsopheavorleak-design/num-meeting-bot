package org.example.tnal_prochum.service;
import jakarta.annotation.PostConstruct;
import org.example.tnal_prochum.model.AdminConfig;
import org.example.tnal_prochum.repository.AdminConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminConfigRepository adminConfigRepository;

    @Value("${telegram.admin.chatid}")
    private String defaultAdminChatId;

    // Get current admin chatid from database
    // If not set yet, use the one from application.properties
    public String getAdminChatId() {
        Optional<AdminConfig> config = adminConfigRepository.findById(1L);
        return config.map(AdminConfig::getAdminChatId).orElse(defaultAdminChatId);
    }

    // Change admin to new chatid
    public void setAdminChatId(String newChatId) {
        AdminConfig config = adminConfigRepository.findById(1L)
                .orElse(new AdminConfig());
        config.setAdminChatId(newChatId);
        adminConfigRepository.save(config);
        System.out.println("✅ Admin changed to: " + newChatId);
    }
    @PostConstruct
    public void init() {
        // On startup, always sync Railway Variable to database
        Optional<AdminConfig> config = adminConfigRepository.findById(1L);
        if (config.isEmpty()) {
            // First time — save the default admin from properties
            adminConfigRepository.save(new AdminConfig(defaultAdminChatId));
            System.out.println("✅ Admin initialized: " + defaultAdminChatId);
        }
    }
}
