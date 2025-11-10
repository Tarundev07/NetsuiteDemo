package com.atomicnorth.hrm.util;

import com.atomicnorth.hrm.tenant.service.dto.AdminUserDTO;
import lombok.Data;

import java.util.List;

@Data
public class OldAPIResponse<T> {
    int recordCount;
    T response;
    private List<AdminUserDTO> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
    public OldAPIResponse(int size, T allaccessgroups) {
    }
    public OldAPIResponse() {
    }
    public OldAPIResponse(int recordCount, T response, List<AdminUserDTO> content, long totalElements, int totalPages, int number, int size) {
        this.recordCount = recordCount;
        this.response = response;
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public List<AdminUserDTO> getContent() {
        return content;
    }

    public void setContent(List<AdminUserDTO> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
