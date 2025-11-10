package com.atomicnorth.hrm.tenant.service.company;


import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.company.Ses_M00_Set_Up_Company;
import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import com.atomicnorth.hrm.tenant.domain.lookup.LookupCode;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.company.SetUpCompanyRepository;
import com.atomicnorth.hrm.tenant.service.dto.company.SetUpCompanyDTOForRequest;
import com.atomicnorth.hrm.tenant.service.dto.company.SetUpCompanyDTOForResponse;
import com.atomicnorth.hrm.tenant.service.lookup.LookupTypeConfigurationService;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SetUpCompanyService {

    private final SetUpCompanyRepository repository;

    private final LookupCodeRepository lookupCodeRepository;

    private final ModelMapper modelMapper;

    private final LookupTypeConfigurationService lookupService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    public SetUpCompanyService(SetUpCompanyRepository repository, LookupCodeRepository lookupCodeRepository, ModelMapper modelMapper, LookupTypeConfigurationService lookupService) {
        this.repository = repository;
        this.lookupCodeRepository = lookupCodeRepository;
        this.modelMapper = modelMapper;
        this.lookupService = lookupService;
    }

    public SetUpCompanyDTOForResponse saveCompany(SetUpCompanyDTOForRequest request) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());
        Ses_M00_Set_Up_Company companyEntity;

        if (request.getParentCompany() != null && request.getParentCompany() == 0) {
            request.setParentCompany(null);
        }

        if (request.getCompanyId() != null) {
            companyEntity = repository.findById(request.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company with ID " + request.getCompanyId() + " not found."));

            Optional<Ses_M00_Set_Up_Company> existingCompany = repository.findByCompanyName(request.getCompanyName());
            if (existingCompany.isPresent() && !existingCompany.get().getCompanyId().equals(request.getCompanyId())) {
                throw new IllegalArgumentException("Another company with the name '" + request.getCompanyName() + "' already exists.");
            }
            if (request.getCompanyAbbreviation() != null) {
                Optional<Ses_M00_Set_Up_Company> existingAbbreviation =
                        repository.findByCompanyAbbreviation(request.getCompanyAbbreviation());

                if (existingAbbreviation.isPresent() &&
                        (request.getCompanyId() == null ||
                                !existingAbbreviation.get().getCompanyId().equals(request.getCompanyId()))) {
                    throw new IllegalArgumentException("A company with '" + request.getCompanyAbbreviation() + "'  abbreviation already exists.");
                }
            }

            modelMapper.map(request, companyEntity);
            companyEntity.setLastUpdatedDate(new Date());
        } else {
            if (repository.findByCompanyName(request.getCompanyName()).isPresent()) {
                throw new IllegalArgumentException("A company with the name '" + request.getCompanyName() + "' already exists.");
            }
            if (repository.findByCompanyAbbreviation(request.getCompanyAbbreviation()).isPresent()) {
                throw new IllegalArgumentException("A company with '" + request.getCompanyAbbreviation() + "'  abbreviation already exists.");
            }
            companyEntity = modelMapper.map(request, Ses_M00_Set_Up_Company.class);
            companyEntity.setCreatedOn(new Date());
            companyEntity.setCreatedBy(username);
        }
        Ses_M00_Set_Up_Company savedEntity = repository.save(companyEntity);
        if (savedEntity.getPayrollCycle() != null) {
            List<Employee> employees = employeeRepository.findAll();
            for (Employee emp : employees) {
                // only update if employee’s payroll cycle is null or zero (allow override)
                if (emp.getPayrollCycle() == null || emp.getPayrollCycle() == 0) {
                    emp.setPayrollCycle(savedEntity.getPayrollCycle());
                }
            }
            employeeRepository.saveAll(employees);
        }
        return modelMapper.map(savedEntity, SetUpCompanyDTOForResponse.class);
    }

    public Map<String, Object> getAllCompanies(Pageable pageable, String searchColumn, String searchValue, String sortColumn, String sortDirection) {
        Page<Ses_M00_Set_Up_Company> pageData = getFilteredCompanies(PageRequest.of(0, Integer.MAX_VALUE), searchColumn, searchValue); // fetch all

        Set<Integer> allLookupIds = new HashSet<>();
        List<Ses_M00_Set_Up_Company> entities = pageData.getContent();

        for (Ses_M00_Set_Up_Company entity : entities) {
            Collections.addAll(allLookupIds,
                    entity.getDefaultCurrency(),
                    entity.getCountry(),
                    entity.getDefaultLetterHead(),
                    entity.getTaxId(),
                    entity.getChartOfAccount(),
                    entity.getChartOfAccountTemplate(),
                    entity.getWriteOffAccount(),
                    entity.getLostAccount(),
                    entity.getDefaultPaymentDiscountAccount(),
                    entity.getPaymentTermTemplate(),
                    entity.getExchangeGainLoss(),
                    entity.getUnreleasedGainLoss(),
                    entity.getRoundOffAccount(),
                    entity.getRoundOffOpening(),
                    entity.getRoundOffCostCenter()
            );
        }

        Map<Integer, String> lookupMap = lookupCodeRepository.findAllById(allLookupIds).stream()
                .collect(Collectors.toMap(LookupCode::getLookupCodeId, LookupCode::getMeaning));

        List<SetUpCompanyDTOForResponse> dtoList = entities.stream()
                .map(entity -> convertToDTOWithLookup(entity, lookupMap))
                .collect(Collectors.toList());

        Comparator<SetUpCompanyDTOForResponse> comparator = getEnrichedFieldComparator(sortColumn);
        if (comparator != null) {
            dtoList.sort("DESC".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator);
        } else {
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), dtoList.size());
            dtoList = dtoList.subList(start, end);
        }

        return Map.of(
                "result", dtoList,
                "currentPage", pageable.getPageNumber() + 1,
                "pageSize", pageable.getPageSize(),
                "totalItems", pageData.getTotalElements(),
                "totalPages", (int) Math.ceil((double) pageData.getTotalElements() / pageable.getPageSize())
        );
    }

    private Page<Ses_M00_Set_Up_Company> getFilteredCompanies(Pageable pageable, String column, String value) {
        if ("holidayName".equalsIgnoreCase(column)) {
            return repository.findByHolidaysCalendar_NameContainingIgnoreCase(value, pageable);
        } else if ("parentCompanyName".equalsIgnoreCase(column)) {
            return repository.findByParentCompanyEntity_CompanyNameContainingIgnoreCase(value, pageable);
        } else if (column != null && value != null) {
            return repository.findAll(searchByColumn(column, value), pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    private Comparator<SetUpCompanyDTOForResponse> getEnrichedFieldComparator(String sortColumn) {
        if (sortColumn == null) return null;

        switch (sortColumn.toLowerCase()) {
            case "countryname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getCountryName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyid":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getCompanyId,
                        Comparator.nullsLast(Integer::compare));
            case "companyname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getCompanyName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyabbreviation":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getCompanyAbbreviation,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "letterheadname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getLetterHeadName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "currency":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getCurrency,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "tax":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getTax,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "chartofaccountname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getChartOfAccountName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "chartofaccounttemplatename":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getChartOfAccountTemplateName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "writeoffaccountname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getWriteOffAccountName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "lostaccountname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getLostAccountName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "defaultpaymentdiscountaccountname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getDefaultPaymentDiscountAccountName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "paymenttermtemplatename":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getPaymentTermTemplateName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "exchangegainlossname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getExchangeGainLossName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "unreleasedgainlossname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getUnreleasedGainLossName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "roundoffaccountname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getRoundOffAccountName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "roundoffopeningname":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getRoundOffOpeningName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            case "roundoffcostcentername":
                return Comparator.comparing(SetUpCompanyDTOForResponse::getRoundOffCostCenterName,
                        Comparator.nullsLast(String::compareToIgnoreCase));
            default:
                return null;
        }
    }

    public Specification<Ses_M00_Set_Up_Company> searchByColumn(String column, String value) {
        return (root, query, cb) -> {
            String field = column.toLowerCase();
            switch (field) {
                case "countryname":
                    return lookupCodeMatchPredicate(cb, root.get("country"), value, "COUNTRY_LIST");
                case "letterheadname":
                    return lookupCodeMatchPredicate(cb, root.get("defaultLetterHead"), value, "LETTER_HEAD");
                case "currency":
                    return lookupCodeMatchPredicate(cb, root.get("defaultCurrency"), value, "CURRENCY_LIST");
                case "tax":
                    return lookupCodeMatchPredicate(cb, root.get("taxId"), value, "TAX_ID");
                case "chartofaccountname":
                    return lookupCodeMatchPredicate(cb, root.get("chartOfAccount"), value, "ACCOUNT_LIST");
                case "chartofaccounttemplatename":
                    return lookupCodeMatchPredicate(cb, root.get("chartOfAccountTemplate"), value, "ACCOUNT_TEMPLATE");
                case "writeoffaccountname":
                    return lookupCodeMatchPredicate(cb, root.get("writeOffAccount"), value, "WRITE_OFF_ACCOUNT");
                case "lostaccountname":
                    return lookupCodeMatchPredicate(cb, root.get("lostAccount"), value, "LOST_ACCOUNT");
                case "defaultpaymentdiscountaccountname":
                    return lookupCodeMatchPredicate(cb, root.get("defaultPaymentDiscountAccount"), value, "PAYMENT_DISCOUNT_ACCOUNT");
                case "paymenttermtemplatename":
                    return lookupCodeMatchPredicate(cb, root.get("paymentTermTemplate"), value, "PAYMENT_TERM_TEMPLATE");
                case "exchangegainlossname":
                    return lookupCodeMatchPredicate(cb, root.get("exchangeGainLoss"), value, "EXCHANGE_GAIN_LOSS");
                case "unreleasedgainlossname":
                    return lookupCodeMatchPredicate(cb, root.get("unreleasedGainLoss"), value, "UNRELEASED_GAIN_LOSS");
                case "roundoffaccountname":
                    return lookupCodeMatchPredicate(cb, root.get("roundOffAccount"), value, "ROUND_OFF_ACCOUNT");
                case "roundoffopeningname":
                    return lookupCodeMatchPredicate(cb, root.get("roundOffOpening"), value, "ROUND_OFF_OPENING");
                case "roundoffcostcentername":
                    return lookupCodeMatchPredicate(cb, root.get("roundOffCostCenter"), value, "ROUND_OFF_COST_CENTER");
                default:
                    return cb.like(cb.lower(root.get(column)), "%" + value.toLowerCase() + "%");
            }
        };
    }

    private Predicate lookupCodeMatchPredicate(CriteriaBuilder cb, Path<Integer> path, String value, String type) {
        List<Integer> ids = lookupService.getIdByMeaningAndType(value, type);
        return ids.isEmpty() ? cb.disjunction() : path.in(ids);
    }

    private SetUpCompanyDTOForResponse convertToDTOWithLookup(Ses_M00_Set_Up_Company entity, Map<Integer, String> lookupMap) {
        SetUpCompanyDTOForResponse dto = modelMapper.map(entity, SetUpCompanyDTOForResponse.class);

        // Set ID fields
        dto.setDefaultCurrencyId(entity.getDefaultCurrency());
        dto.setCountryId(entity.getCountry());
        dto.setDefaultLetterHeadId(entity.getDefaultLetterHead());
        dto.setChartOfAccountId(entity.getChartOfAccount());
        dto.setChartOfAccountTemplateId(entity.getChartOfAccountTemplate());
        dto.setWriteOffAccountId(entity.getWriteOffAccount());
        dto.setLostAccountId(entity.getLostAccount());
        dto.setDefaultPaymentDiscountAccountId(entity.getDefaultPaymentDiscountAccount());
        dto.setPaymentTermTemplateId(entity.getPaymentTermTemplate());
        dto.setExchangeGainLossId(entity.getExchangeGainLoss());
        dto.setUnreleasedGainLossId(entity.getUnreleasedGainLoss());
        dto.setRoundOffAccountId(entity.getRoundOffAccount());
        dto.setRoundOffOpeningId(entity.getRoundOffOpening());
        dto.setRoundOffCostCenterId(entity.getRoundOffCostCenter());
        dto.setTaxId(entity.getTaxId());
        dto.setPayrollCycle(entity.getPayrollCycle());

        // Enriched values using lookupMap
        dto.setCurrency(lookupMap.getOrDefault(entity.getDefaultCurrency(), "Unknown"));
        dto.setCountryName(lookupMap.getOrDefault(entity.getCountry(), "Unknown"));
        dto.setLetterHeadName(lookupMap.getOrDefault(entity.getDefaultLetterHead(), "Unknown"));
        dto.setTax(lookupMap.getOrDefault(entity.getTaxId(), "Unknown"));
        dto.setChartOfAccountName(lookupMap.getOrDefault(entity.getChartOfAccount(), "Unknown"));
        dto.setChartOfAccountTemplateName(lookupMap.getOrDefault(entity.getChartOfAccountTemplate(), "Unknown"));
        dto.setWriteOffAccountName(lookupMap.getOrDefault(entity.getWriteOffAccount(), "Unknown"));
        dto.setLostAccountName(lookupMap.getOrDefault(entity.getLostAccount(), "Unknown"));
        dto.setDefaultPaymentDiscountAccountName(lookupMap.getOrDefault(entity.getDefaultPaymentDiscountAccount(), "Unknown"));
        dto.setPaymentTermTemplateName(lookupMap.getOrDefault(entity.getPaymentTermTemplate(), "Unknown"));
        dto.setExchangeGainLossName(lookupMap.getOrDefault(entity.getExchangeGainLoss(), "Unknown"));
        dto.setUnreleasedGainLossName(lookupMap.getOrDefault(entity.getUnreleasedGainLoss(), "Unknown"));
        dto.setRoundOffAccountName(lookupMap.getOrDefault(entity.getRoundOffAccount(), "Unknown"));
        dto.setRoundOffOpeningName(lookupMap.getOrDefault(entity.getRoundOffOpening(), "Unknown"));
        dto.setRoundOffCostCenterName(lookupMap.getOrDefault(entity.getRoundOffCostCenter(), "Unknown"));

        // Additional fields
        dto.setHolidayName(Optional.ofNullable(entity.getHolidaysCalendar()).map(HolidaysCalendar::getName).orElse("Unknown"));
        dto.setParentCompanyName(Optional.ofNullable(entity.getParentCompanyEntity()).map(Ses_M00_Set_Up_Company::getCompanyName).orElse("Unknown"));

        return dto;
    }

    public SetUpCompanyDTOForResponse getCompaniesById(int id) {
        return repository.findById(id)
                .map(entity -> {
                    SetUpCompanyDTOForResponse dto = modelMapper.map(entity, SetUpCompanyDTOForResponse.class);
                    dto.setCountryId(entity.getCountry());
                    dto.setDefaultCurrencyId(entity.getDefaultCurrency());
                    dto.setParentCompanyId(entity.getParentCompany());
                    dto.setDefaultLetterHeadId(entity.getDefaultLetterHead());
                    dto.setChartOfAccountId(entity.getChartOfAccount());
                    dto.setChartOfAccountTemplateId(entity.getChartOfAccountTemplate());
                    dto.setWriteOffAccountId(entity.getWriteOffAccount());
                    dto.setLostAccountId(entity.getLostAccount());
                    dto.setDefaultPaymentDiscountAccountId(entity.getDefaultPaymentDiscountAccount());
                    dto.setPaymentTermTemplateId(entity.getPaymentTermTemplate());
                    dto.setExchangeGainLossId(entity.getExchangeGainLoss());
                    dto.setUnreleasedGainLossId(entity.getUnreleasedGainLoss());
                    dto.setRoundOffAccountId(entity.getRoundOffAccount());
                    dto.setRoundOffOpeningId(entity.getRoundOffOpening());
                    dto.setRoundOffCostCenterId(entity.getRoundOffCostCenter());
                    dto.setTaxId(entity.getTaxId());
                    dto.setPayrollCycle(entity.getPayrollCycle());
                    return dto;
                })
                .orElse(null);
    }

    public List<Map<String, Object>> companyDropdownList() {
        return repository.findAll().stream().map(company -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("companyId", company.getCompanyId());
            result.put("companyName", company.getCompanyName());
            return result;
        }).collect(Collectors.toList());
    }
}
