package com.atomicnorth.hrm.tenant.service.location;

import com.atomicnorth.hrm.tenant.domain.location.CityMaster;
import com.atomicnorth.hrm.tenant.domain.location.CountryMaster;
import com.atomicnorth.hrm.tenant.domain.location.StateMaster;
import com.atomicnorth.hrm.tenant.repository.location.CityMasterRepository;
import com.atomicnorth.hrm.tenant.repository.location.CountryMasterRepository;
import com.atomicnorth.hrm.tenant.repository.location.StateMasterRepository;
import com.atomicnorth.hrm.tenant.service.dto.location.CityMasterDTO;
import com.atomicnorth.hrm.tenant.service.dto.location.CountryMasterDTO;
import com.atomicnorth.hrm.tenant.service.dto.location.StateMasterDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class LocationService {
    @Autowired
    private CityMasterRepository cityMasterRepository;
    @Autowired
    private StateMasterRepository stateMasterRepository;
    @Autowired
    private CountryMasterRepository countryMasterRepository;
    @Autowired
    private ModelMapper modelMapper;
    public List<CountryMasterDTO> getAllCountries() {
        List<CountryMaster> countries = countryMasterRepository.findAll();
        return countries.stream()
                .map(country -> modelMapper.map(country, CountryMasterDTO.class))
                .collect(Collectors.toList());
    }
    public List<StateMasterDTO> getStatesByCountryId(Integer countryId) {
        List<StateMaster> states = stateMasterRepository.findByCountryId(countryId);
        return states.stream()
                .map(state -> modelMapper.map(state, StateMasterDTO.class))
                .sorted(Comparator.comparing(StateMasterDTO::getStateName))
                .collect(Collectors.toList());
    }
    public List<CityMasterDTO> getCitiesByStateId(Integer stateId) {
        List<CityMaster> cityEntities = cityMasterRepository.findByStateId(stateId);
        return cityEntities.stream()
                .map(city -> modelMapper.map(city, CityMasterDTO.class))
                .sorted(Comparator.comparing(CityMasterDTO::getCityName))
                .collect(Collectors.toList());
    }
    public List<CityMasterDTO> getCitiesByCountryId(Integer countryId) {
        List<StateMaster> states = stateMasterRepository.findByCountryId(countryId);

        List<Integer> stateIds = states.stream()
                .map(StateMaster::getStateId)
                .collect(Collectors.toList());

        List<CityMaster> cities = cityMasterRepository.findByStateIdIn(stateIds);

        return cities.stream()
                .map(city -> modelMapper.map(city, CityMasterDTO.class))
                .collect(Collectors.toList());
    }
}
