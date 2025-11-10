package com.atomicnorth.hrm.tenant.service.employeeExit;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.asset.EmpAssetAssignment;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitInterview;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitInterviewFeedback;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitInterviewers;
import com.atomicnorth.hrm.tenant.domain.employeeExit.EmpExitRequest;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitInterviewRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeExit.EmpExitRequestRepository;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitInterviewDTO;
import com.atomicnorth.hrm.tenant.service.dto.employeeExit.EmpExitInterviewFeedbackDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpExitInterviewService {

    @Autowired
    private EmpExitInterviewRepository interviewRepo;
    @Autowired
    private EmpExitRequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public void createInterview(Integer exitRequestId) {
        EmpExitInterview interview = new EmpExitInterview();
        interview.setExitRequestId(exitRequestId);
        interview.setStatus("Pending");
        interviewRepo.save(interview);
    }

    public boolean existsByExitRequestId(Integer exitRequestId) {
        return interviewRepo.existsByExitRequestId(exitRequestId);
    }

    @Transactional
    public EmpExitInterviewDTO getExitInterviewDetails(Integer exitRequestId) {
        EmpExitInterview exitInterview = interviewRepo.findByExitRequestId(exitRequestId).orElseThrow(() -> new EntityNotFoundException("No request found."));
        EmpExitRequest exitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("No exit request found."));
        Employee employee = employeeRepository.findByEmployeeId(exitRequest.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found."));

        EmpExitInterviewDTO dto = new EmpExitInterviewDTO();
        dto.setId(exitInterview.getId());
        dto.setEmployeeName(employee.getFullName() + " (" + employee.getEmployeeNumber() + ")");
        dto.setExitRequestNumber(exitRequest.getExitRequestNumber());
        dto.setDepartment(employee.getDepartment().getDname());
        dto.setDesignation(employee.getDesignation().getDesignationName());
        dto.setReportingManager(employee.getReportingManager().getFullName() + " (" + employee.getReportingManager().getEmployeeNumber() + ")");
        dto.setJoiningDate(employee.getEffectiveStartDate());
        dto.setLastWorkingDate(exitRequest.getLastWorkingDate());
        dto.setExitInterviewStatus(exitRequest.getExitInterviewStatus());
        dto.setResignationType(exitRequest.getExitType());
        dto.setReason(exitRequest.getExitReason());
        dto.setNoticePeriod(employee.getNoticeDays());
        dto.setStatus(exitRequest.getStatus());
        dto.setInterviewDate(exitInterview.getInterviewDate());
        dto.setInterviewType(exitInterview.getInterviewType());
        dto.setSuggestions(exitInterview.getSuggestions());
        dto.setFinalComments(exitInterview.getFinalComments());
        dto.setInterviewStatus(exitInterview.getStatus());
        List<EmpExitInterviewFeedbackDTO> feedbackDTOS = exitInterview.getEmpExitInterviewFeedbacks().stream().map(feedback -> {
            EmpExitInterviewFeedbackDTO feedbackDTO = new EmpExitInterviewFeedbackDTO();
            feedbackDTO.setInterviewFeedbackId(feedback.getInterviewFeedbackId());
            feedbackDTO.setInterviewId(exitInterview.getId());
            feedbackDTO.setFeedbackType(feedback.getFeedbackType());
            feedbackDTO.setFeedbackValue(feedback.getFeedbackValue());
            return feedbackDTO;
        }).collect(Collectors.toList());
        List<Integer> interviewers = exitInterview.getEmpExitInterviewers().stream().map(EmpExitInterviewers::getEmployeeId).collect(Collectors.toList());
        dto.setInterviewerId(interviewers);
        dto.setFeedbackDTOList(feedbackDTOS);
        return dto;
    }

    @Transactional
    public EmpExitInterview saveExitInterview(EmpExitInterviewDTO exitInterviewDTO) {
        EmpExitInterview empExitInterview;
        if (exitInterviewDTO.getId() != null) {
            empExitInterview = interviewRepo.findById(exitInterviewDTO.getId()).orElse(new EmpExitInterview());
        } else {
            empExitInterview = new EmpExitInterview();
        }
        modelMapper.map(exitInterviewDTO, empExitInterview);
        empExitInterview.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitInterview.setLastUpdatedDate(Instant.now());
        empExitInterview.setStatus(exitInterviewDTO.getInterviewStatus());
        List<EmpExitInterviewers> interviewersList = exitInterviewDTO.getInterviewerId().stream().map(interviewer -> {
            EmpExitInterviewers interviewers = new EmpExitInterviewers();
            interviewers.setEmpExitInterview(empExitInterview);
            interviewers.setInterviewId(empExitInterview.getId());
            interviewers.setEmployeeId(interviewer);
            return interviewers;
        }).collect(Collectors.toList());
        List<EmpExitInterviewFeedback> feedbacks = exitInterviewDTO.getFeedbackDTOList().stream().map(feedback -> {
            EmpExitInterviewFeedback interviewFeedback = new EmpExitInterviewFeedback();
            if (feedback.getInterviewFeedbackId() != null) {
                interviewFeedback.setInterviewFeedbackId(feedback.getInterviewFeedbackId());
            }
            interviewFeedback.setFeedbackValue(feedback.getFeedbackValue());
            interviewFeedback.setFeedbackType(feedback.getFeedbackType());
            interviewFeedback.setExitInterview(empExitInterview);
            interviewFeedback.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
            interviewFeedback.setLastUpdatedDate(Instant.now());
            return interviewFeedback;
        }).collect(Collectors.toList());
        if (empExitInterview.getEmpExitInterviewFeedbacks() == null) {
            empExitInterview.setEmpExitInterviewFeedbacks(new ArrayList<>());
        } else {
            empExitInterview.getEmpExitInterviewFeedbacks().clear();
        }
        if (empExitInterview.getEmpExitInterviewers() == null) {
            empExitInterview.setEmpExitInterviewers(new ArrayList<>());
        } else {
            empExitInterview.getEmpExitInterviewers().clear();
        }
        empExitInterview.getEmpExitInterviewers().addAll(interviewersList);
        empExitInterview.getEmpExitInterviewFeedbacks().addAll(feedbacks);
        return interviewRepo.save(empExitInterview);
    }

    @Transactional
    public EmpExitRequest approveExitInterview(Integer exitRequestId) {
        EmpExitRequest empExitRequest = requestRepository.findById(exitRequestId).orElseThrow(() -> new EntityNotFoundException("Request not found."));
        empExitRequest.setExitInterviewStatus("Approved");
        empExitRequest.setLastUpdatedBy(String.valueOf(SessionHolder.getUserLoginDetail().getUsername()));
        empExitRequest.setLastUpdatedDate(Instant.now());
        return requestRepository.save(empExitRequest);
    }
}
