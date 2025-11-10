package com.atomicnorth.hrm.tenant.service.logs;

import com.atomicnorth.hrm.tenant.domain.attendance.AttendanceMoaf;


import com.atomicnorth.hrm.tenant.service.dto.logs.ErrorDetails;
import com.atomicnorth.hrm.tenant.service.dto.logs.LogRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class LoggingService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void logInfoToThirdParty(AttendanceMoaf attendanceMoaf) {
        try {
            String url = "http://atm.supraesapp.com/internal-errorlog-api/api/loggingApi/LogInfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AttendanceMoaf> request = new HttpEntity<>(attendanceMoaf, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Logging response: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("Failed to log to third-party API: " + e.getMessage());
        }
    }

    public void customLogExceptionToThirdParty(Exception e,LogRequest logRequestObj) {
        ErrorDetails error = new ErrorDetails();
        error.setLogType(0);
        error.setFunctionName(logRequestObj.getError().getFunctionName());
        error.setClassName(logRequestObj.getError().getClassName());
        error.setModuleCode(logRequestObj.getError().getModuleCode());
        error.setIpInformation("127.0.0.1");
        error.setUserId(logRequestObj.getError().getUserId());

        LogRequest logRequest = new LogRequest();
        logRequest.setError(error);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                .withZone(ZoneOffset.UTC);

        String logDate = formatter.format(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        logRequest.setLogDate(logDate);
        // logRequest.setLogDate(formattedDate);
        logRequest.setMessage(logRequestObj.getMessage());
        logRequest.setSource("YourAppName");
        logRequest.setStackTrace(getStackTraceAsString(e));
        logRequest.setTargetSite(e.toString());
        logRequest.setLogTable("CustomErrorLog");
        logRequest.setJsonData("{}");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LogRequest> request = new HttpEntity<>(logRequest, headers);
        String logApiUrl = "http://atm.supraesapp.com/internal-errorlog-api/api/loggingApi/LogCustomError";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(logApiUrl, request, String.class);
        } catch (HttpStatusCodeException ex) {
            System.err.println("Failed to log to third-party: " + ex.getMessage());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
