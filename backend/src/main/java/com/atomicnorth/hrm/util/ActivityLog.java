package com.atomicnorth.hrm.util;

import com.atomicnorth.hrm.tenant.repository.leave.LeaveLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class ActivityLog {

    static final Logger logger = LoggerFactory.getLogger(ActivityLog.class);
    @Autowired
    CommonUtility commonUtility;
    @Autowired
    private LeaveLedgerRepository leaveLedgerRepository;

    public synchronized void captureUserActivity(String activityCategoryId, String username, String objectId, String moduleId, String activityMessage, String createdBy) {
        try {
            //jdbcTemplate.update(GetQueryAPI.getQuery(TM235,activityCategoryId, username, objectId, moduleId, activityMessage, createdBy));
            leaveLedgerRepository.insertUserActivityLog(activityCategoryId, username, objectId, moduleId, activityMessage, createdBy);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private Map<String, Object> prepareModel(Object data, Object[] watcher) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mapData", data);
        model.put("fullName", watcher[0]); // Assuming that the first element is FULL_NAME
        /* Added for v1.11 start*/
        model.put("eManagerURL", commonUtility.getEmanagerUrl());
        /* Added for v1.11 end*/
        return model;
    }

}
