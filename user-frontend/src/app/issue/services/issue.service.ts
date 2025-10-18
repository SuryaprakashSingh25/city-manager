import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { CreateIssueRequest, IssueResponse, PagedResponse, UpdateIssueStatusRequest } from "../models/issue.model";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class IssueService{
    private baseUrl = 'http://localhost:8080/api/issues';

    constructor(private http:HttpClient){}

    //Citizen Endpoints

    createDraftIssue(issue: CreateIssueRequest): Observable<IssueResponse>{
        return this.http.post<IssueResponse>(this.baseUrl,issue);
    }

    submitDraftIssue(issueId: string): Observable<IssueResponse>{
        return this.http.put<IssueResponse>(`${this.baseUrl}/${issueId}/submit`,{})
    }

    getMyIssues(
        page: number = 0,
        size: number = 12,
        sortBy: string = 'createdAt',
        direction: 'ASC' | 'DESC' = 'DESC',
        status?: string
    ):Observable<PagedResponse<IssueResponse>>{
        let params=new HttpParams()
        .set('page',page)
        .set('size',size)
        .set('sortBy',sortBy)
        .set('direction',direction);

        if(status){
            params=params.set('status',status);
        }

        return this.http.get<PagedResponse<IssueResponse>>(`${this.baseUrl}/my`,{params});
    }

    getMyIssueDetails(id:string): Observable<IssueResponse>{
        return this.http.get<IssueResponse>(`${this.baseUrl}/my/${id}`);
    }

    //Staff Endpoints
    
    getOpenIssues(
        page: number = 0,
        size: number = 12,
        sortBy: string = 'createdAt',
        direction: 'ASC' | 'DESC' = 'DESC' 
    ): Observable<PagedResponse<IssueResponse>>{
        let params=new HttpParams()
        .set('page',page)
        .set('size',size)
        .set('sortBy',sortBy)
        .set('direction',direction);
        return this.http.get<PagedResponse<IssueResponse>>(`${this.baseUrl}/open`,{params});
    }

    getOpenIssueDetails(id: string): Observable<IssueResponse>{
        return this.http.get<IssueResponse>(`${this.baseUrl}/open/${id}`);
    }

    getAssignedIssues(
        page: number = 0,
        size: number = 12,
        sortBy: string = 'updatedAt',
        direction: 'ASC' | 'DESC' = 'DESC',
        status?: string
    ): Observable<PagedResponse<IssueResponse>>{
        let params=new HttpParams()
        .set('page',page)
        .set('size',size)
        .set('sortBy',sortBy)
        .set('direction',direction);

        if(status){
            params=params.set('status',status);
        }
        return this.http.get<PagedResponse<IssueResponse>>(`${this.baseUrl}/assigned`,{params});
    }

    getAssignedIssueDetails(id:string):Observable<IssueResponse>{
        return this.http.get<IssueResponse>(`${this.baseUrl}/assigned/${id}`);
    }

    acceptIssue(issueId:string): Observable<IssueResponse>{
        return this.http.put<IssueResponse>(`${this.baseUrl}/${issueId}/accept`,{});
    }

    updateIssueStatus(id:string, {status,staffComment}: UpdateIssueStatusRequest):Observable<IssueResponse>{
        return this.http.put<IssueResponse>(`${this.baseUrl}/${id}/status`,{status,staffComment});
    }

    getOpenIssueLocks(): Observable<Record<string, boolean>> {
        return this.http.get<Record<string, boolean>>(`${this.baseUrl}/open/locks`);
    }

    releaseLock(issueId: string) {
        return this.http.delete(`${this.baseUrl}/open/${issueId}/lock`);
    }

}