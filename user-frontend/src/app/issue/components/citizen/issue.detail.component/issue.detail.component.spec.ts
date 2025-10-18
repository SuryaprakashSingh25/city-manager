import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { IssueDetailComponent } from './issue.detail.component';
import { IssueService } from '../../../services/issue.service';
import { MediaService } from '../../../../media/service/media.service';
import { ToastService } from '../../../../shared/toast/toast.service';
import { CommonModule } from '@angular/common';
import { IssueResponse } from '../../../models/issue.model';
import { MediaResponse } from '../../../../media/model/media.model';

describe('IssueDetailComponent', () => {
  let component: IssueDetailComponent;
  let fixture: ComponentFixture<IssueDetailComponent>;

  // Mocks
  let issueServiceSpy: jasmine.SpyObj<IssueService>;
  let mediaServiceSpy: jasmine.SpyObj<MediaService>;
  let toastServiceSpy: jasmine.SpyObj<ToastService>;

  const mockActivatedRoute = {
    snapshot: {
      paramMap: {
        get: (key: string) => (key === 'id' ? '123' : null)
      }
    }
  };

  const mockIssue: IssueResponse = {
    id: '123',
    title: 'Road Damage',
    description: 'Pothole on Main Street',
    location: 'Main Street',
    creatorUserId: 'user1',
    status: 'OPEN',
    staffComment: '',
    createdAt: '2025-10-18T12:00:00Z',
    updatedAt: '2025-10-18T12:30:00Z'
  };

  const mockMediaList: MediaResponse[] = [
    {
      id: '1',
      issueId: '123',
      fileName: 'photo1.jpg',
      mediaType: 'IMAGE',
      contentType: 'image/jpeg',
      uploadedBy: 'user1',
      url: 'http://localhost/media1.jpg'
    },
    {
      id: '2',
      issueId: '123',
      fileName: 'photo2.jpg',
      mediaType: 'IMAGE',
      contentType: 'image/jpeg',
      uploadedBy: 'user1',
      url: 'http://localhost/media2.jpg'
    }
  ];

  beforeEach(async () => {
    issueServiceSpy = jasmine.createSpyObj('IssueService', ['getMyIssueDetails']);
    mediaServiceSpy = jasmine.createSpyObj('MediaService', ['getMediaByIssue']);
    toastServiceSpy = jasmine.createSpyObj('ToastService', ['error']);

    await TestBed.configureTestingModule({
      imports: [CommonModule, IssueDetailComponent], // standalone component
      providers: [
        { provide: IssueService, useValue: issueServiceSpy },
        { provide: MediaService, useValue: mediaServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(IssueDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load issue and media successfully on init', () => {
    issueServiceSpy.getMyIssueDetails.and.returnValue(of(mockIssue));
    mediaServiceSpy.getMediaByIssue.and.returnValue(of(mockMediaList));

    fixture.detectChanges(); // triggers ngOnInit

    expect(issueServiceSpy.getMyIssueDetails).toHaveBeenCalledWith('123');
    expect(mediaServiceSpy.getMediaByIssue).toHaveBeenCalledWith('123');
    expect(component.issue).toEqual(mockIssue);
    expect(component.mediaUrls).toEqual([
      'http://localhost/media1.jpg',
      'http://localhost/media2.jpg'
    ]);
  });

  it('should call window.history.back() when goBack() is invoked', () => {
    spyOn(window.history, 'back');
    component.goBack();
    expect(window.history.back).toHaveBeenCalled();
  });
});
