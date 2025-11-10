package com.atomicnorth.hrm.tenant.service.manageColumn;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.domain.manageColumn.SesM00UserManageColumns;
import com.atomicnorth.hrm.tenant.domain.manageColumn.SesM01UserManageColumnDetails;
import com.atomicnorth.hrm.tenant.repository.manageColumn.SesM00UserManageColumnsRepository;
import com.atomicnorth.hrm.tenant.repository.manageColumn.SesM01UserManageColumnDetailsRepository;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM00UserManageColumnsDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM00UserManageColumnsResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM01UserManageColumnDetailsDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.SesM01UserManageColumnDetailsResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto.SesM00UserManageColumnsGetResponseDTO;
import com.atomicnorth.hrm.tenant.service.dto.manageColumn.get_data_dto.UserManageColumnDetailsGetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManageService {
    @Autowired
    private SesM00UserManageColumnsRepository userManageColumnsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SesM01UserManageColumnDetailsRepository columnDetailsRepository;

    public boolean isExisting(Integer userId, String pageKey) {
        return userManageColumnsRepository.findByUserIdAndPageKey(userId, pageKey).isPresent();
    }

    @Transactional
    public SesM00UserManageColumnsResponseDTO createUserManageColumns(SesM00UserManageColumnsDTO userManageColumnsDTO) {
        // Convert DTO to entity and set DISPLAY_SNO before saving
        SesM00UserManageColumns userManageColumns = convertToEntity(userManageColumnsDTO);

        // Set DISPLAY_SNO for the columnDetails BEFORE saving the parent entity
        List<SesM01UserManageColumnDetails> columnDetails = userManageColumns.getColumnDetails();
        for (int i = 0; i < columnDetails.size(); i++) {
            SesM01UserManageColumnDetails details = columnDetails.get(i);
            details.setDisplaySno(i + 1); // Set sequential DISPLAY_SNO starting from 1
            details.setUserManageColumn(userManageColumns); // Associate the child with the parent
        }

        // Now save the parent entity along with its child details
        SesM00UserManageColumns savedUserManageColumns = userManageColumnsRepository.save(userManageColumns);

        // Convert entity to response DTO and return
        return convertToResponseDTO(savedUserManageColumns);
    }

    // Convert DTO to Entity
    private SesM00UserManageColumns convertToEntity(SesM00UserManageColumnsDTO dto) {
        SesM00UserManageColumns entity = new SesM00UserManageColumns();
        entity.setUserId(dto.getUserId());
        entity.setModuleId(dto.getModuleId());
        entity.setModuleFeatureId(dto.getModuleFeatureId());
        entity.setPageKey(dto.getPageKey());
        entity.setIsPublic(dto.getIsPublic());
        entity.setCreationDate(dto.getCreationDate());
        entity.setLastUpdateDate(dto.getLastUpdateDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setLastUpdatedBy(dto.getLastUpdatedBy());
        entity.setOperationSource(dto.getOperationSource());
        entity.setPageSection(dto.getPageSection());

        // Convert columnDetails and set DISPLAY_SNO sequentially
        List<SesM01UserManageColumnDetails> details = dto.getColumnDetails().stream()
                .map(detailDto -> {
                    SesM01UserManageColumnDetails detail = new SesM01UserManageColumnDetails();
                    detail.setColumnLookupCode(detailDto.getColumnLookupCode());
                    detail.setCreationDate(detailDto.getCreationDate());
                    detail.setLastUpdateDate(detailDto.getLastUpdateDate());
                    detail.setCreatedBy(detailDto.getCreatedBy());
                    detail.setLastUpdatedBy(detailDto.getLastUpdatedBy());
                    detail.setOperationSource(detailDto.getOperationSource());
                    detail.setIsLocked(detailDto.getIsLocked());
                    return detail;
                }).collect(Collectors.toList());
        entity.setColumnDetails(details);

        return entity;
    }

    @Transactional
    public SesM00UserManageColumnsResponseDTO updateUserManageColumns(Integer userId, String pageKey, SesM00UserManageColumnsDTO userManageColumnsDTO) {
        // Fetch the existing user manage columns
        SesM00UserManageColumns existingManageColumn = userManageColumnsRepository.findByUserIdAndPageKey(userId, pageKey)
                .orElseThrow(() -> new BadApiRequestException("User manage column not found."));

        // Update the existing user manage columns
        updateExistingManageColumn(existingManageColumn, userManageColumnsDTO);

        // Update column details and collect the updated details
        List<SesM01UserManageColumnDetailsResponseDTO> updatedColumnDetails = updateColumnDetails(existingManageColumn, userManageColumnsDTO.getColumnDetails());

        // Save updated parent entity
        SesM00UserManageColumns updatedManageColumn = userManageColumnsRepository.save(existingManageColumn);

        // Convert entity to response DTO and set the updated column details
        SesM00UserManageColumnsResponseDTO responseDTO = convertToResponseDTO(updatedManageColumn);
        responseDTO.setColumnDetails(updatedColumnDetails); // Set updated column details in response

        return responseDTO;
    }

    private void updateExistingManageColumn(SesM00UserManageColumns existingManageColumn, SesM00UserManageColumnsDTO dto) {
        existingManageColumn.setModuleId(dto.getModuleId());
        existingManageColumn.setModuleFeatureId(dto.getModuleFeatureId());
        existingManageColumn.setIsPublic(dto.getIsPublic());
        existingManageColumn.setLastUpdateDate(dto.getLastUpdateDate());
        existingManageColumn.setLastUpdatedBy(dto.getLastUpdatedBy());
        existingManageColumn.setOperationSource(dto.getOperationSource());
        existingManageColumn.setCreatedBy(dto.getCreatedBy());
        existingManageColumn.setLastUpdateDate(dto.getLastUpdateDate());
        existingManageColumn.setPageSection(dto.getPageSection());
        // Do not update userId and pageKey as per your requirements
    }

    private List<SesM01UserManageColumnDetailsResponseDTO> updateColumnDetails(SesM00UserManageColumns existingManageColumn, List<SesM01UserManageColumnDetailsDTO> columnDetailsDTOList) {
        List<SesM01UserManageColumnDetails> existingDetails = existingManageColumn.getColumnDetails();

        // Create a map for existing column lookup codes for quick access
        Map<String, SesM01UserManageColumnDetails> existingDetailMap = existingDetails.stream()
                .collect(Collectors.toMap(SesM01UserManageColumnDetails::getColumnLookupCode, detail -> detail));

        // Create a set to keep track of processed column lookup codes
        Set<String> processedCodes = new HashSet<>();

        // Loop through incoming column details
        for (SesM01UserManageColumnDetailsDTO columnDetailsDTO : columnDetailsDTOList) {
            processedCodes.add(columnDetailsDTO.getColumnLookupCode()); // Mark this code as processed

            // Check if the column detail already exists
            if (existingDetailMap.containsKey(columnDetailsDTO.getColumnLookupCode())) {
                // Update existing detail
                SesM01UserManageColumnDetails existingDetail = existingDetailMap.get(columnDetailsDTO.getColumnLookupCode());

                // Update isLocked and displaySno based on conditions
                existingDetail.setIsLocked(columnDetailsDTO.getDisplaySno() == 1 || columnDetailsDTO.getDisplaySno() == 2 ? "true" : columnDetailsDTO.getIsLocked());

                // Update displaySno only if it's specified
                if (columnDetailsDTO.getDisplaySno() != null) {
                    existingDetail.setDisplaySno(columnDetailsDTO.getDisplaySno());
                }

                // Always update creationDate, lastUpdateDate, createdBy, lastUpdatedBy, operationSource
                existingDetail.setCreationDate(columnDetailsDTO.getCreationDate());
                existingDetail.setLastUpdateDate(columnDetailsDTO.getLastUpdateDate());
                existingDetail.setCreatedBy(columnDetailsDTO.getCreatedBy());
                existingDetail.setLastUpdatedBy(columnDetailsDTO.getLastUpdatedBy());
                existingDetail.setOperationSource(columnDetailsDTO.getOperationSource());

                columnDetailsRepository.save(existingDetail);
            } else {
                // Create new detail if not found
                SesM01UserManageColumnDetails newDetail = new SesM01UserManageColumnDetails();
                newDetail.setUserManageColumn(existingManageColumn); // Associate with parent
                newDetail.setColumnLookupCode(columnDetailsDTO.getColumnLookupCode());
                newDetail.setDisplaySno(columnDetailsDTO.getDisplaySno()); // Set displaySno from incoming request
                newDetail.setCreationDate(columnDetailsDTO.getCreationDate());
                newDetail.setLastUpdateDate(columnDetailsDTO.getLastUpdateDate());
                newDetail.setCreatedBy(columnDetailsDTO.getCreatedBy());
                newDetail.setLastUpdatedBy(columnDetailsDTO.getLastUpdatedBy());
                newDetail.setIsLocked("true"); // New records are always locked
                newDetail.setOperationSource(columnDetailsDTO.getOperationSource());
                columnDetailsRepository.save(newDetail);
            }
        }

        // Update isLocked to false for existing details not in the incoming list
        for (SesM01UserManageColumnDetails existingDetail : existingDetails) {
            if (!processedCodes.contains(existingDetail.getColumnLookupCode())) {
                existingDetail.setIsLocked("false"); // Set to false for those not included in the incoming details
                columnDetailsRepository.save(existingDetail);
            }
        }

        // Collect all details (existing and new) to respond
        List<SesM01UserManageColumnDetailsResponseDTO> detailsResponse = existingDetails.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        // Add newly created details to the response, ensuring no duplicates
        for (SesM01UserManageColumnDetailsDTO columnDetailsDTO : columnDetailsDTOList) {
            if (!existingDetailMap.containsKey(columnDetailsDTO.getColumnLookupCode())) {
                SesM01UserManageColumnDetailsResponseDTO newDetailResponse = new SesM01UserManageColumnDetailsResponseDTO();
                newDetailResponse.setColumnLookupCode(columnDetailsDTO.getColumnLookupCode());
                newDetailResponse.setDisplaySno(columnDetailsDTO.getDisplaySno());
                newDetailResponse.setCreationDate(columnDetailsDTO.getCreationDate());
                newDetailResponse.setLastUpdateDate(columnDetailsDTO.getLastUpdateDate());
                newDetailResponse.setCreatedBy(columnDetailsDTO.getCreatedBy());
                newDetailResponse.setLastUpdatedBy(columnDetailsDTO.getLastUpdatedBy());
                newDetailResponse.setIsLocked("true"); // New records are always locked
                newDetailResponse.setOperationSource(columnDetailsDTO.getOperationSource());
                detailsResponse.add(newDetailResponse);
            }
        }

        return detailsResponse;
    }

    // Convert column details to response DTO with formatted header
    private SesM01UserManageColumnDetailsResponseDTO convertToResponseDTO(SesM01UserManageColumnDetails entity) {
        SesM01UserManageColumnDetailsResponseDTO detailResponseDTO = new SesM01UserManageColumnDetailsResponseDTO();
        detailResponseDTO.setUserManageColumnDetailsId(entity.getUserManageColumnDetailsId());
        detailResponseDTO.setColumnLookupCode(entity.getColumnLookupCode());

        // Use the helper method to format the column lookup code into a proper header
        detailResponseDTO.setColumnLookupCodeHeader(formatColumnLookupCodeHeader(entity.getColumnLookupCode()));

        detailResponseDTO.setDisplaySno(entity.getDisplaySno());
        detailResponseDTO.setCreationDate(entity.getCreationDate());
        detailResponseDTO.setLastUpdateDate(entity.getLastUpdateDate());
        detailResponseDTO.setCreatedBy(entity.getCreatedBy());
        detailResponseDTO.setLastUpdatedBy(entity.getLastUpdatedBy());
        detailResponseDTO.setOperationSource(entity.getOperationSource());
        detailResponseDTO.setIsLocked(entity.getIsLocked());

        return detailResponseDTO;
    }

    // Convert entity to response DTO
    private SesM00UserManageColumnsResponseDTO convertToResponseDTO(SesM00UserManageColumns entity) {
        SesM00UserManageColumnsResponseDTO responseDTO = new SesM00UserManageColumnsResponseDTO();
        responseDTO.setUserManageColumnId(entity.getUserManageColumnId());
        responseDTO.setUserId(entity.getUserId());
        responseDTO.setModuleId(entity.getModuleId());
        responseDTO.setModuleFeatureId(entity.getModuleFeatureId());
        responseDTO.setPageKey(entity.getPageKey());
        responseDTO.setIsPublic(entity.getIsPublic());
        responseDTO.setCreationDate(entity.getCreationDate());
        responseDTO.setLastUpdateDate(entity.getLastUpdateDate());
        responseDTO.setCreatedBy(entity.getCreatedBy());
        responseDTO.setLastUpdatedBy(entity.getLastUpdatedBy());
        responseDTO.setPageSection(entity.getPageSection());
        responseDTO.setOperationSource(entity.getOperationSource());
        responseDTO.setColumnDetails(entity.getColumnDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList()));

        return responseDTO;
    }

    private String formatColumnLookupCodeHeader(String columnLookupCode) {
        // Replace underscores, hyphens, and digits with spaces
        String formatted = columnLookupCode.replaceAll("[-_]", " ");
        formatted = formatted.replaceAll("\\d", ""); // Remove digits

        // Add space before any uppercase letter that is not at the start of the string
        formatted = formatted.replaceAll("([a-z])([A-Z])", "$1 $2");

        // Capitalize the first letter of each word
        formatted = Arrays.stream(formatted.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        return formatted.trim(); // Return with leading and trailing spaces removed
    }

    public SesM00UserManageColumnsGetResponseDTO getUserManageColumnsByUserIdAndPageKey(Integer userId, String pageKey) {
        // Fetch the entity from the repository
        SesM00UserManageColumns entity = userManageColumnsRepository
                .findByUserIdAndPageKey(userId, pageKey)
                .orElseThrow(() -> new NoSuchElementException("No user manage columns found"));

        // Map the entity to DTO
        return new SesM00UserManageColumnsGetResponseDTO(
                entity.getUserManageColumnId(),
                entity.getUserId(),
                entity.getPageKey(),
                entity.getIsPublic(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getCreatedBy(),
                entity.getLastUpdatedBy(),
                entity.getOperationSource(),
                entity.getPageSection()
        );
    }

    public List<UserManageColumnDetailsGetDTO> getUserManageColumnDetailsByUserManageColumnId(Integer userManageColumnId) {
        List<SesM01UserManageColumnDetails> detailsList = columnDetailsRepository.findAll()
                .stream()
                .filter(detail -> detail.getUserManageColumn().getUserManageColumnId().equals(userManageColumnId))
                .collect(Collectors.toList());

        // Map to DTOs
        return detailsList.stream()
                .map(detail -> new UserManageColumnDetailsGetDTO(
                        detail.getUserManageColumnDetailsId(),
                        detail.getColumnLookupCode(),
                        detail.getDisplaySno(),
                        detail.getCreationDate(),
                        detail.getLastUpdateDate(),
                        detail.getCreatedBy(),
                        detail.getLastUpdatedBy(),
                        detail.getOperationSource(),
                        detail.getIsLocked()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getManageColumnDetails(int userId, String pageKey, String schemaName) throws Exception {
        //String schemaName = "anpl"; // Replace with your schema name
        String procedureName = schemaName + ".GetManageColumnDetails";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             CallableStatement callableStatement = connection.prepareCall("{CALL " + procedureName + "(?, ?)}")) {

            callableStatement.setInt(1, userId);
            callableStatement.setString(2, pageKey);

            boolean hasResults = callableStatement.execute();

            while (hasResults) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    while (resultSet.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            row.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                        }
                        result.add(row);
                    }
                }
                hasResults = callableStatement.getMoreResults();
            }
        }
        return result;
    }
}
