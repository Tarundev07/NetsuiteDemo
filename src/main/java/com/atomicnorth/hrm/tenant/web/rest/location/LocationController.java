package com.atomicnorth.hrm.tenant.web.rest.location;

import com.atomicnorth.hrm.tenant.service.dto.location.CityMasterDTO;
import com.atomicnorth.hrm.tenant.service.dto.location.CountryMasterDTO;
import com.atomicnorth.hrm.tenant.service.dto.location.StateMasterDTO;
import com.atomicnorth.hrm.tenant.service.location.LocationService;
import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
@RestController
@RequestMapping("/api/location")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @GetMapping("/getAllCountries")
    public ResponseEntity<ApiResponse<List<CountryMasterDTO>>> getAllCountries() {
        try {
            List<CountryMasterDTO> countryDTOs = locationService.getAllCountries();
            ApiResponse<List<CountryMasterDTO>> response = new ApiResponse<>(countryDTOs, true, "COUNTRY-LIST-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<CountryMasterDTO>> response = new ApiResponse<>(null, false, "COUNTRY-LIST-FETCH-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/getStatesByCountryId")
    public ResponseEntity<ApiResponse<List<StateMasterDTO>>> getStatesByCountryId(@RequestParam Integer countryId) {
        try {
            List<StateMasterDTO> stateDTOs = locationService.getStatesByCountryId(countryId);
            ApiResponse<List<StateMasterDTO>> response = new ApiResponse<>(stateDTOs, true, "STATE-LIST-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<StateMasterDTO>> response = new ApiResponse<>(null, false, "STATE-LIST-FETCH-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/getCitiesByStateId")
    public ResponseEntity<ApiResponse<List<CityMasterDTO>>> getCitiesByStateId(@RequestParam Integer stateId) {
        try {
            List<CityMasterDTO> cityDTOs = locationService.getCitiesByStateId(stateId);
            ApiResponse<List<CityMasterDTO>> response = new ApiResponse<>(cityDTOs, true, "CITY-LIST-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<CityMasterDTO>> response = new ApiResponse<>(null, false, "CITY-LIST-FETCH-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/getCitiesByCountryId")
    public ResponseEntity<ApiResponse<List<CityMasterDTO>>> getCitiesByCountryId(@RequestParam Integer countryId) {
        try {
            List<CityMasterDTO> cityDTOs = locationService.getCitiesByCountryId(countryId);
            ApiResponse<List<CityMasterDTO>> response = new ApiResponse<>(cityDTOs, true, "CITY-LIST-FETCH-SUCCESS", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<CityMasterDTO>> response = new ApiResponse<>(null, false, "CITY-LIST-FETCH-FAILURE", "Failure", Collections.singletonList(e.getMessage()));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
