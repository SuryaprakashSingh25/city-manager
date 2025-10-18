import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserManagementComponent } from './user.management.component';
import { AuthService } from '../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { UserResponse } from '../core/models/auth.models';
import { CommonModule } from '@angular/common';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let authService: AuthService;

  const mockUsers: UserResponse[] = [
    { id: '1', email: 'alice@example.com', role: 'USER', status: 'ACTIVE' },
    { id: '2', email: 'bob@example.com', role: 'ADMIN', status: 'ACTIVE' }
  ];

  // Mock AuthService
  const authServiceMock = {
    getAllUsers: jasmine.createSpy('getAllUsers').and.returnValue(of(mockUsers)),
    changeUserRole: jasmine.createSpy('changeUserRole').and.returnValue(of(mockUsers[0]))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserManagementComponent, CommonModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
  });

  beforeEach(() => {
    // Reset spies before each test
    authServiceMock.getAllUsers.calls.reset();
    authServiceMock.changeUserRole.calls.reset();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set message if getAllUsers fails', () => {
    const errorResponse = { error: { message: 'Failed!' } };
    authServiceMock.getAllUsers.and.returnValue(throwError(() => errorResponse));

    component.ngOnInit();

    expect(component.message).toBe('Failed!');
  });

  it('should update user role on updateRole', () => {
    const user = { id: '1', email: 'alice@example.com', role: 'USER', status: 'ACTIVE' };
    const updatedUser = { ...user, role: 'ADMIN' };
    authServiceMock.changeUserRole.and.returnValue(of(updatedUser));

    const event = { target: { value: 'ADMIN' } } as unknown as Event;

    component.updateRole(user, event);

    expect(authService.changeUserRole).toHaveBeenCalledWith(user.id, 'ADMIN');
    expect(user.role).toBe('ADMIN');
  });

  it('should log error if changeUserRole fails', () => {
    const user = { id: '1', email: 'alice@example.com', role: 'USER', status: 'ACTIVE' };
    const consoleSpy = spyOn(console, 'error');

    authServiceMock.changeUserRole.and.returnValue(throwError(() => new Error('Error')));

    const event = { target: { value: 'ADMIN' } } as unknown as Event;

    component.updateRole(user, event);

    expect(consoleSpy).toHaveBeenCalled();
  });
});
