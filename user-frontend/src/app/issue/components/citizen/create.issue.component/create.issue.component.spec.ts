import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateIssueComponent } from './create.issue.component';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { HttpEventType } from '@angular/common/http';
import { IssueService } from '../../../services/issue.service';
import { MediaService } from '../../../../media/service/media.service';
import { ToastService } from '../../../../shared/toast/toast.service';

// Mock services
class MockIssueService {
  createDraftIssue = jasmine.createSpy('createDraftIssue').and.returnValue(of({ id: '123' }));
  submitDraftIssue = jasmine.createSpy('submitDraftIssue').and.returnValue(of({}));
}
class MockMediaService {
  uploadMedia = jasmine.createSpy('uploadMedia');
  deleteMedia = jasmine.createSpy('deleteMedia').and.returnValue(of({}));
}
class MockToastService {
  error = jasmine.createSpy('error');
  success = jasmine.createSpy('success');
}
class MockRouter {
  navigate = jasmine.createSpy('navigate');
}

describe('CreateIssueComponent (standalone)', () => {
  let component: CreateIssueComponent;
  let fixture: ComponentFixture<CreateIssueComponent>;
  let issueService: MockIssueService;
  let mediaService: MockMediaService;
  let toast: MockToastService;
  let router: MockRouter;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // ✅ For standalone components, use imports[], not declarations[]
      imports: [CreateIssueComponent, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: IssueService, useClass: MockIssueService },
        { provide: MediaService, useClass: MockMediaService },
        { provide: ToastService, useClass: MockToastService },
        { provide: Router, useClass: MockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateIssueComponent);
    component = fixture.componentInstance;
    issueService = TestBed.inject(IssueService) as any;
    mediaService = TestBed.inject(MediaService) as any;
    toast = TestBed.inject(ToastService) as any;
    router = TestBed.inject(Router) as any;
    fixture.detectChanges();
  });

  it('should create component and initialize form', () => {
    expect(component).toBeTruthy();
    expect(component.issueForm).toBeDefined();
    expect(component.issueForm.valid).toBeFalse();
  });

  it('should mark form valid when all fields are filled', () => {
    component.issueForm.setValue({
      title: 'Test title',
      description: 'Test description',
      location: 'Test location',
    });
    expect(component.issueForm.valid).toBeTrue();
  });

  it('should save draft issue successfully', () => {
    component.issueForm.setValue({
      title: 'T1',
      description: 'D1',
      location: 'L1',
    });

    component.saveDraft();
    expect(issueService.createDraftIssue).toHaveBeenCalledWith({
      title: 'T1',
      description: 'D1',
      location: 'L1',
    });
    expect(component.draftIssueId).toBe('123');
  });

  it('should show error toast on draft save failure', () => {
    issueService.createDraftIssue.and.returnValue(throwError(() => new Error('fail')));
    component.issueForm.setValue({
      title: 'T1',
      description: 'D1',
      location: 'L1',
    });
    component.saveDraft();
    expect(toast.error).toHaveBeenCalledWith('Failed to save draft.');
  });

  it('should set selected file on file selection', () => {
    const mockFile = new File(['test'], 'test.png', { type: 'image/png' });
    const event = { target: { files: [mockFile] } };
    component.onFileSelected(event);
    expect(component.selectedFile).toBe(mockFile);
  });

  it('should update upload progress', () => {
    component.draftIssueId = '123';
    const mockFile = new File(['test'], 'file.png');
    component.selectedFile = mockFile;

    mediaService.uploadMedia.and.returnValue(
      of({ type: HttpEventType.UploadProgress, loaded: 50, total: 100 })
    );

    component.uploadMedia();
    expect(component.uploadProgress).toBe(50);
  });

  it('should handle successful upload response', () => {
    component.draftIssueId = '123';
    component.selectedFile = new File(['test'], 'file.png');
    const mockResponse = {
      type: HttpEventType.Response,
      body: { id: 'm1', fileName: 'file.png' },
    };

    mediaService.uploadMedia.and.returnValue(of(mockResponse));
    component.uploadMedia();

    expect(component.uploadedMedia.length).toBe(1);
    expect(component.uploadedMedia[0].mediaId).toBe('m1');
    expect(component.selectedFile).toBeNull();
    expect(component.uploadProgress).toBe(0);
  });

  it('should show error toast on upload failure', () => {
    component.draftIssueId = '123';
    component.selectedFile = new File(['test'], 'file.png');
    mediaService.uploadMedia.and.returnValue(throwError(() => new Error('fail')));

    component.uploadMedia();
    expect(toast.error).toHaveBeenCalledWith('Failed to upload image.');
  });

  it('should submit issue and navigate on success', () => {
    component.draftIssueId = '123';
    component.uploadedMedia = [{ fileName: 'f.png', mediaId: 'm1' }];

    component.submitIssue();
    expect(issueService.submitDraftIssue).toHaveBeenCalledWith('123');
    expect(router.navigate).toHaveBeenCalledWith(['/citizen/issues']);
  });

  it('should show error toast on submit failure', () => {
    component.draftIssueId = '123';
    component.uploadedMedia = [{ fileName: 'f.png', mediaId: 'm1' }];
    issueService.submitDraftIssue.and.returnValue(throwError(() => new Error('fail')));

    component.submitIssue();
    expect(toast.error).toHaveBeenCalledWith('Failed to submit issue.');
  });

  it('should remove uploaded media successfully', () => {
    component.uploadedMedia = [
      { fileName: 'f1.png', mediaId: 'm1' },
      { fileName: 'f2.png', mediaId: 'm2' },
    ];

    component.removeUploadedMedia('m1');
    expect(mediaService.deleteMedia).toHaveBeenCalledWith('m1');
    expect(component.uploadedMedia.length).toBe(1);
    expect(component.uploadedMedia[0].mediaId).toBe('m2');
    expect(toast.success).toHaveBeenCalledWith('Media removed successfully!');
  });

  it('should show error toast if media deletion fails', () => {
    mediaService.deleteMedia.and.returnValue(throwError(() => new Error('fail')));
    component.uploadedMedia = [{ fileName: 'f1.png', mediaId: 'm1' }];

    component.removeUploadedMedia('m1');
    expect(toast.error).toHaveBeenCalledWith('Failed to delete media.');
  });
});
