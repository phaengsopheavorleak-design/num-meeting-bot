package org.example.tnal_prochum.repository;





import org.example.tnal_prochum.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByChatId(String chatId);
    Optional<Participant> findByUsername(String username);
    Optional<Participant> findByFirstName(String firstName);
}
