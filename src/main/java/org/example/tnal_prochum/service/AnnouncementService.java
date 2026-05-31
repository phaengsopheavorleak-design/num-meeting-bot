package org.example.tnal_prochum.service;



import org.example.tnal_prochum.model.Participant;
import org.example.tnal_prochum.telegram.MeetingBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private ParticipantService participantService;

    @Lazy
    @Autowired
    private MeetingBot meetingBot;

    // Store latest announcement in memory
    private String latestAnnouncement = null;

    public void saveAndSendAnnouncement(String text) {
        // Save it so new participants can see it on /start
        this.latestAnnouncement = text;

        // Send to all current participants
        List<Participant> participants = participantService.getAllParticipants();
        for (Participant participant : participants) {
            meetingBot.sendMessage(participant.getChatId(), "📢 Announcement:\n\n" + text);
        }

        System.out.println("📢 Announcement sent to " + participants.size() + " participants");
    }

    public String getLatestAnnouncement() {
        return latestAnnouncement;
    }
}
