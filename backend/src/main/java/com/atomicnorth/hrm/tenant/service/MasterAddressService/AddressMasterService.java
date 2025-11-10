package com.atomicnorth.hrm.tenant.service.MasterAddressService;

import com.atomicnorth.hrm.tenant.domain.MasterAddress.AddressMaster;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.MasterAddressRepo.AddressMasterRepository;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressRequestDTO;
import com.atomicnorth.hrm.tenant.service.dto.MasterAddressDTO.AddressResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AddressMasterService {

    @Autowired
    private AddressMasterRepository addressMasterRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Map<String, Object> getPaginatedAddresses(Pageable pageable, String searchColumn, String searchValue) {
        Specification<AddressMaster> spec = buildAddressSpecification(searchColumn, searchValue);

        Page<AddressMaster> addressPage = addressMasterRepository.findAll(spec, pageable);

        List<AddressResponseDTO> addressDTOList = addressPage.getContent()
                .stream()
                .map(address -> modelMapper.map(address, AddressResponseDTO.class))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", addressDTOList);
        response.put("currentPage", addressPage.getNumber() + 1);  // Return page starting from 1
        response.put("totalItems", addressPage.getTotalElements());
        response.put("totalPages", addressPage.getTotalPages());

        return response;
    }

    public static Specification<AddressMaster> buildAddressSpecification(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            if (searchField == null || searchKeyword == null || searchKeyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Predicate predicate = null;
            String lowerKeyword = "%" + searchKeyword.toLowerCase() + "%";


            if ("country".equalsIgnoreCase(searchField)) {
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), lowerKeyword);
            } else if ("state".equalsIgnoreCase(searchField)) {
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), lowerKeyword);
            } else if ("city".equalsIgnoreCase(searchField)) {
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), lowerKeyword);
            } else {

                try {
                    Field field = AddressMaster.class.getDeclaredField(searchField);
                    if (field.getType().equals(String.class)) {
                        predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), lowerKeyword);
                    } else {
                        predicate = criteriaBuilder.equal(root.get(searchField), searchKeyword);
                    }
                } catch (NoSuchFieldException e) {
                    System.err.println("Invalid field for search: " + searchField);
                    predicate = criteriaBuilder.conjunction(); // return empty predicate to avoid crash
                }
            }

            return predicate;
        };
    }



    @Transactional
    public AddressResponseDTO saveOrUpdateAddress(AddressRequestDTO request) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());

        try {
            // If an address ID exists, update the existing record, otherwise create a new one.
            AddressMaster address = (request.getAddressId() != null)
                    ? addressMasterRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Address not found"))
                    : new AddressMaster();

            modelMapper.map(request, address);
            address.setCreatedBy(username);
            address.setLastUpdatedBy(username);
            address.setLastUpdatedDate(LocalDateTime.now());

            address = addressMasterRepository.save(address);

            return modelMapper.map(address, AddressResponseDTO.class);

        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", ex);
        }
    }

    public AddressResponseDTO getAddressById(Integer id) {
        AddressMaster address = addressMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + id));

        return modelMapper.map(address, AddressResponseDTO.class);
    }

    public List<Map<String, Object>> getAddressDropdownList() {
        return addressMasterRepository.findAll()
                .stream().map(address -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("addressId", address.getAddressId());
                    result.put("addressName", address.getFullAddress());
                    return result;
                }).collect(Collectors.toList());
    }
}

