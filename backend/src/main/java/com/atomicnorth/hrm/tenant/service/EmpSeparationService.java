package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.tenant.domain.EmpSeparation;
import com.atomicnorth.hrm.tenant.repository.EmpSeparationRepository;
import com.atomicnorth.hrm.tenant.service.dto.EmpSeparationDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmpSeparationService {

    private final Logger log = LoggerFactory.getLogger(EmpSeparationService.class);
    @Autowired
    private EmpSeparationRepository empSeparationRepository;
    @Autowired
    private ModelMapper modelMapper;

    public EmpSeparationDTO createEmpSeparation(EmpSeparationDTO dto) {
        validateRelievingDate(dto.getResignationDate(), dto.getRelievingDate());
        validateExitInterviewDate(dto.getExitInterviewHeldOn(), dto.getResignationDate(), dto.getRelievingDate());
        EmpSeparation empSeparation = modelMapper.map(dto, EmpSeparation.class);
        empSeparation.setCreatedDate(LocalDate.now()); // Set created date to current date
        empSeparation.setEmployeeId(dto.getEmployeeId());
        EmpSeparation savedEmpSeparation = empSeparationRepository.save(empSeparation);
        log.info("Employee Separation created with ID: {}", savedEmpSeparation.getId());
        return modelMapper.map(savedEmpSeparation, EmpSeparationDTO.class);
    }

    private void validateRelievingDate(LocalDate resignationDate, LocalDate relievingDate) {
        LocalDate expectedRelievingDate = resignationDate.plusDays(59);
        if (!relievingDate.equals(expectedRelievingDate)) {
            throw new IllegalArgumentException(
                    "Relieving date must be exactly 60 days from the resignation date, Expected: " + expectedRelievingDate
            );
        }
    }

    private void validateExitInterviewDate(LocalDate exitInterviewHeldOn, LocalDate resignationDate, LocalDate relievingDate) {
        if (exitInterviewHeldOn != null) {
            if (exitInterviewHeldOn.isBefore(resignationDate) || exitInterviewHeldOn.isAfter(relievingDate)) {
                throw new IllegalArgumentException(
                        "Exit interview date must be between resignation date and relieving date (inclusive)."
                );
            }
        }
    }
}
