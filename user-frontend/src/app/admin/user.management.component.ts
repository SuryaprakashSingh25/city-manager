import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/services/auth.service';
import { UserResponse } from '../core/models/auth.models';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user.management.component.html',
  styleUrls: ['./user.management.component.scss']
})
export class UserManagementComponent {
  users: UserResponse[] = [];
  message = '';

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.auth.getAllUsers().subscribe({
      next: res => this.users = res,
      error: err => this.message = err.error?.message || 'Failed to load users'
    });
  }

  updateRole(user: UserResponse, event: Event) {
    const newRole = (event.target as HTMLSelectElement).value;
    this.auth.changeUserRole(user.id, newRole).subscribe({
      next: updatedUser => {
        user.role = updatedUser.role; // update UI
      },
      error: err => console.error(err)
    });
  }
}
