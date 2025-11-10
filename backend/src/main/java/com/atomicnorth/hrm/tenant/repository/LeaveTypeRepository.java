package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.branch.LeaveTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveTypes, Integer> {

    @Query("SELECT lt FROM LeaveTypes lt WHERE " +
            "(:searchField = 'leaveName' AND LOWER(lt.leaveName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isCarryForward' AND LOWER(lt.isCarryForward) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isLeaveWithoutPay' AND LOWER(lt.isLeaveWithoutPay) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isPartialPaidLeave' AND LOWER(lt.isPartialPaidLeave) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isOptionalLeave' AND LOWER(lt.isOptionalLeave) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'allowNegativeBalance' AND LOWER(lt.allowNegativeBalance) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'allowOverApplication' AND LOWER(lt.allowOverApplication) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'includeHolidaysWithinLeaves' AND LOWER(lt.includeHolidaysWithinLeaves) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isCompensatoryLeaves' AND LOWER(lt.isCompensatoryLeaves) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'allocationAllowedLeavePeriod' AND LOWER(lt.allocationAllowedLeavePeriod) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'allowLeaveApplicationAfterWorkingDays' AND LOWER(lt.allowLeaveApplicationAfterWorkingDays) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'maximumConsecutiveLeaveAllowed' AND LOWER(lt.maximumConsecutiveLeaveAllowed) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'maximumLeave' AND CAST(lt.maximumLeave AS string) LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "(:searchField = 'maxIncashableLeave' AND CAST(lt.maxIncashableLeave AS string) LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "(:searchField = 'nonIncashableLeave' AND CAST(lt.nonIncashableLeave AS string) LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "(:searchField = 'earningComponent' AND CAST(lt.earningComponent AS string) LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "(:searchField = 'maxCarryForwardDays' AND CAST(lt.maxCarryForwardDays AS string) LIKE CONCAT('%', :searchKeyword, '%')) OR " +
            "(:searchField = 'allowEncashment' AND LOWER(lt.allowEncashment) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'isEarnedleave' AND LOWER(lt.isEarnedleave) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR " +
            "(:searchField = 'applicable' AND CAST(lt.applicable AS string) LIKE CONCAT('%', :searchKeyword, '%'))")
    Page<LeaveTypes> searchLeaveTypes(@Param("searchKeyword") String searchKeyword,
                                      @Param("searchField") String searchField,
                                      Pageable pageable);


    Optional<LeaveTypes> findByLeaveCode(String leaveCode);

}
