import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserResponse } from '../models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUsers: UserResponse[] = [
    { id: '1', email: 'alice@example.com', role: 'USER', status: 'ACTIVE' },
    { id: '2', email: 'bob@example.com', role: 'ADMIN', status: 'ACTIVE' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // ensure no outstanding requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all users', () => {
    service.getAllUsers().subscribe(users => {
      expect(users).toEqual(mockUsers);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/users/allUsers');
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should change user role', () => {
    const userId = '1';
    const newRole = 'ADMIN';
    const updatedUser: UserResponse = { ...mockUsers[0], role: newRole };

    service.changeUserRole(userId, newRole).subscribe(user => {
      expect(user.role).toBe(newRole);
    });

    const req = httpMock.expectOne(`http://localhost:8080/api/users/allUsers/${userId}/role`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ role: newRole });
    req.flush(updatedUser);
  });

  it('should handle error on getAllUsers', () => {
    const errorMessage = 'Server error';
    service.getAllUsers().subscribe({
      next: () => fail('Should have failed'),
      error: error => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne('http://localhost:8080/api/users/allUsers');
    req.flush({ message: errorMessage }, { status: 500, statusText: 'Server Error' });
  });
});
