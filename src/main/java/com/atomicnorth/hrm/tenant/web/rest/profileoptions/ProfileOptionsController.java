package com.atomicnorth.hrm.tenant.web.rest.profileoptions;

import com.atomicnorth.hrm.tenant.domain.roles.ApplicationModule;
import com.atomicnorth.hrm.tenant.domain.roles.FunctionEntity;
import com.atomicnorth.hrm.tenant.service.dto.roles.ApplicationModuleDTO;
import com.atomicnorth.hrm.tenant.service.profileoptions.ProfileOptionsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/options")
public class ProfileOptionsController {

    private final ObjectMapper objectMapper;
    @Autowired
    private ProfileOptionsService profileOptionsService;

    public ProfileOptionsController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/by-module/{moduleId}")
    public ResponseEntity<Object> getFunctionsByModuleId(@PathVariable Long moduleId) {
        List<FunctionEntity> functions = profileOptionsService.getFunctionsByModuleId(moduleId);

        if (functions.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No functions found for the given module ID.");
            response.put("status", HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(functions, HttpStatus.OK);
        }
    }

    @GetMapping
    public ResponseEntity<List<ApplicationModule>> getAllModules() {
        List<ApplicationModule> modules = profileOptionsService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/work-flow-dropdown")
    public ResponseEntity<List<ApplicationModuleDTO>> getModuleFunctionNested() {
        List<ApplicationModuleDTO> data = profileOptionsService.getModuleFunctionMap();
        return ResponseEntity.ok(data);
    }

}