import { CommonModule } from "@angular/common";
import { Component, OnDestroy } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { IssueService } from "../../../services/issue.service";
import { MediaService } from "../../../../media/service/media.service";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector: 'open-issue-detail',
    imports: [CommonModule],
    templateUrl: './open.issue.component.html',
    styleUrls: ['./open.issue.component.scss']
})
export class StaffOpenComponent implements OnDestroy{
    issue!: any;
    mediaUrls: string[] = [];
    private issueId: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private issueService: IssueService,
        private mediaService: MediaService,
        private router: Router,
        private toast: ToastService
    ){}

    ngOnInit(): void {
        this.issueId = this.route.snapshot.paramMap.get('id');
        if(this.issueId){
            this.issueService.getOpenIssueDetails(this.issueId).subscribe({
                next: issue => {
                    this.issue = issue;

                    this.mediaService.getMediaByIssue(this.issueId!).subscribe({
                        next: mediaList => {
                            this.mediaUrls = mediaList.map(m => m.url);
                        }
                    });

                },
                error: (err) => {
                    console.error(err);
                    if(err.status === 409){ // locked by another staff
                        this.toast.error('This issue is currently locked by another staff.');
                    } else {
                        this.toast.error('Failed to load issue details.');
                    }
                }
            });
        }
    }

    ngOnDestroy(): void {
        if(this.issueId){
            this.issueService.releaseLock(this.issueId).subscribe({
                error: err => console.error('Failed to release lock', err)
            });
        }
    }

    acceptIssue() {
        if (!this.issueId) return;
        this.issueService.acceptIssue(this.issueId).subscribe({
            next: () => {
                this.router.navigate(['/staff/issues']);
            },
            error: err => {
                console.error("Failed to accept issue", err);
                this.toast.error(err.error?.message || "Failed to accept issue. Try again.");
            }
        });
    }



    goBack() {
        window.history.back(); 
    }
}