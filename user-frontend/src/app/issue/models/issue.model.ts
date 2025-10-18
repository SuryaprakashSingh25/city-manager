export interface CreateIssueRequest {
  title: string;
  description: string;
  location: string;
  attachmentIds?: string[];
}

export interface UpdateIssueStatusRequest {
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED' | 'ON_HOLD';
  staffComment: string;
}

export interface IssueResponse {
  id: string;
  title: string;
  description: string;
  location: string;
  creatorUserId: string;
  assignedStaffId?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED' | 'ON_HOLD';
  staffComment: string;
  attachmentUrls?: string[];
  createdAt: string;
  updatedAt: string;
  locked?: boolean;
}

export interface PagedResponse<T> {
    content: T[];
    currentPage: number;
    totalPages: number;
    totalItems: number;
}

