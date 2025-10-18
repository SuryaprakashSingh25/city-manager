package com.project.issue_service.infrastructure.persistence;

import com.project.issue_service.domain.enums.Status;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;
import com.project.issue_service.domain.repository.IssueRepository;
import com.project.issue_service.exposition.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoIssueRepository implements IssueRepository {
    private final SpringDataIssueRepository springDataRepo;

    @Override
    public Issue save(Issue issue) {
        IssueDocument saved=springDataRepo.save(IssueMapper.toDocument(issue));
        return IssueMapper.toDomain(saved);
    }

    @Override
    public Optional<Issue> findById(IssueId id) {
        return springDataRepo.findById(id.getValue())
                .map(IssueMapper::toDomain);
    }

    @Override
    public PagedResponse<Issue> findByCreator(String creatorUserId,int page,int size,String sortBy,String direction,Status status) {
        Sort sort=Sort.by(Sort.Direction.fromString(direction),sortBy);
        Pageable pageable= PageRequest.of(page,size,sort);

        Page<IssueDocument> pageResult;

        if(status!=null){
            pageResult=springDataRepo.findByCreatorUserIdAndStatus(creatorUserId,status,pageable);
        }
        else{
            pageResult=springDataRepo.findByCreatorUserId(creatorUserId,pageable);
        }

        List<Issue> issues=pageResult.getContent().stream()
                .map(IssueMapper::toDomain)
                .toList();

        return new PagedResponse<>(
                issues,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );
    }

    @Override
    public PagedResponse<Issue> findAssignedToStaff(String staffId, int page, int size, String sortBy, String direction, Status status) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueDocument> pageResult;
        if (status != null) {
            pageResult = springDataRepo.findByAssignedStaffIdAndStatus(staffId, status, pageable);
        } else {
            pageResult = springDataRepo.findByAssignedStaffId(staffId, pageable);
        }

        List<Issue> issues = pageResult.getContent().stream()
                .map(IssueMapper::toDomain)
                .toList();

        return new PagedResponse<>(
                issues,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );
    }

    @Override
    public PagedResponse<Issue> findOpenIssues(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueDocument> pageResult = springDataRepo.findByStatus(Status.OPEN, pageable);

        List<Issue> issues = pageResult.getContent().stream()
                .map(IssueMapper::toDomain)
                .toList();

        return new PagedResponse<>(
                issues,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );
    }

}
