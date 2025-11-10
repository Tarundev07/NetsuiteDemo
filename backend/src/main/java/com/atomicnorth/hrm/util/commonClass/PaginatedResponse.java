package com.atomicnorth.hrm.util.commonClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> paginationData;  // List of companies
    private int totalPages;  // Total pages available
    private long totalElements;  // Total number of elements (companies)
    private int pageSize;  // Number of companies per page
    private int currentPage;  // Current page number


}

