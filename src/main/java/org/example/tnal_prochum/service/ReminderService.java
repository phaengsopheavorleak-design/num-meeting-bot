package org.example.tnal_prochum.service;

import org.example.tnal_prochum.model.Meeting;
import org.example.tnal_prochum.model.Rsvp;
import org.example.tnal_prochum.telegram.MeetingBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    @Autowired
    private RsvpService rsvpService;

    @Lazy
    @Autowired
    private MeetingBot meetingBot;

    private String formatTime(Meeting meeting) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return meeting.getMeetingTime().format(formatter);
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void checkReminders() {
        System.out.println("⏰ Scheduler is running...");

        Optional<Meeting> meetingOpt = rsvpService.getActiveMeeting();
        if (meetingOpt.isEmpty()) {
            System.out.println("⏰ No active meeting found.");
            return;
        }

        Meeting meeting = meetingOpt.get();
        LocalDateTime now = LocalDateTime.now();
        long minutesUntil = ChronoUnit.MINUTES.between(now, meeting.getMeetingTime());

        System.out.println("⏰ Minutes until meeting: " + minutesUntil);

        // 1 day before — exactly 1440 minutes, tight 10 minute window
        if (minutesUntil >= 1435 && minutesUntil <= 1445) {
            if (!meeting.isOneDayReminderSent()) {
                sendOneDayReminder(meeting);
                meeting.setOneDayReminderSent(true);
                rsvpService.saveMeeting(meeting);
            }
        }

        // 3 hours before — exactly 180 minutes, tight 10 minute window
        if (minutesUntil >= 175 && minutesUntil <= 185) {
            if (!meeting.isThreeHourReminderSent()) {
                sendThreeHourReminder(meeting);
                meeting.setThreeHourReminderSent(true);
                rsvpService.saveMeeting(meeting);
            }
        }

        // 5 minutes before — exactly 5 minutes
        if (minutesUntil >= 4 && minutesUntil <= 6) {
            if (!meeting.isFiveMinuteReminderSent()) {
                sendFiveMinuteReminder(meeting);
                meeting.setFiveMinuteReminderSent(true);
                rsvpService.saveMeeting(meeting);
            }
        }

        // Meeting started — exactly 0 minutes
        if (minutesUntil >= -1 && minutesUntil <= 1) {
            if (!meeting.isStartedNotificationSent()) {
                sendMeetingStarted(meeting);
                meeting.setStartedNotificationSent(true);
                rsvpService.saveMeeting(meeting);
            }
        }

        // Mark inactive 10 minutes after meeting started
        if (minutesUntil <= -10) {
            meeting.setActive(false);
            rsvpService.saveMeeting(meeting);
        }
    }

    private void sendOneDayReminder(Meeting meeting) {
        System.out.println("📢 Sending 1-day reminder...");

        List<Rsvp> accepted = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.ACCEPT);
        for (Rsvp rsvp : accepted) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "⏰ Reminder: Tomorrow is the meeting!\n\n" +
                            "📋 " + meeting.getTitle() + "\n" +
                            "🕐 " + formatTime(meeting) + "\n\n" +
                            "We look forward to seeing you! ✅"
            );
        }

        List<Rsvp> maybe = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.MAYBE);
        for (Rsvp rsvp : maybe) {
            meetingBot.sendTwoButtons(rsvp.getChatId(),
                    "⏰ The meeting is tomorrow!\n\n" +
                            "📋 " + meeting.getTitle() + "\n" +
                            "🕐 " + formatTime(meeting) + "\n\n" +
                            "⚠️ This is your last chance to decide!\n" +
                            "Please choose — Maybe is no longer available:"
            );
        }
    }

    private void sendThreeHourReminder(Meeting meeting) {
        System.out.println("📢 Sending 3-hour reminder...");

        List<Rsvp> accepted = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.ACCEPT);
        for (Rsvp rsvp : accepted) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "⏰ Meeting starts in 3 hours!\n\n" +
                            "📋 " + meeting.getTitle() + "\n" +
                            "🕐 " + formatTime(meeting) + "\n\n" +
                            "Please be on time! ✅"
            );
        }

        List<Rsvp> maybe = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.MAYBE);
        for (Rsvp rsvp : maybe) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "⏰ Meeting starts in 3 hours!\n\n" +
                            "📋 " + meeting.getTitle() + "\n" +
                            "🕐 " + formatTime(meeting) + "\n\n" +
                            "📲 If you wish to attend, please register your attendance\n" +
                            "by scanning the QR code at the meeting room entrance."
            );
        }
    }

    private void sendFiveMinuteReminder(Meeting meeting) {
        System.out.println("📢 Sending 5-minute reminder...");

        List<Rsvp> accepted = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.ACCEPT);
        for (Rsvp rsvp : accepted) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "⏰ Meeting starts in 5 minutes!\n\n" +
                            "📋 " + meeting.getTitle() + "\n\n" +
                            "Please head to the meeting room now! ✅"
            );
        }

        List<Rsvp> maybe = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.MAYBE);
        for (Rsvp rsvp : maybe) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "⏰ Meeting starts in 5 minutes!\n\n" +
                            "📋 " + meeting.getTitle() + "\n\n" +
                            "📲 If you wish to attend, please register your attendance\n" +
                            "by scanning the QR code at the meeting room entrance."
            );
        }
    }

    private void sendMeetingStarted(Meeting meeting) {
        System.out.println("🟢 Sending meeting started message...");

        List<Rsvp> accepted = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.ACCEPT);
        for (Rsvp rsvp : accepted) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "🟢 The meeting has started!\n\n" +
                            "📋 " + meeting.getTitle() + "\n\n" +
                            "Please join now! See you there 👋"
            );
        }

        List<Rsvp> maybe = rsvpService.getByStatus(meeting.getId(), Rsvp.Status.MAYBE);
        for (Rsvp rsvp : maybe) {
            meetingBot.sendMessage(rsvp.getChatId(),
                    "🟢 The meeting has started!\n\n" +
                            "📋 " + meeting.getTitle() + "\n\n" +
                            "📲 If you wish to attend, please register your attendance\n" +
                            "by scanning the QR code at the meeting room entrance."
            );
        }
    }
}