import { Routes } from "@angular/router";
import { AuthGuard } from "../../../core/guards/auth.guard";
import { StaffIssueComponent } from "../../components/staff/issues.home.component/issues.home.component";
import { StaffOpenComponent } from "../../components/staff/open.issue.detail.component/open.issue.component";
import { StaffAssignedComponent } from "../../components/staff/assigned.issue.detail.component/assigned.issue.component";

export const staffRoutes: Routes = [
  {
    path: 'issues',
    component: StaffIssueComponent,
    canActivate: [AuthGuard],
    data: { roles: ['STAFF'] }
  },
  {
    path: 'issues/open/:id',
    component: StaffOpenComponent,
    canActivate: [AuthGuard],
    data: {roles: ['STAFF']}
  },
  {
    path: 'issues/assigned/:id',
    component: StaffAssignedComponent,
    canActivate: [AuthGuard],
    data: {roles: ['STAFF']}
  }
  
];