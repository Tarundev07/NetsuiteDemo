package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.ApplicationSequence;
import com.atomicnorth.hrm.tenant.repository.ApplicationSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Year;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final ApplicationSequenceRepository sequenceRepository;

    @Transactional
    public String generateSequence(String type, Integer division) {
        Optional<ApplicationSequence> optionalApplicationSequence = (division != null)
                ? sequenceRepository.findByTypeAndDivision(type, division)
                : sequenceRepository.findByTypeAndDivisionIsNull(type);

        ApplicationSequence sequence = optionalApplicationSequence
                .or(() -> sequenceRepository.findByTypeAndDivisionIsNull(type))
                .orElseThrow(() -> new EntityNotFoundException("No sequence config found for type: " + type));

        Integer current = sequence.getCurrentNumber() != null
                ? sequence.getCurrentNumber()
                : sequence.getStartNumber() - sequence.getIncrement();

        Integer next = current + sequence.getIncrement();
        sequence.setCurrentNumber(next);
        sequenceRepository.save(sequence);

        return sequence.getPrefix() + String.format("%04d", next);
    }

    @Transactional
    public String generateSequenceWithYear(String type, Integer division) {
        Optional<ApplicationSequence> optionalApplicationSequence = (division != null)
                ? sequenceRepository.findByTypeAndDivision(type, division)
                : sequenceRepository.findByTypeAndDivisionIsNull(type);

        ApplicationSequence sequence = optionalApplicationSequence
                .or(() -> sequenceRepository.findByTypeAndDivisionIsNull(type))
                .orElseThrow(() -> new EntityNotFoundException("No sequence config found for type: " + type));

        Integer current = (sequence.getCurrentNumber() != null)
                ? sequence.getCurrentNumber()
                : sequence.getStartNumber() - sequence.getIncrement();

        Integer next = current + sequence.getIncrement();
        sequence.setCurrentNumber(next);
        sequenceRepository.save(sequence);

        int currentYear = Year.now().getValue();

        return String.format("%s-%d-%05d", sequence.getPrefix(), currentYear, next);
    }

    public String previewNewRequest(String type, Integer division) {
        Optional<ApplicationSequence> optionalApplicationSequence = (division != null)
                ? sequenceRepository.findByTypeAndDivision(type, division)
                : sequenceRepository.findByTypeAndDivisionIsNull(type);

        ApplicationSequence seq = optionalApplicationSequence
                .or(() -> sequenceRepository.findByTypeAndDivisionIsNull(type))
                .orElseThrow(() -> new EntityNotFoundException("No sequence config found for type: " + type));

        int next = ((seq.getCurrentNumber() != null) ? seq.getCurrentNumber()
                : seq.getStartNumber() - seq.getIncrement()) + seq.getIncrement();

        int currentYear = Year.now().getValue();
        return String.format("%s-%d-%05d", seq.getPrefix(), currentYear, next);
    }
}
