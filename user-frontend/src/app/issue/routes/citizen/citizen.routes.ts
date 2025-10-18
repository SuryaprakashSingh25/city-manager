import { Routes } from "@angular/router";
import { MyIssueComponent } from "../../components/citizen/my.issues.component/my.issues.component";
import { AuthGuard } from "../../../core/guards/auth.guard";
import { IssueDetailComponent } from "../../components/citizen/issue.detail.component/issue.detail.component";
import { CreateIssueComponent } from "../../components/citizen/create.issue.component/create.issue.component";

export const citizenRoutes: Routes = [
  {
    path: 'issues',
    component: MyIssueComponent,
    canActivate: [AuthGuard],
    data: { roles: ['CITIZEN'] }
  },
  {
    path: 'issues/create',
    component: CreateIssueComponent,
    canActivate: [AuthGuard],
    data: { roles : ['CITIZEN']}
  },
  {
    path: 'issues/:id',
    component: IssueDetailComponent,
    canActivate: [AuthGuard],
    data: {roles: ['CITIZEN']}
  }
];