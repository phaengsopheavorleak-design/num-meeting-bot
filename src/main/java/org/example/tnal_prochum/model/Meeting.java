package org.example.tnal_prochum.model;

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

    // Flags to prevent duplicate messages
    private boolean oneDayReminderSent = false;
    private boolean threeHourReminderSent = false;
    private boolean fiveMinuteReminderSent = false;
    private boolean startedNotificationSent = false;

    public Meeting() {}

    public Meeting(String title, LocalDateTime meetingTime) {
        this.title = title;
        this.meetingTime = meetingTime;
        this.active = true;
        this.oneDayReminderSent = false;
        this.threeHourReminderSent = false;
        this.fiveMinuteReminderSent = false;
        this.startedNotificationSent = false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getMeetingTime() { return meetingTime; }
    public void setMeetingTime(LocalDateTime meetingTime) { this.meetingTime = meetingTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isOneDayReminderSent() { return oneDayReminderSent; }
    public void setOneDayReminderSent(boolean v) { this.oneDayReminderSent = v; }

    public boolean isThreeHourReminderSent() { return threeHourReminderSent; }
    public void setThreeHourReminderSent(boolean v) { this.threeHourReminderSent = v; }

    public boolean isFiveMinuteReminderSent() { return fiveMinuteReminderSent; }
    public void setFiveMinuteReminderSent(boolean v) { this.fiveMinuteReminderSent = v; }

    public boolean isStartedNotificationSent() { return startedNotificationSent; }
    public void setStartedNotificationSent(boolean v) { this.startedNotificationSent = v; }
}