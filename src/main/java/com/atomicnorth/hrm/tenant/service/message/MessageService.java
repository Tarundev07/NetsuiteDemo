package com.atomicnorth.hrm.tenant.service.message;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.domain.message.Message;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.repository.message.MessageRepository;
import com.atomicnorth.hrm.tenant.service.dto.message.MessageDTO;
import com.atomicnorth.hrm.tenant.service.dto.message.MessageWrapperDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageService {

    private final MessageRepository repository;
    private final ModelMapper modelMapper;
    private final SupraTranslationCommonServices translationCommonServices;

    private String generateLangCode(String text, String code) {
        return "MSG-" + text.toUpperCase() + "-" + code;
    }

    public List<MessageDTO> saveMessages(MessageWrapperDTO wrapperDto) {
        List<MessageDTO> savedMessages = new ArrayList<>();

        if (wrapperDto.getMessageDTOS() == null || wrapperDto.getMessageDTOS().isEmpty()) {
            throw new BadApiRequestException("No messages found to save.");
        }
        for (MessageDTO dto : wrapperDto.getMessageDTOS()) {

            Optional<Message> existingByCode = repository.findByMessageCode(dto.getMessageCode());
            if (existingByCode.isPresent()) {
                Message existing = existingByCode.get();

                if (dto.getMessageId() == null) {
                    throw new BadApiRequestException("Message code '" + dto.getMessageCode() + "' already exists.");
                }

                if (!existing.getMessageId().equals(dto.getMessageId())) {
                    throw new BadApiRequestException(
                            "Message code '" + dto.getMessageCode() + "' already exists for another record."
                    );
                }
            }
            Message message = (dto.getMessageId() != null) ? repository.findById(dto.getMessageId()).orElse(new Message()) : new Message();

            modelMapper.map(dto, message);
            message.setMessageCode(dto.getMessageCode().toUpperCase());
            message.setModuleId(wrapperDto.getModuleId());
            message.setModuleFunctionId(wrapperDto.getModuleFunctionId());
            String generatedDescription = generateLangCode(dto.getMessageCode(), "D");
            String generatedText = generateLangCode(dto.getMessageCode(), "M");
            message.setMessageDescriptionCode(generatedDescription);
            message.setMessageTextCode(generatedText);

            Message saved = repository.save(message);

            Map<String, String> translationDataMap = new HashMap<>();
            translationDataMap.put(saved.getMessageTextCode(), dto.getMessageMeaning());
            translationDataMap.put(saved.getMessageDescriptionCode(), dto.getMessageDescription());

            boolean translationSuccess = translationCommonServices.updateTranslationData(translationDataMap);
            if (!translationSuccess) {
                throw new BadApiRequestException("Translation failed for message code: " + dto.getMessageCode());
            }
            savedMessages.add(modelMapper.map(saved, MessageDTO.class));
        }

        return savedMessages;
    }

    public Map<String, Object> getAllMessages(String sortBy, String sortDir, String searchField, String searchKeyword, Pageable pageable, Integer moduleId, Integer functionId) {
        List<Message> messageList;
        if (moduleId != null && functionId != null) {
            messageList = repository.findByModuleIdAndModuleFunctionId(moduleId, functionId);
        } else if (moduleId != null) {
            messageList = repository.findByModuleId(moduleId);
        } else {
            messageList = repository.findAll();
        }
        List<MessageDTO> dtoList = messageList.stream().map(m -> {
            MessageDTO dto = modelMapper.map(m, MessageDTO.class);
            String meaning = translationCommonServices.getDescription(1, m.getMessageTextCode());
            String description = translationCommonServices.getDescription(1, m.getMessageDescriptionCode());
            dto.setMessageMeaning(meaning);
            dto.setMessageDescription(description);
            return dto;
        }).collect(Collectors.toList());

        if (searchField != null && searchKeyword != null && !searchField.isBlank() && !searchKeyword.isBlank()) {
            dtoList = dtoList.stream().filter(dto -> {
                try {
                    Field field = MessageDTO.class.getDeclaredField(searchField);
                    field.setAccessible(true);
                    Object fieldValue = field.get(dto);
                    return fieldValue != null && fieldValue.toString().toLowerCase().contains(searchKeyword.toLowerCase());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        if (sortBy != null && !sortBy.isBlank()) {
            try {
                Field sortField = MessageDTO.class.getDeclaredField(sortBy);
                sortField.setAccessible(true);

                Comparator<MessageDTO> comparator = Comparator.comparing(dto -> {
                    try {
                        Object val = sortField.get(dto);
                        return (Comparable<Object>) val;
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Comparator.naturalOrder()));

                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                dtoList.sort(comparator);

            } catch (NoSuchFieldException e) {
                System.out.println(e.getMessage());
            }
        }
        int totalItems = dtoList.size();
        int currentPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        List<MessageDTO> paginatedResult = (startIndex < totalItems) ? dtoList.subList(startIndex, endIndex) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("result", paginatedResult);
        response.put("currentPage", currentPage + 1);
        response.put("pageSize", pageSize);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);

        return response;
    }

    public List<Map<String, Object>> messageCodeList() {
        return repository.findAll().stream().map(m -> {
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", m.getMessageId());
            result.put("messageCode", m.getMessageCode());
            return result;
        }).collect(Collectors.toList());
    }

    public List<MessageDTO> getMessagesByFunctionId(Integer functionId) {
        List<Message> messageList = repository.findByModuleFunctionId(functionId);
        return messageList.stream().map(m -> {
            MessageDTO dto = modelMapper.map(m, MessageDTO.class);
            String meaning = translationCommonServices.getDescription(1, m.getMessageTextCode());
            String description = translationCommonServices.getDescription(1, m.getMessageDescriptionCode());
            dto.setMessageMeaning(meaning);
            dto.setMessageDescription(description);
            return dto;
        }).collect(Collectors.toList());
    }
}
