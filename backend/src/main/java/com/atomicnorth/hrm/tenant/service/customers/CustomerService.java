package com.atomicnorth.hrm.tenant.service.customers;

import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.customers.Customer;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerAccount;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerSite;
import com.atomicnorth.hrm.tenant.domain.location.CityMaster;
import com.atomicnorth.hrm.tenant.domain.location.CountryMaster;
import com.atomicnorth.hrm.tenant.domain.location.StateMaster;
import com.atomicnorth.hrm.tenant.domain.project.Project;
import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import com.atomicnorth.hrm.tenant.repository.customers.CustomerAccountRepository;
import com.atomicnorth.hrm.tenant.repository.customers.CustomerRepository;
import com.atomicnorth.hrm.tenant.repository.customers.CustomerSiteRepository;
import com.atomicnorth.hrm.tenant.repository.employement.employee_job_applicant.JobApplicantRepository;
import com.atomicnorth.hrm.tenant.repository.project.ProjectRepository;
import com.atomicnorth.hrm.tenant.service.SequenceGeneratorService;
import com.atomicnorth.hrm.tenant.service.dto.customers.*;
import com.atomicnorth.hrm.util.Enum.SequenceType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private CustomerSiteRepository customerSiteRepo;
    @Autowired
    private CustomerAccountRepository customerAccountRepository;
    @Autowired
    private JobApplicantRepository employeeJobApplicantRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private LookupCodeRepository lookupCodeRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;
    @Autowired
    private ProjectRepository projectRepository;


    public Map<String, Object> getPaginatedCustomerSites(Pageable pageable, String searchColumn, String searchValue) {
        Specification<CustomerSite> spec = (root, query, criteriaBuilder) -> {
            if (searchColumn != null && searchValue != null && !searchValue.isEmpty()) {
                // Handle search for countryName (fetching corresponding countryId)
                if (searchColumn.equals("countryName")) {
                    List<Integer> countryIds = lookupCodeRepository.findLookupIdsByName(searchValue);
                    if (countryIds.isEmpty()) return criteriaBuilder.disjunction();
                    return root.get("countryId").in(countryIds);
                }

                // Default: Handle numeric or string search dynamically
                try {
                    Integer numericValue = Integer.parseInt(searchValue);
                    return criteriaBuilder.equal(root.get(searchColumn), numericValue);
                } catch (NumberFormatException e) {
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get(searchColumn).as(String.class)), "%" + searchValue.toLowerCase() + "%");
                }
            }
            return criteriaBuilder.conjunction();
        };

        Page<CustomerSite> customerSitePage = customerSiteRepo.findAll(spec, pageable);

        // Fetch related data in batch
        Set<Integer> countryIds = new HashSet<>();

        customerSitePage.forEach(site -> {
            if (site.getCountryId() != null) countryIds.add(site.getCountryId());
        });

        Map<Integer, String> countryMap = fetchLookupNames(countryIds);

        // Map to DTOs
        List<CustomerEntityCustomerSiteDTO> siteDTOList = customerSitePage.getContent().stream().map(site -> {
            CustomerEntityCustomerSiteDTO dto = modelMapper.map(site, CustomerEntityCustomerSiteDTO.class);
            dto.setCountryName((countryMap.get(site.getCountryId()))); // Map country name
            return dto;
        }).collect(Collectors.toList());

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("result", siteDTOList);
        response.put("currentPage", customerSitePage.getNumber() + 1);
        response.put("totalItems", customerSitePage.getTotalElements());
        response.put("totalPages", customerSitePage.getTotalPages());

        return response;
    }

    // Fetch Lookup Names (e.g., Country Names)
    private Map<Integer, String> fetchLookupNames(Set<Integer> lookupIds) {
        if (lookupIds.isEmpty()) return Collections.emptyMap();

        List<Object[]> results = lookupCodeRepository.findLookupNamesByIds(lookupIds);
        return results.stream().collect(Collectors.toMap(row -> (Integer) row[0], // Country ID
                row -> (String) row[1]   // Country Name
        ));
    }

    public CustomerDTO saveOrUpdate(CustomerDTO dto) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String name = String.valueOf(user.getUsername());
        Customer customer;
        if (dto.getCustomerId() != null) {
            // Update existing
            customer = customerRepository.findById(dto.getCustomerId()).orElseThrow(() -> new EntityNotFoundException("Customer not found with ID " + dto.getCustomerId()));
        } else {
            // Create new
            customer = new Customer();
            customer.setCreatedDate(Instant.now());
            customer.setCreatedBy(name);
        }
        modelMapper.map(dto, customer);
        if (dto.getEffectiveEndDate() != null) {
            customer.setEffectiveEndDate(dto.getEffectiveEndDate());
        } else {
            customer.setEffectiveEndDate(null);
        }
        customer.setLastUpdatedDate(Instant.now());
        customer.setLastUpdatedBy(name);
        Customer savedCustomer = customerRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerDTO.class);
    }

    public CustomerDTO getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId).orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        CustomerDTO dto = modelMapper.map(customer, CustomerDTO.class);
        dto.setCountryName(customer.getCountry() != null ? customer.getCountry().getCountryName() : "Unknown");
        dto.setCityName(customer.getCity() != null ? customer.getCity().getCityName() : "Unknown");
        dto.setSalesRepresentativeName(customer.getSalesRepresentative() != null ? customer.getSalesRepresentative().getFullName() : "Unknown");
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CustomerNamesDtos> getAllCustomersNameAndId() {
        return customerRepository.findAllBy();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllCustomers(Pageable pageable, String searchField, String searchKeyword) {
        Page<Customer> customers;

        if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<Customer> spec = buildCustomerSpecification(searchField, searchKeyword);
            customers = customerRepository.findAll(spec, pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        List<CustomerDTO> customerDTOList = customers.getContent().stream().map(this::mapToCustomerDTO).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", customerDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", customers.getTotalElements());
        response.put("totalPages", customers.getTotalPages());

        return response;
    }

    private Specification<Customer> buildCustomerSpecification(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = null;
            String lowerSearchKeyword = "%" + searchKeyword.toLowerCase() + "%";
            if ("countryName".equalsIgnoreCase(searchField)) {
                Join<Customer, CountryMaster> countryJoin = root.join("country", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(countryJoin.get("countryName")), lowerSearchKeyword);
            } else if ("cityName".equalsIgnoreCase(searchField)) {
                Join<Customer, CityMaster> cityJoin = root.join("city", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("cityName")), lowerSearchKeyword);
            } else if ("stateName".equalsIgnoreCase(searchField)) {
                Join<Customer, StateMaster> stateJoin = root.join("state", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(stateJoin.get("stateName")), lowerSearchKeyword);
            } else if ("salesRepresentativeName".equalsIgnoreCase(searchField)) {
                Join<Customer, Employee> employeeJoin = root.join("salesRepresentative", JoinType.LEFT);
                Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("firstName")), lowerSearchKeyword);
                Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(employeeJoin.get("lastName")), lowerSearchKeyword);
                predicate = criteriaBuilder.or(firstNamePredicate, lastNamePredicate);
            } else if ("effectiveStartDate".equalsIgnoreCase(searchField) || "effectiveEndDate".equalsIgnoreCase(searchField)) {
                Expression<String> formattedDate = criteriaBuilder.function("DATE_FORMAT", String.class, root.get(searchField), criteriaBuilder.literal("%Y-%m-%d"));
                predicate = criteriaBuilder.like(criteriaBuilder.lower(formattedDate), lowerSearchKeyword);
            } else {
                try {
                    predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField).as(String.class)), lowerSearchKeyword);
                } catch (IllegalArgumentException e) {
                    System.err.println("Search field '" + searchField + "' not found or not suitable for LIKE operation. Error: " + e.getMessage());
                    predicate = criteriaBuilder.conjunction();
                }
            }
            return predicate;
        };
    }
    private CustomerDTO mapToCustomerDTO(Customer customer) {
        CustomerDTO dto = modelMapper.map(customer, CustomerDTO.class);
        dto.setPrimaryCurrency(lookupCodeRepository.findByLookupCodes(dto.getPrimaryCurrency()));
        dto.setCountryName(customer.getCountry() != null ? customer.getCountry().getCountryName() : "Unknown");
        dto.setStateName(customer.getState() != null ? customer.getState().getStateName() : "Unknown");
        dto.setCityName(customer.getCity() != null ? customer.getCity().getCityName() : "Unknown");
        dto.setSalesRepresentativeName(customer.getSalesRepresentative() != null ? customer.getSalesRepresentative().getFullName() : "Unknown");

        return dto;
    }

    public CustomerSite findCustomerSiteBySiteId(Integer siteId) {
        return customerSiteRepo.findById(siteId).orElseThrow(() -> new EntityNotFoundException("Customer site not found with ID " + siteId));
    }

    //create customer account and site
    public CustomerAccountDTO saveOrUpdateCustomerAccount(CustomerAccountDTO dto) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String name = String.valueOf(user.getUsername());
        if (dto.getAccountName() == null || dto.getAccountName().trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty");
        }
        // Validate duplicate account name
        validateDuplicateAccountName(dto.getAccountName(), dto.getAccountId());
        CustomerAccount customer;
        if (dto.getAccountId() != null) {
            // Update existing
            customer = customerAccountRepository.findById(dto.getAccountId()).orElseThrow(() -> new EntityNotFoundException("Customer Account not found with ID " + dto.getAccountId()));
        } else {
            // Create new
            customer = new CustomerAccount();
            customer.setCreatedDate(Instant.now());
            customer.setCreatedBy(name);
        }
        modelMapper.map(dto, customer);
        if (dto.getAccountCode() == null || dto.getAccountCode().isEmpty()) {
            customer.setAccountCode(sequenceGeneratorService.generateSequence(SequenceType.CUSTOMER_ACCOUNT.toString(), null));
        }
        customer.setLastUpdatedDate(Instant.now());
        customer.setLastUpdatedBy(name);
        CustomerAccount savedCustomer = customerAccountRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerAccountDTO.class);
    }

    private void validateDuplicateAccountName(String accountName, Integer currentId) {
        String normalizedInput = accountName.trim().replaceAll("\\s+", " ").toLowerCase();

        List<CustomerAccount> accounts = customerAccountRepository.findAll();

        boolean exists = accounts.stream()
                .filter(acc -> currentId == null || !acc.getAccountId().equals(currentId)) // Skip current on update
                .map(acc -> acc.getAccountName().trim().replaceAll("\\s+", " ").toLowerCase())
                .anyMatch(normalized -> normalized.equals(normalizedInput));
        if (exists) {
            throw new IllegalArgumentException("Duplicate account name");
        }
    }

    public CustomerAccountDTO getCustomerByAccountId(Integer accountId) {
        CustomerAccount customer = customerAccountRepository.findByAccountId(accountId).orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + accountId));
        return modelMapper.map(customer, CustomerAccountDTO.class);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllCustomerAccount(Pageable pageable, String searchField, String searchKeyword) {
        Page<CustomerAccount> customers;

        if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<CustomerAccount> spec = buildCustomerAccountSpecification(searchField, searchKeyword);
            customers = customerAccountRepository.findAll(spec, pageable);
        } else {
            customers = customerAccountRepository.findAll(pageable);
        }

        List<CustomerAccountDTO> customerDTOList = customers.getContent().stream().map(this::mapToCustomerAccountDTOs).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", customerDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", customers.getTotalElements());
        response.put("totalPages", customers.getTotalPages());

        return response;
    }

    private Specification<CustomerAccount> buildCustomerAccountSpecification(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = null;
            String lowerSearchKeyword = "%" + searchKeyword.toLowerCase() + "%";

            if ("countryName".equalsIgnoreCase(searchField)) {
                Join<CustomerAccount, CountryMaster> countryJoin = root.join("country", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(countryJoin.get("countryName")), lowerSearchKeyword);
            } else if ("cityName".equalsIgnoreCase(searchField)) {
                Join<CustomerAccount, CityMaster> cityJoin = root.join("city", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("cityName")), lowerSearchKeyword);
            } else if ("stateName".equalsIgnoreCase(searchField)) {
                Join<CustomerAccount, StateMaster> cityJoin = root.join("state", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("stateName")), lowerSearchKeyword);
            } else if ("customerName".equalsIgnoreCase(searchField)) {
                Join<CustomerAccount, Customer> customerJoin = root.join("customer", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("customerName")), lowerSearchKeyword);
            }  else if ("startDate".equalsIgnoreCase(searchField) || "endDate".equalsIgnoreCase(searchField)) {
                Expression<String> formattedDate = criteriaBuilder.function("DATE_FORMAT", String.class, root.get(searchField), criteriaBuilder.literal("%Y-%m-%d"));
                predicate = criteriaBuilder.like(criteriaBuilder.lower(formattedDate), lowerSearchKeyword);
            }else {
                try {
                    predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), lowerSearchKeyword);
                } catch (IllegalArgumentException e) {
                    System.err.println("Search field '" + searchField + "' not found or not a string for LIKE operation. Error: " + e.getMessage());
                    predicate = criteriaBuilder.conjunction();
                }
            }
            return predicate;
        };
    }
    private CustomerAccountDTO mapToCustomerAccountDTOs(CustomerAccount customer) {
        CustomerAccountDTO dto = modelMapper.map(customer, CustomerAccountDTO.class);
        dto.setPaymentType(lookupCodeRepository.findByLookupCodeId(Integer.valueOf(dto.getPaymentType())).orElse("Unknown Activity"));
        dto.setCountryName(customer.getCountry() != null ? customer.getCountry().getCountryName() : "Unknown");
        dto.setCustomerName(customer.getCustomer() != null ? customer.getCustomer().getCustomerName() : "Unknown");
        dto.setStateName(customer.getState() != null ? customer.getState().getStateName() : "Unknown");
        dto.setCityName(customer.getCity() != null ? customer.getCity().getCityName() : "Unknown");
        return dto;
    }

    public List<CustomerSiteDTO> saveOrUpdateCustomerSites(List<CustomerSiteDTO> dtos) {
        UserLoginDetail user = SessionHolder.getUserLoginDetail();
        String username = String.valueOf(user.getUsername());
        List<CustomerSiteDTO> responseList = new ArrayList<>();
        for (CustomerSiteDTO dto : dtos) {
            if (dto.getSiteName() == null || dto.getSiteName().trim().isEmpty()) {
                throw new IllegalArgumentException("Site name cannot be empty");
            }
            // Optional: Add duplicate check if needed
            validateDuplicateSiteName(dto.getSiteName(), dto.getSiteId());
            CustomerSite site;
            if (dto.getSiteId() != null) {
                // Update
                site = customerSiteRepo.findById(dto.getSiteId())
                        .orElseThrow(() -> new EntityNotFoundException("Customer Site not found with ID " + dto.getSiteId()));
            } else {
                // New
                site = new CustomerSite();
                site.setCreatedDate(Instant.now());
                site.setCreatedBy(username);
            }
            modelMapper.map(dto, site);
            if (dto.getSiteCode() == null || dto.getSiteCode().isEmpty()) {
                site.setSiteCode(sequenceGeneratorService.generateSequence(SequenceType.CUSTOMER_SITE.toString(), null));
            }
            site.setLastUpdatedDate(Instant.now());
            site.setLastUpdatedBy(username);
            CustomerSite saved = customerSiteRepo.save(site);
            responseList.add(modelMapper.map(saved, CustomerSiteDTO.class));
        }
        return responseList;
    }

    private void validateDuplicateSiteName(String siteName, Integer currentId) {
        String normalizedInput = siteName.trim().replaceAll("\\s+", " ").toLowerCase();

        List<CustomerSite> accounts = customerSiteRepo.findAll();

        boolean exists = accounts.stream()
                .filter(acc -> currentId == null || !acc.getSiteId().equals(currentId)) // Skip current on update
                .map(acc -> acc.getSiteName().trim().replaceAll("\\s+", " ").toLowerCase())
                .anyMatch(normalized -> normalized.equals(normalizedInput));
        if (exists) {
            throw new IllegalArgumentException("Duplicate site name");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllCustomerSites(Pageable pageable, String searchField, String searchKeyword) {
        Page<CustomerSite> sites;

        if (searchField != null && searchKeyword != null && !searchKeyword.trim().isEmpty() && !searchField.trim().isEmpty()) {
            Specification<CustomerSite> spec = buildCustomerSitespecification(searchField, searchKeyword);
            sites = customerSiteRepo.findAll(spec, pageable);
        } else {
            sites = customerSiteRepo.findAll(pageable);
        }

        List<CustomerSiteDTO> customerDTOList = sites.getContent().stream().map(this::mapToCustomerSitesDTOs).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", customerDTOList);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", sites.getTotalElements());
        response.put("totalPages", sites.getTotalPages());

        return response;
    }

    private Specification<CustomerSite> buildCustomerSitespecification(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = null;
            String lowerSearchKeyword = "%" + searchKeyword.toLowerCase() + "%";

            if ("countryName".equalsIgnoreCase(searchField)) {
                Join<CustomerSite, CountryMaster> countryJoin = root.join("country", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(countryJoin.get("countryName")), lowerSearchKeyword);
            } else if ("cityName".equalsIgnoreCase(searchField)) {
                Join<CustomerSite, CityMaster> cityJoin = root.join("city", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("cityName")), lowerSearchKeyword);
            } else if ("stateName".equalsIgnoreCase(searchField)) {
                Join<CustomerSite, StateMaster> cityJoin = root.join("state", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("stateName")), lowerSearchKeyword);
            } else if ("accountName".equalsIgnoreCase(searchField)) {
                Join<CustomerSite, CustomerAccount> customerJoin = root.join("customerAccount", JoinType.LEFT);
                predicate = criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("accountName")), lowerSearchKeyword);
            }  else if ("startDate".equalsIgnoreCase(searchField) || "endDate".equalsIgnoreCase(searchField)) {
                Expression<String> formattedDate = criteriaBuilder.function("DATE_FORMAT", String.class, root.get(searchField), criteriaBuilder.literal("%Y-%m-%d"));
                predicate = criteriaBuilder.like(criteriaBuilder.lower(formattedDate), lowerSearchKeyword);
            }else {
                try {
                    predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), lowerSearchKeyword);
                } catch (IllegalArgumentException e) {
                    System.err.println("Search field '" + searchField + "' not found or not a string for LIKE operation. Error: " + e.getMessage());
                    predicate = criteriaBuilder.conjunction();
                }
            }
            return predicate;
        };
    }

    private CustomerSiteDTO mapToCustomerSitesDTOs(CustomerSite customer) {
        CustomerSiteDTO dto = modelMapper.map(customer, CustomerSiteDTO.class);
        dto.setCurrency(lookupCodeRepository.findByLookupCodeId(Integer.valueOf(dto.getCurrency())).orElse("Unknown Activity"));
        dto.setCountryName(customer.getCountry() != null ? customer.getCountry().getCountryName() : "Unknown");
        dto.setAccountName(customer.getCustomerAccount() != null ? customer.getCustomerAccount().getAccountName() : "Unknown");
        dto.setStateName(customer.getState() != null ? customer.getState().getStateName() : "Unknown");
        dto.setCityName(customer.getCity() != null ? customer.getCity().getCityName() : "Unknown");
        return dto;
    }

    public CustomerSiteDTO getCustomerBySiteId(Integer siteId) {
        CustomerSite customerSite = customerSiteRepo.findBySiteId(siteId).orElseThrow(() -> new EntityNotFoundException("Customer site not found with id: " + siteId));
        return modelMapper.map(customerSite, CustomerSiteDTO.class);
    }
    public List<CustomerSiteDTO> getCustomersAccountId(Integer accountId) {
        List<CustomerSite> customerSites = customerSiteRepo.findCustomersByAccountId(accountId);
        if (customerSites.isEmpty()) {
            throw new EntityNotFoundException("Customer not found with id: " + accountId);
        }
        return customerSites.stream().map(customer -> {
            CustomerSiteDTO dto = modelMapper.map(customer, CustomerSiteDTO.class);
            dto.setCountryName(customer.getCountry() != null ? customer.getCountry().getCountryName() : "Unknown");
            dto.setStateName(customer.getState() != null ? customer.getState().getStateName() : "Unknown");
            dto.setCityName(customer.getCity() != null ? customer.getCity().getCityName() : "Unknown");
            dto.setAccountName(customer.getCustomerAccount() != null ? customer.getCustomerAccount().getAccountName() : "Unknown");
            return dto;
        }).collect(Collectors.toList());
    }
    public Map<String, Object> findCustomerDataByProjectId(Integer projectId) {
        Integer siteId = projectRepository.findById(projectId).map(Project::getSiteId)
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found."));
        CustomerSite site = customerSiteRepo.findById(siteId).orElseThrow(() -> new EntityNotFoundException("Customer Site not found for id " + siteId));
        CustomerAccount customerAccount = customerAccountRepository.findById(site.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Customer Account not found for id " + site.getAccountId()));
        Customer customer = customerRepository.findById(customerAccount.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found for id " + customerAccount.getCustomerId()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("siteCode", site.getSiteCode());
        result.put("customerName", customer.getCustomerName());
        result.put("customerDescription", customer.getCustomerDescription());
        return result;
    }

    public List<Map<String, Object>> getCustomerDropdownList() {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> "Y".equals(customer.getIsActive()))
                .map(customer -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("customerId", customer.getCustomerId());
                    result.put("customerName", customer.getCustomerName());
                    return result;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAccountsByCustomerId(Integer customerId, Pageable pageable, String searchColumn, String searchValue) {
        Specification<CustomerAccount> spec = (root, query, cb) -> cb.equal(root.get("customer").get("customerId"), customerId);
        if (searchColumn != null && searchValue != null && !searchValue.trim().isEmpty() && !searchColumn.trim().isEmpty()) {
            spec = spec.and(buildCustomerAccountSpecifications(searchColumn, searchValue));
        }
        Page<CustomerAccount> pageResult = customerAccountRepository.findAll(spec, pageable);
        if (pageResult.isEmpty() && pageable.getPageNumber() == 0) {
            throw new EntityNotFoundException("Customer accounts not found for customer id: " + customerId);
        }
        List<CustomerAccountDTO> customerAccountDTOs = pageResult.getContent().stream().map(this::mapToCustomerAccountDTO).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", customerAccountDTOs);
        response.put("currentPage", pageable.getPageNumber() + 1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }

    private Specification<CustomerAccount> buildCustomerAccountSpecifications(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate;
            String keyword = "%" + searchKeyword.toLowerCase() + "%";
            switch (searchField.toLowerCase()) {
                case "countryname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("country", JoinType.LEFT).get("countryName")), keyword);
                case "statename":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("state", JoinType.LEFT).get("stateName")), keyword);
                case "cityname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("city", JoinType.LEFT).get("cityName")), keyword);
                case "customername":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("customer", JoinType.LEFT).get("customerName")), keyword);
                case "accountname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("accountName")), keyword);
                case "accountnumber":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber")), keyword);
                case "startdate":
                case "enddate":
                    Expression<String> formattedDate = criteriaBuilder.function("DATE_FORMAT", String.class, root.get(searchField),
                            criteriaBuilder.literal("%Y-%m-%d"));
                    return criteriaBuilder.like(criteriaBuilder.lower(formattedDate), keyword);
                default:
                    try {
                        return criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), keyword);
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Invalid search field for CustomerAccount: " + searchField + " - " + ex.getMessage());
                        return criteriaBuilder.conjunction();
                    }
            }
        };
    }
    private CustomerAccountDTO mapToCustomerAccountDTO(CustomerAccount customerAccount) {
        CustomerAccountDTO dto = modelMapper.map(customerAccount, CustomerAccountDTO.class);
        dto.setPaymentType(lookupCodeRepository.findByLookupCodeId(Integer.valueOf(dto.getPaymentType())).orElse("Unknown Activity"));
        dto.setCountryName(customerAccount.getCountry() != null ? customerAccount.getCountry().getCountryName() : "Unknown");
        dto.setStateName(customerAccount.getState() != null ? customerAccount.getState().getStateName() : "Unknown");
        dto.setCityName(customerAccount.getCity() != null ? customerAccount.getCity().getCityName() : "Unknown");
        dto.setCustomerName(customerAccount.getCustomer() != null ? customerAccount.getCustomer().getCustomerName() : "Unknown");


        return dto;
    }
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomersByAccountId(Integer accountId, Pageable pageable, String searchColumn, String searchValue) {
        Specification<CustomerSite> spec = (root, query, cb) -> cb.equal(root.get("customerAccount").get("accountId"), accountId);

        if (searchColumn != null && searchValue != null && !searchValue.trim().isEmpty() && !searchColumn.trim().isEmpty()) {
            spec = spec.and(buildCustomerSitespecifications(searchColumn, searchValue));
        }

        Page<CustomerSite> pageResult = customerSiteRepo.findAll(spec, pageable);

        List<CustomerSiteDTO> customerDTOs = pageResult.getContent().stream()
                .map(this::mapToCustomerSitesDTOs)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("result", customerDTOs);
        response.put("currentPage", pageable.getPageNumber()+1);
        response.put("pageSize", pageable.getPageSize());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }
    private Specification<CustomerSite> buildCustomerSitespecifications(String searchField, String searchKeyword) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate;
            String keyword = "%" + searchKeyword.toLowerCase() + "%";

            switch (searchField.toLowerCase()) {
                case "countryname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("country", JoinType.LEFT).get("countryName")), keyword);
                case "statename":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("state", JoinType.LEFT).get("stateName")), keyword);
                case "cityname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("city", JoinType.LEFT).get("cityName")), keyword);
                case "accountname":
                    return criteriaBuilder.like(criteriaBuilder.lower(root.join("customerAccount", JoinType.LEFT).get("accountName")), keyword);
                case "startdate":
                case "enddate":
                    Expression<String> formattedDate = criteriaBuilder.function("DATE_FORMAT", String.class, root.get(searchField),
                            criteriaBuilder.literal("%Y-%m-%d"));
                    return criteriaBuilder.like(criteriaBuilder.lower(formattedDate), keyword);
                default:
                    try {
                        return criteriaBuilder.like(criteriaBuilder.lower(root.get(searchField)), keyword);
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Invalid search field: " + searchField + " - " + ex.getMessage());
                        return criteriaBuilder.conjunction();
                    }
            }
        };
    }
}