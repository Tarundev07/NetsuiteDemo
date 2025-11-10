package com.atomicnorth.hrm.tenant.service.language;

import com.atomicnorth.hrm.tenant.domain.language.SupraLanguageEntity;
import com.atomicnorth.hrm.tenant.repository.language.SupraLanguageRepo;
import com.atomicnorth.hrm.tenant.service.dto.language.SupraLanguageDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SupraLanguageServices {
    @Autowired
    private SupraLanguageRepo supraLanguageRepo;

    @Autowired
    private ModelMapper modelMapper;

    public List<SupraLanguageDTO> getSupraLanguageDTO() {
        List<SupraLanguageEntity> supraLanguages = supraLanguageRepo.findAll();

        return supraLanguages.stream()
                .map(supraLanguage -> modelMapper.map(supraLanguage, SupraLanguageDTO.class))
                .collect(Collectors.toList());
    }

    public Integer[] getSupraLanguageIds() {
        List<SupraLanguageEntity> supraLanguages = supraLanguageRepo.findAll();

        List<SupraLanguageDTO> supraLanguagesDto = supraLanguages.stream()
                .map(supraLanguage -> modelMapper.map(supraLanguage, SupraLanguageDTO.class))
                .collect(Collectors.toList());

        System.out.println(supraLanguagesDto);

        return supraLanguagesDto.stream()
                .map(SupraLanguageDTO::getLanguageId)
                .filter(Objects::nonNull)
                .toArray(Integer[]::new);
    }

}
