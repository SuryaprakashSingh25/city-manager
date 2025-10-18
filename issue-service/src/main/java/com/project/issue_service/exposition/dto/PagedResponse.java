package com.project.issue_service.exposition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalItems;

}
