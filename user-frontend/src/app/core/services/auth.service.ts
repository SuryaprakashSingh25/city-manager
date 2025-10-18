import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { 
  RegisterRequest,
  LoginRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  RefreshTokenRequest,
  AuthResponse,
  UserResponse,
  MessageResponse 
} from '../models/auth.models';
import { JwtHelperService } from '@auth0/angular-jwt';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/users';
  private authState = new BehaviorSubject<boolean>(this.hasToken());

  private jwtHelper = new JwtHelperService();

  isRefreshing = false;

  authState$ = this.authState.asObservable();

  constructor(private http: HttpClient) {}

  register(data: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/register`, data);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data).pipe(
      tap(res => this.saveTokens(res))
    );
  }

  verifyEmail(token: string): Observable<MessageResponse> {
    return this.http.get<MessageResponse>(`${this.apiUrl}/verify?token=${token}`);
  }

  forgotPassword(data: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, data);
  }

  resetPassword(data: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, data);
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh-token`, { refreshToken } as RefreshTokenRequest)
      .pipe(tap(res => this.saveTokens(res)));
  }

  getAllUsers() {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/allUsers`);
  }

  changeUserRole(userId: string, role: string) {
    return this.http.put<UserResponse>(`${this.apiUrl}/allUsers/${userId}/role`, { role });
  }


  logout() {
    localStorage.clear();
    this.authState.next(false);
  }

  getUserFromToken(): any | null {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    return this.jwtHelper.decodeToken(token); 
  }
  

  private saveTokens(res: AuthResponse) {
    localStorage.setItem('accessToken', res.accessToken);
    localStorage.setItem('refreshToken', res.refreshToken);
    this.authState.next(true);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('accessToken');
  }
}
