import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { IssueResponse, PagedResponse } from "../../../models/issue.model";
import { IssueService } from "../../../services/issue.service";
import { RouterModule } from "@angular/router";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector:'app-my-issues',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl:'./my.issues.component.html',
    styleUrls:['./my.issues.component.scss']
})
export class MyIssueComponent implements OnInit{
    issues: IssueResponse[] = [];

    currentPage: number = 0;
    pageSize: number = 12;
    totalPages: number = 0;
    totalItems: number = 0;

    sortBy: string = 'createdAt';
    sortDirection: 'ASC' | 'DESC' = 'DESC';
    filterStatus?: string;

    constructor(private issueService: IssueService, private toast: ToastService){}

    ngOnInit(): void {
        this.loadMyIssues();
    }

    loadMyIssues(){
        this.issueService.getMyIssues(
            this.currentPage,
            this.pageSize,
            this.sortBy,
            this.sortDirection,
            this.filterStatus
        ).subscribe({
            next: (paged: PagedResponse<IssueResponse>) => {
                this.issues = paged.content;
                this.currentPage = paged.currentPage;
                this.totalPages = paged.totalPages;
                this.totalItems = paged.totalItems;
            },
            error: (err) => {
                console.error(err);
                this.toast.error('Failed to load issues.');
            }
        });
    }

    goToPage(page: number){
        if(page >= 0 && page < this.totalPages){
            this.currentPage = page;
            this.loadMyIssues();
        }
    }

    changePageSize(size: number){
        this.pageSize = size;
        this.currentPage = 0;
        this.loadMyIssues();
    }

    changeSort(event: Event){
        const sortBy = (event.target as HTMLSelectElement).value;
        if(this.sortBy === sortBy){
            this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
        } else {
            this.sortBy = sortBy;
            this.sortDirection = 'DESC';
        }
        this.loadMyIssues();
    }

    setFilterStatus(event: Event) {
        const status = (event.target as HTMLSelectElement).value || undefined;
        this.filterStatus = status;
        this.currentPage = 0;
        this.loadMyIssues();
    }
}
