package org.example.tnal_prochum.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime meetingTime;
    private boolean active;

    public Meeting() {}

    public Meeting(String title, LocalDateTime meetingTime) {
        this.title = title;
        this.meetingTime = meetingTime;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getMeetingTime() { return meetingTime; }
    public void setMeetingTime(LocalDateTime meetingTime) { this.meetingTime = meetingTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
