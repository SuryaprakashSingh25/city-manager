import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { IssueService } from "../../../services/issue.service";
import { MediaService } from "../../../../media/service/media.service";
import { IssueResponse } from "../../../models/issue.model";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector: 'app-issue-detail',
    imports: [CommonModule],
    templateUrl: './issue.detail.component.html',
    styleUrls: ['./issue.detail.component.scss']
})
export class IssueDetailComponent implements OnInit{
    issue!: IssueResponse;
    mediaUrls: string[] = [];

    constructor(
        private route: ActivatedRoute,
        private issueService: IssueService,
        private mediaService: MediaService,
        private toast: ToastService
    ){}

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if(id){
            this.issueService.getMyIssueDetails(id).subscribe({
                next: issue => {
                    this.issue = issue;

                    this.mediaService.getMediaByIssue(id).subscribe({
                        next: mediaList => {
                            this.mediaUrls = mediaList.map(m => m.url);
                        },
                        error: (err) => {
                            console.error(err);
                            this.toast.error('Failed to load media.');
                        }
                    });

                },
                error: (err) => {
                    console.error(err);
                    this.toast.error('Failed to load issue details.');
                }
            });
        }
    }

    goBack() {
        window.history.back(); 
    }
}
