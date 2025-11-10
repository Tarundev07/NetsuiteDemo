package com.atomicnorth.hrm.master.web.rest;

import com.atomicnorth.hrm.master.domain.DataSourceConfig;
import com.atomicnorth.hrm.master.repository.DataSourceConfigRepository;
import com.atomicnorth.hrm.util.HeaderUtil;
import com.atomicnorth.hrm.util.ResponseUtil;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/data-source-configs")
public class DataSourceConfigResource {

    private static final String ENTITY_NAME = "dataSourceConfig";
    private final Logger log = LoggerFactory.getLogger(DataSourceConfigResource.class);
    private final DataSourceConfigRepository dataSourceConfigRepository;
    @Value("${spring.application.name}")
    private String applicationName;

    public DataSourceConfigResource(DataSourceConfigRepository dataSourceConfigRepository) {
        this.dataSourceConfigRepository = dataSourceConfigRepository;
    }

    @PostMapping
    @Timed
    public ResponseEntity<DataSourceConfig> createDataSourceConfig(@Valid @RequestBody DataSourceConfig dataSourceConfig) throws URISyntaxException {
        log.debug("REST request to save DataSourceConfig : {}", dataSourceConfig);
        if (dataSourceConfig.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(applicationName, true, ENTITY_NAME, "idexists", "A new dataSourceConfig cannot already have an ID")).body(null);
        }
        DataSourceConfig result = dataSourceConfigRepository.save(dataSourceConfig);
        return ResponseEntity.created(new URI("/api/data-source-configs/" + result.getId()))
                .headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, result.getId()))
                .body(result);
    }

    @PutMapping
    @Timed
    public ResponseEntity<DataSourceConfig> updateDataSourceConfig(@Valid @RequestBody DataSourceConfig dataSourceConfig) throws URISyntaxException {
        log.debug("REST request to update DataSourceConfig : {}", dataSourceConfig);
        if (dataSourceConfig.getId() == null) {
            return createDataSourceConfig(dataSourceConfig);
        }
        DataSourceConfig result = dataSourceConfigRepository.save(dataSourceConfig);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createAlert(applicationName, ENTITY_NAME, dataSourceConfig.getId()))
                .body(result);
    }

    @GetMapping
    @Timed
    public List<DataSourceConfig> getAllDataSourceConfigs() {
        log.debug("REST request to get all DataSourceConfigs");
        return dataSourceConfigRepository.findAll();
    }

    @GetMapping("{id}")
    @Timed
    public ResponseEntity<DataSourceConfig> getDataSourceConfig(@PathVariable Long id) {
        log.debug("REST request to get DataSourceConfig : {}", id);
        DataSourceConfig dataSourceConfig = dataSourceConfigRepository.getReferenceById(id);
        return ResponseUtil.wrapOrNotFound(Optional.of(dataSourceConfig));
    }
}
