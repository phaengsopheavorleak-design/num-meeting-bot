package org.example.tnal_prochum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String chatId;

    private String firstName;
    private String username;

    public Participant() {}

    public Participant(String chatId, String firstName, String username) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}