package com.atomicnorth.hrm.tenant.service.employement;


import com.atomicnorth.hrm.tenant.domain.employement.EmployeeAddress;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.employement.EmployeeAddressRepo;
import com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO.Ses_M00_Addresses_Request;
import com.atomicnorth.hrm.tenant.service.dto.employement.employeeAddressDTO.Ses_M00_Addresses_Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeAddressService {

    @Autowired
    private EmployeeAddressRepo employeeAddressRepo;

    @Transactional
    public List<Ses_M00_Addresses_Request> saveOrUpdateAddress(List<Ses_M00_Addresses_Request> addressDTOs) {
        UserLoginDetail tokenHolder = SessionHolder.getUserLoginDetail();
        List<Ses_M00_Addresses_Request> updatedDTOs = new ArrayList<>();

        for (Ses_M00_Addresses_Request dto : addressDTOs) {
            EmployeeAddress sesM00Addresses = dto.getAddressId() != null
                    ? employeeAddressRepo.findById(dto.getAddressId()).orElse(new EmployeeAddress())
                    : new EmployeeAddress();

            mapToEntity(dto, sesM00Addresses);

            if (sesM00Addresses.getAddressId() == null) {
                sesM00Addresses.setCreatedBy(String.valueOf(tokenHolder.getUsername()));
                sesM00Addresses.setCreatedDate(Instant.now());
            }
            sesM00Addresses.setLastUpdatedBy(String.valueOf(tokenHolder.getUsername()));
            sesM00Addresses.setLastUpdatedDate(Instant.now());

            EmployeeAddress savedEntity = employeeAddressRepo.save(sesM00Addresses);

            dto.setAddressId(savedEntity.getAddressId());
            updatedDTOs.add(dto);
        }

        return updatedDTOs;
    }

    private void mapToEntity(Ses_M00_Addresses_Request dto, EmployeeAddress entity) {
        entity.setUsername(dto.getUsername());
        entity.setIsDeleted(dto.getIsDeleted());
        entity.setCountryId(dto.getCountryId());
        entity.setCityId(dto.getCityId());
        entity.setPincode(dto.getPincode());
        entity.setStateId(dto.getStateId());
        entity.setAddressText(dto.getAddressText());
        entity.setAddressId(dto.getAddressId());
        entity.setAddressTypeCode(dto.getAddressTypeCode());
    }

    @Transactional(readOnly = true)
    public List<Ses_M00_Addresses_Response> getAllAddressesByUsername(Integer username) {
        List<EmployeeAddress> addresses = employeeAddressRepo.findByUsername(username);
        return addresses.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Ses_M00_Addresses_Response mapToResponseDTO(EmployeeAddress savedAddress) {
        Ses_M00_Addresses_Response responseDTO = new Ses_M00_Addresses_Response();
        responseDTO.setAddressId(savedAddress.getAddressId());
        responseDTO.setUsername(savedAddress.getUsername());
        responseDTO.setAddressTypeCode(savedAddress.getAddressTypeCode());
        responseDTO.setAddressText(savedAddress.getAddressText());
        responseDTO.setIsDeleted(savedAddress.getIsDeleted());
        responseDTO.setEntityId(savedAddress.getEntityId());
        responseDTO.setClientId(savedAddress.getClientId());
        responseDTO.setLastUpdateSessionId(savedAddress.getLastUpdateSessionId());
        responseDTO.setCreatedDate(savedAddress.getCreatedDate());
        responseDTO.setCreatedBy(savedAddress.getCreatedBy());
        responseDTO.setCityId(savedAddress.getCityId());
        responseDTO.setPincode(savedAddress.getPincode());
        responseDTO.setCountryId(savedAddress.getCountryId());
        responseDTO.setStateId(savedAddress.getStateId());
        responseDTO.setLastUpdatedBy(savedAddress.getLastUpdatedBy());
        responseDTO.setLastUpdatedDate(savedAddress.getLastUpdatedDate());
        responseDTO.setRecordInfo(savedAddress.getRecordInfo());
        return responseDTO;
    }

}
