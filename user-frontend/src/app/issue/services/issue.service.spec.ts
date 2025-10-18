import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { IssueService } from './issue.service';
import { HttpParams } from '@angular/common/http';
import { CreateIssueRequest, IssueResponse, PagedResponse, UpdateIssueStatusRequest } from '../models/issue.model';

describe('IssueService', () => {
  let service: IssueService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080/api/issues';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [IssueService]
    });
    service = TestBed.inject(IssueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // ensures no pending requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // Citizen Endpoints

  it('should create a draft issue', () => {
    const mockIssue: CreateIssueRequest = { title: 'Pothole', description: 'On main road' } as any;
    const mockResponse: IssueResponse = { id: '1', title: 'Pothole' } as any;

    service.createDraftIssue(mockIssue).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockIssue);
    req.flush(mockResponse);
  });

  it('should submit draft issue', () => {
    const id = '123';
    const mockResponse: IssueResponse = { id } as any;

    service.submitDraftIssue(id).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/${id}/submit`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should get my issues with params', () => {
    const mockResponse: PagedResponse<IssueResponse> = { content: [], totalElements: 0 } as any;

    service.getMyIssues(1, 10, 'createdAt', 'DESC', 'OPEN').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      r => r.url === `${baseUrl}/my` && r.params.has('page') && r.params.get('status') === 'OPEN'
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get my issue details', () => {
    const id = '10';
    const mockResponse: IssueResponse = { id } as any;

    service.getMyIssueDetails(id).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/my/${id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  // Staff Endpoints

  it('should get open issues', () => {
    const mockResponse: PagedResponse<IssueResponse> = { content: [], totalElements: 5 } as any;

    service.getOpenIssues(0, 12, 'createdAt', 'DESC').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/open?page=0&size=12&sortBy=createdAt&direction=DESC`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get open issue details', () => {
    const id = '22';
    const mockResponse: IssueResponse = { id } as any;

    service.getOpenIssueDetails(id).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/open/${id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get assigned issues', () => {
    const mockResponse: PagedResponse<IssueResponse> = { content: [], totalElements: 10 } as any;

    service.getAssignedIssues(0, 12, 'updatedAt', 'DESC', 'IN_PROGRESS').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      r => r.url === `${baseUrl}/assigned` && r.params.get('status') === 'IN_PROGRESS'
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get assigned issue details', () => {
    const id = '33';
    const mockResponse: IssueResponse = { id } as any;

    service.getAssignedIssueDetails(id).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/assigned/${id}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should accept an issue', () => {
    const id = '44';
    const mockResponse: IssueResponse = { id, status: 'ACCEPTED' } as any;

    service.acceptIssue(id).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/${id}/accept`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);
  });

  it('should update issue status', () => {
    const id = '55';
    const updateReq: UpdateIssueStatusRequest = { status: 'RESOLVED', staffComment: 'Fixed' };
    const mockResponse: IssueResponse = { id, status: 'RESOLVED' } as any;

    service.updateIssueStatus(id, updateReq).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/${id}/status`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateReq);
    req.flush(mockResponse);
  });

  it('should get open issue locks', () => {
    const mockResponse = { '1': true, '2': false };

    service.getOpenIssueLocks().subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/open/locks`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should release lock', () => {
    const id = '66';

    service.releaseLock(id).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/open/${id}/lock`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
