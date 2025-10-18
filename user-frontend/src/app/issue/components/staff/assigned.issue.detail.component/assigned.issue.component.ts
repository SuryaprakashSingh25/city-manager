import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { IssueResponse } from "../../../models/issue.model";
import { ActivatedRoute, Router } from "@angular/router";
import { IssueService } from "../../../services/issue.service";
import { MediaService } from "../../../../media/service/media.service";
import { StatusCommentComponent } from "../../../../shared/status.comment.component/status.comment";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector: 'assigned-issue-detail',
    imports: [CommonModule, StatusCommentComponent],
    templateUrl: './assigned.issue.component.html',
    styleUrls: ['./assigned.issue.component.scss']
})
export class StaffAssignedComponent implements OnInit{
    issue!: IssueResponse;
    mediaUrls: { id: string, url: string }[] = [];
    showModal = false;
    pendingStatus: 'REJECTED' | 'ON_HOLD' | 'RESOLVED' | '' = '';

    constructor(
        private route: ActivatedRoute,
        private issueService: IssueService,
        private mediaService: MediaService,
        private router: Router,
        private toast: ToastService
    ){}

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if(id){
            this.issueService.getAssignedIssueDetails(id).subscribe({
                next: issue => {
                    this.issue = issue;
                    this.mediaService.getMediaByIssue(id).subscribe({
                        next: mediaList => {
                            this.mediaUrls = mediaList;
                        },
                        error: err => this.toast.error('Failed to load media.')
                    });
                },
                error: err => this.toast.error('Failed to load issue details.')
            });
        }
    }

    openModal(status: 'REJECTED' | 'ON_HOLD' | 'RESOLVED') {
        this.pendingStatus = status;
        this.showModal = true;
    }

    downloadMedia(mediaId: string) {
        this.mediaService.downloadMedia(mediaId).subscribe({
            next: (blob: Blob) => {
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `media-${mediaId}`;
                link.click();
                window.URL.revokeObjectURL(url);
                this.toast.success('Media downloaded successfully.');
            },
            error: () => this.toast.error('Failed to download media.')
        });
    }

    confirmComment(comment: string) {
        this.showModal = false;
        if (!this.pendingStatus) {
            this.toast.error('No status selected.');
            return;
        }

        const payload = { status: this.pendingStatus, staffComment: comment };
        this.issueService.updateIssueStatus(this.issue.id, payload).subscribe({
            next: updatedIssue => {
                this.issue.status = updatedIssue.status;
                this.toast.success(`Issue status updated to ${updatedIssue.status}.`);
                this.router.navigate(['/staff/issues']);
            },
            error: () => this.toast.error('Failed to update issue status.')
        });
    }

    cancelModal() {
        this.showModal = false;
    }

    goBack(){
        window.history.back();
    }
}
