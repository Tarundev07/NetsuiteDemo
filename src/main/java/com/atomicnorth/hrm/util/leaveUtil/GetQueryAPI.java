package com.atomicnorth.hrm.util.leaveUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetQueryAPI {


    static final Logger logger = LoggerFactory.getLogger(GetQueryAPI.class);

    public static String getQuery(String query, String... args) {
        StringBuffer temp = new StringBuffer(query);
        int count = 0;
        while (temp.indexOf("$") != -1) {
            count++;
            temp.deleteCharAt(temp.indexOf("$"));
        }
        temp = new StringBuffer(query);


        if (count != args.length) {
            return "Arguments mismatch";
        }

        count = 1;

        for (String arg : args) {
            temp.replace(temp.indexOf("$" + count), temp.indexOf("$" + count) + ("$" + count).length(), arg);

            count++;
        }
        query = temp.toString();
        logger.info("Query Returned:" + query);
        return query;
    }
}
