import { CommonModule } from "@angular/common";
import { Component, OnInit, OnDestroy } from "@angular/core";
import { IssueResponse, PagedResponse } from "../../../models/issue.model";
import { IssueService } from "../../../services/issue.service";
import { RouterModule } from "@angular/router";
import { Subscription, timer } from "rxjs";
import { switchMap } from "rxjs/operators";
import { ToastService } from "../../../../shared/toast/toast.service";

@Component({
    selector: 'app-staff-issues',
    imports: [CommonModule, RouterModule],
    templateUrl: './issues.home.component.html',
    styleUrls: ['./issues.home.component.scss']
})
export class StaffIssueComponent implements OnInit, OnDestroy {
    issues: IssueResponse[] = [];
    selectedTab: 'open' | 'assigned' = 'open';

    currentPage: number = 0;
    pageSize: number = 12;
    totalPages: number = 0;
    totalItems: number = 0;

    sortBy: string = 'createdAt';
    sortDirection: 'ASC' | 'DESC' = 'DESC';
    filterStatus?: string;


    private lockSubscription?: Subscription;

    constructor(private issueService: IssueService, private toast: ToastService) {}

    ngOnInit(): void {
        this.loadIssues();
    }

    loadIssues() {

        if (this.selectedTab === 'open') {
            this.filterStatus = undefined;
            this.sortBy = 'createdAt';
            this.loadOpenIssues();
        } else {
            this.sortBy = 'updatedAt';
            this.loadAssignedIssues();
        }
    }

    private loadOpenIssues() {
        this.issueService.getOpenIssues(
            this.currentPage,
            this.pageSize,
            this.sortBy,
            this.sortDirection
        ).subscribe({
            next: (paged: PagedResponse<IssueResponse>) => {
                this.issues = paged.content.map(issue => ({ ...issue, locked: false }));
                this.currentPage = paged.currentPage;
                this.totalPages = paged.totalPages;
                this.totalItems = paged.totalItems;
                this.startLockPolling();
            },
            error: () => {
                this.toast.error('Failed to load open issues.');
            }
        });
    }

    private loadAssignedIssues() {
        this.stopLockPolling();
        this.issueService.getAssignedIssues(
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
            error: () => {
                this.toast.error('Failed to load assigned issues.');
            }
        });
    }

    onToggle(tab: 'open' | 'assigned') {
        this.selectedTab = tab;
        this.currentPage=0;
        this.loadIssues();
    }

    goToPage(page: number) {
        if (page >= 0 && page < this.totalPages) {
            this.currentPage = page;
            this.loadIssues();
        }
    }

    changePageSize(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.loadIssues();
    }

    changeSort(event: Event) {
        const sortBy = (event.target as HTMLSelectElement).value;
        if (this.sortBy === sortBy) {
            this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
        } else {
            this.sortBy = sortBy;
            this.sortDirection = 'DESC';
        }
        this.loadIssues();
    }

    setFilterStatus(event: Event) {
        const status = (event.target as HTMLSelectElement).value || undefined;
        this.filterStatus = status;
        this.currentPage = 0;
        this.loadIssues();
    }

    private startLockPolling() {
        this.stopLockPolling();

        this.lockSubscription = timer(0, 15000)
            .pipe(
                switchMap(() => this.issueService.getOpenIssueLocks())
            )
            .subscribe({
                next: (lockMap) => {
                    this.issues = this.issues.map(issue => ({
                        ...issue,
                        locked: !!lockMap[issue.id]
                    }));
                },
                error: () => {
                    console.error("Failed to update locks");
                }
            });
    }

    private stopLockPolling() {
        if (this.lockSubscription) {
            this.lockSubscription.unsubscribe();
            this.lockSubscription = undefined;
        }
    }

    ngOnDestroy(): void {
        this.stopLockPolling();
    }
}
