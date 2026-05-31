package org.example.tnal_prochum.repository;

import org.example.tnal_prochum.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findTopByActiveTrueOrderByMeetingTimeDesc();
}