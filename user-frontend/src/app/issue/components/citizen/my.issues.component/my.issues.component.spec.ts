import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MyIssueComponent } from './my.issues.component';
import { IssueService } from '../../../services/issue.service';
import { ToastService } from '../../../../shared/toast/toast.service';
import { of, throwError } from 'rxjs';
import { IssueResponse, PagedResponse } from '../../../models/issue.model';
import { RouterTestingModule } from '@angular/router/testing';

describe('MyIssueComponent', () => {
  let component: MyIssueComponent;
  let fixture: ComponentFixture<MyIssueComponent>;
  let issueServiceSpy: jasmine.SpyObj<IssueService>;
  let toastServiceSpy: jasmine.SpyObj<ToastService>;

  const mockPagedResponse: PagedResponse<IssueResponse> = {
    content: [
      { id: '1', title: 'Issue 1', description: 'Desc 1', location: 'Loc 1', creatorUserId: 'user1', status: 'OPEN', staffComment: '', createdAt: '', updatedAt: '' },
      { id: '2', title: 'Issue 2', description: 'Desc 2', location: 'Loc 2', creatorUserId: 'user2', status: 'RESOLVED', staffComment: '', createdAt: '', updatedAt: '' }
    ],
    currentPage: 0,
    totalPages: 2,
    totalItems: 2
  };

  beforeEach(async () => {
    issueServiceSpy = jasmine.createSpyObj('IssueService', ['getMyIssues']);
    toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success']);

    await TestBed.configureTestingModule({
      imports: [
        MyIssueComponent,      // standalone component
        RouterTestingModule    // provides ActivatedRoute & routerLink
      ],
      providers: [
        { provide: IssueService, useValue: issueServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyIssueComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(component.issues.length).toBe(2);
    expect(component.totalPages).toBe(2);
  });

  it('should load issues on init', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    fixture.detectChanges();
    expect(issueServiceSpy.getMyIssues).toHaveBeenCalled();
    expect(component.issues[0].title).toBe('Issue 1');
  });

  it('should show error toast when loading issues fails', () => {
    issueServiceSpy.getMyIssues.and.returnValue(throwError(() => new Error('Failed')));
    fixture.detectChanges();
    expect(toastServiceSpy.error).toHaveBeenCalledWith('Failed to load issues.');
  });

  it('should change page size and reload issues', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    component.changePageSize(24);
    expect(component.pageSize).toBe(24);
    expect(issueServiceSpy.getMyIssues).toHaveBeenCalled();
  });

  it('should set filter status and reload issues', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    const event = { target: { value: 'OPEN' } } as unknown as Event;
    component.setFilterStatus(event);
    expect(component.filterStatus).toBe('OPEN');
    expect(component.currentPage).toBe(0);
    expect(issueServiceSpy.getMyIssues).toHaveBeenCalled();
  });

  it('should change sortBy and reset sort direction if new field selected', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    const event = { target: { value: 'updatedAt' } } as unknown as Event;
    component.sortBy = 'createdAt';
    component.sortDirection = 'DESC';
    component.changeSort(event);
    expect(component.sortBy).toBe('updatedAt');
    expect(component.sortDirection).toBe('DESC'); // reset direction
    expect(issueServiceSpy.getMyIssues).toHaveBeenCalled();
  });

  it('should toggle sort direction if same field selected', () => {
    issueServiceSpy.getMyIssues.and.returnValue(of(mockPagedResponse));
    const event = { target: { value: 'createdAt' } } as unknown as Event;
    component.sortBy = 'createdAt';
    component.sortDirection = 'DESC';
    component.changeSort(event);
    expect(component.sortDirection).toBe('ASC'); // toggled
    expect(issueServiceSpy.getMyIssues).toHaveBeenCalled();
  });
});
