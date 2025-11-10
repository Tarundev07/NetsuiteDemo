package com.atomicnorth.hrm.tenant.service.util;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Utility {

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
