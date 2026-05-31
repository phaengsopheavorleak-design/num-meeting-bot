package org.example.tnal_prochum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rsvp")
public class Rsvp {

    public enum Status { ACCEPT, DECLINE, MAYBE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;
    private String firstName;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long meetingId;

    public Rsvp() {}

    public Rsvp(String chatId, String firstName, Status status, Long meetingId) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.status = status;
        this.meetingId = meetingId;
    }

    public Long getId() { return id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
}