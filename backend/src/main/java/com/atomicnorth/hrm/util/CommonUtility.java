package com.atomicnorth.hrm.util;

import com.atomicnorth.hrm.tenant.repository.LookupCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommonUtility {

    @Autowired
    private LookupCodeRepository lookupCodeRepository;

    public String getDomainName() {
        try {
            return String.valueOf(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("APPLICATION_DOMAIN", "E_MANAGER_DOMAIN"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getEmanagerUrl() {
        try {
            return String.valueOf(lookupCodeRepository.findMeaningByLookupTypeAndLookupCode("APPLICATION_URL", "E_MANAGER_URL"));
        } catch (DataAccessException e) {
            e.printStackTrace();
            return "";
        }
    }
    public List<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        Calendar callast = new GregorianCalendar();
        callast.setTime(enddate);
        dates.add(callast.getTime());
        return dates;
    }
}
