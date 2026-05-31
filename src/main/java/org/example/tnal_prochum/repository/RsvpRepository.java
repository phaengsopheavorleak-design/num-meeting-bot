package org.example.tnal_prochum.repository;

import org.example.tnal_prochum.model.Rsvp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RsvpRepository extends JpaRepository<Rsvp, Long> {
    Optional<Rsvp> findByChatIdAndMeetingId(String chatId, Long meetingId);
    List<Rsvp> findByMeetingIdAndStatus(Long meetingId, Rsvp.Status status);
    List<Rsvp> findByMeetingId(Long meetingId);
}