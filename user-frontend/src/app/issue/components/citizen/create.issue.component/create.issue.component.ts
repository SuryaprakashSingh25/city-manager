import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterModule } from "@angular/router";
import { IssueService } from "../../../services/issue.service";
import { MediaService } from "../../../../media/service/media.service";
import { HttpEventType } from "@angular/common/http";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector: 'app-create-issue',
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './create.issue.component.html',
    styleUrls: ['./create.issue.component.scss']
})
export class CreateIssueComponent {
  issueForm: FormGroup;
  selectedFile: File | null = null;
  uploadProgress: number = 0;
  draftIssueId: string | null = null;
  uploadedMedia: { fileName: string, mediaId: string }[] = [];

  constructor(
    private fb: FormBuilder,
    private issueService: IssueService,
    private mediaService: MediaService,
    private router: Router,
    private toast: ToastService
  ) {
    this.issueForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      location: ['', Validators.required]
    });
  }

  saveDraft() {
    if (this.issueForm.invalid || this.draftIssueId) return;

    const { title, description, location } = this.issueForm.value;
    this.issueService.createDraftIssue({ title, description, location }).subscribe({
      next: (issue) => {
        this.draftIssueId = issue.id;
      },
      error: () => this.toast.error('Failed to save draft.')
    });
  }

  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  uploadMedia() {
    if (!this.selectedFile || !this.draftIssueId) return;

    this.mediaService.uploadMedia(this.draftIssueId, this.selectedFile).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round((100 * event.loaded) / event.total);
        } else if (event.type === HttpEventType.Response) {
          const media = event.body;
          this.uploadedMedia.push({ fileName: media.fileName, mediaId: media.id });
          this.selectedFile = null;
          this.uploadProgress = 0;
        }
      },
      error: () => this.toast.error('Failed to upload image.')
    });
  }

  submitIssue() {
    if (!this.draftIssueId || this.uploadedMedia.length === 0) return;

    this.issueService.submitDraftIssue(this.draftIssueId).subscribe({
      next: () => {
        this.router.navigate(['/citizen/issues']);
      },
      error: () => this.toast.error('Failed to submit issue.')
    });
  }

  removeUploadedMedia(mediaId: string) {
    this.mediaService.deleteMedia(mediaId).subscribe({
      next: () => {
        this.uploadedMedia = this.uploadedMedia.filter(m => m.mediaId !== mediaId);
        this.toast.success('Media removed successfully!');
      },
      error: () => this.toast.error('Failed to delete media.')
    });
  }
}
