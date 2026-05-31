package org.example.tnal_prochum.service;

import org.example.tnal_prochum.model.Meeting;
import org.example.tnal_prochum.model.Rsvp;
import org.example.tnal_prochum.repository.MeetingRepository;
import org.example.tnal_prochum.repository.RsvpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RsvpService {

    @Autowired
    private RsvpRepository rsvpRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    public Meeting createMeeting(String title, LocalDateTime meetingTime) {
        Meeting meeting = new Meeting(title, meetingTime);
        return meetingRepository.save(meeting);
    }

    public Optional<Meeting> getActiveMeeting() {
        return meetingRepository.findTopByActiveTrueOrderByMeetingTimeDesc();
    }

    public void saveMeeting(Meeting meeting) {
        meetingRepository.save(meeting);
    }

    public void saveOrUpdateRsvp(String chatId, String firstName, Rsvp.Status status, Long meetingId) {
        Optional<Rsvp> existing = rsvpRepository.findByChatIdAndMeetingId(chatId, meetingId);
        if (existing.isPresent()) {
            existing.get().setStatus(status);
            rsvpRepository.save(existing.get());
        } else {
            rsvpRepository.save(new Rsvp(chatId, firstName, status, meetingId));
        }
    }

    public Optional<Rsvp> getRsvp(String chatId, Long meetingId) {
        return rsvpRepository.findByChatIdAndMeetingId(chatId, meetingId);
    }

    public List<Rsvp> getByStatus(Long meetingId, Rsvp.Status status) {
        return rsvpRepository.findByMeetingIdAndStatus(meetingId, status);
    }

    public List<Rsvp> getAll(Long meetingId) {
        return rsvpRepository.findByMeetingId(meetingId);
    }
}