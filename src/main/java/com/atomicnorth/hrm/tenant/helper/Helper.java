package com.atomicnorth.hrm.tenant.helper;

import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Helper {

    public static PageableResponse<List<Map<String, Object>>> getPageableResponse(Page<Map<String, Object>> page) {
        PageableResponse<List<Map<String, Object>>> response = new PageableResponse<>();
        response.setContent(Collections.singletonList(page.getContent()));
        response.setPageNumber(page.getNumber() + 1); // Adding 1 to make page numbers start from 1
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());
        return response;
    }
}