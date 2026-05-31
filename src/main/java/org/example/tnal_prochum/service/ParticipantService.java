package org.example.tnal_prochum.service;



import org.example.tnal_prochum.model.Participant;
import org.example.tnal_prochum.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    public void saveParticipant(String chatId, String firstName, String username) {
        if (participantRepository.findByChatId(chatId).isEmpty()) {
            participantRepository.save(new Participant(chatId, firstName, username));
            System.out.println("✅ New participant saved: " + firstName);
        }
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public Optional<Participant> findByUsername(String username) {
        return participantRepository.findByUsername(username);
    }

    public Optional<Participant> findByFirstName(String firstName) {
        return participantRepository.findByFirstName(firstName);
    }
}
