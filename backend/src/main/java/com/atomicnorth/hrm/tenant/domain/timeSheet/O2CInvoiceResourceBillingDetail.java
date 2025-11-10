package com.atomicnorth.hrm.tenant.domain.timeSheet;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Setter
@Getter
@Component
public class O2CInvoiceResourceBillingDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    Map<String, Object> subLine;
    List<Map<String, Object>> allocationList;
    private Integer totalDaysInMonth;
    private Integer totalDaysInPeriod;
    private Double totalWorkingDaysInPeriod;
    private Double totalDaysWorked;
    private Double totalUserLeave;
    private Double totalBillableLeave;
    private Integer totalUsersHOInPeriod;
    private Double totalBillableHOInPeriod;
    private Integer totalWOInPeriod;
    private Double totalBillableWOInPeriod;
    private Double totalAdditionalDays;
    private String fromDate;
    private String toDate;
    private Integer username;
    private String fullName;
    private String userCode;
    private Integer projectRfNum;
    private String projectName;
    private String projectCode;
    private Set<String> missingTSDays;
    private Set<String> unFreezedTSDays;
    private String allocationDatesText;
    private Integer contractId;
    private Integer lineId;
    private Integer lineTypeId;
    private String contractRefNumber;
    private String contractStatus;
    private String lineType;
    private String lineName;
    private String resourceText;
    private String allFreezeFlag;
    private Integer missingTimesheetCount;
    private String allApprovedFlag;
    private String lastUpdatedOn;
    private String lastUpdatedBy;
    private String lastUpdatedByFullname;
    private Double projectBillingHoursPerDay;
    private String uomRate;

    public O2CInvoiceResourceBillingDetail() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "O2CInvoiceResourceBillingDetail [subLine="
                + (subLine != null ? toString(subLine.entrySet(), maxLen) : null) + ", totalDaysInMonth="
                + totalDaysInMonth + ", totalDaysInPeriod=" + totalDaysInPeriod + ", totalWorkingDaysInPeriod="
                + totalWorkingDaysInPeriod + ", totalDaysWorked=" + totalDaysWorked + ", totalUserLeave="
                + totalUserLeave + ", totalBillableLeave=" + totalBillableLeave + ", totalUsersHOInPeriod="
                + totalUsersHOInPeriod + ", totalBillableHOInPeriod=" + totalBillableHOInPeriod + ", totalWOInPeriod="
                + totalWOInPeriod + ", totalBillableWOInPeriod=" + totalBillableWOInPeriod + ", totalAdditionalDays="
                + totalAdditionalDays + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }
}

