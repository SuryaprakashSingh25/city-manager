package com.project.issue_service.exposition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateIssueRequest {
    private String title;
    private String description;
    private String location;
    private List<String> attachmentIds;
}
