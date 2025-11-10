package com.atomicnorth.hrm.tenant.web.rest.message;

import com.atomicnorth.hrm.exception.BadApiRequestException;
import com.atomicnorth.hrm.tenant.service.dto.message.MessageDTO;
import com.atomicnorth.hrm.tenant.service.dto.message.MessageWrapperDTO;
import com.atomicnorth.hrm.tenant.service.message.MessageService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("save-messages")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> saveMessages(@RequestBody MessageWrapperDTO dto) {
        try {
            List<MessageDTO> saved = messageService.saveMessages(dto);
            return ResponseEntity.ok(new ApiResponse<>(saved, true, "MESSAGE_SAVED_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException | BadApiRequestException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "MESSAGE_SAVED_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("get-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "messageId", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchKeyword",required = false) String searchKeyword,
            @RequestParam(value = "moduleId", required = false) Integer moduleId,
            @RequestParam(value = "functionId", required = false) Integer functionId
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.fromString(sortDir), sortBy);
            Map<String, Object> messages = messageService.getAllMessages(sortBy, sortDir, searchField, searchKeyword, pageable, moduleId, functionId);
            return ResponseEntity.ok(new ApiResponse<>(messages, true, "MESSAGE_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "MESSAGE_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("get-message-codes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMessageCodes() {
        try {
            List<Map<String, Object>> maps = messageService.messageCodeList();
            return ResponseEntity.ok(new ApiResponse<>(maps, true, "MESSAGE_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "MESSAGE_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("messages-by-functionId/{functionId}")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessagesByFunctionId(@PathVariable Integer functionId) {
        try {
            List<MessageDTO> messagesByFunctionId = messageService.getMessagesByFunctionId(functionId);
            return ResponseEntity.ok(new ApiResponse<>(messagesByFunctionId, true, "MESSAGE_FETCH_SUCCESS", "SUCCESS"));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse<>(null, false, "MESSAGE_FETCH_ERROR", "ERROR", Collections.singletonList(e.getMessage())), HttpStatus.OK);
        }
    }
}
