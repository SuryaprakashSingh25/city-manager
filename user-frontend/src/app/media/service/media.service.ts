import { HttpClient, HttpEvent } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { MediaResponse } from "../model/media.model";

@Injectable({
    providedIn: 'root'
})
export class MediaService{
    private baseUrl='http://localhost:8080/api/media';

    constructor(private http: HttpClient){}

    uploadMedia(issueId: string, file: File): Observable<HttpEvent<any>> {
        const formData=new FormData();
        formData.append('file',file);

        return this.http.post<any>(`${this.baseUrl}/upload/${issueId}`,formData,{
            reportProgress: true,
            observe: 'events'
        });
    }

    downloadMedia(mediaId: string): Observable<Blob>{
        return this.http.get(`${this.baseUrl}/download/${mediaId}`,{
            responseType: 'blob'
        });
    }

    deleteMedia(mediaId: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${mediaId}`);
    }

    getMediaById(mediaId: string): Observable<MediaResponse> {
        return this.http.get<MediaResponse>(`${this.baseUrl}/${mediaId}`);
    }

    getMediaByIssue(issueId: string): Observable<MediaResponse[]> {
        return this.http.get<MediaResponse[]>(`${this.baseUrl}/issues/${issueId}`);
    }

    getPresignedUrl(mediaId: string): Observable<{url: string}> {
        return this.http.get<{url: string}>(`${this.baseUrl}/presigned/${mediaId}`);
    }

}