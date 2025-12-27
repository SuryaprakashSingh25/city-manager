# Issue Service

The **Issue Service** manages city-level issues reported by citizens and handled by staff.  
It provides **role-based endpoints** for creating, submitting, viewing, and updating issues.

---


## 1. Overview

- Service Name: `issue-service`  
- Base URL: `/api/issues`  
- Handles issues lifecycle:
  - Citizens create and submit issues
  - Staff accept, assign, and update issues
- Supports:
  - Pagination
  - Sorting
  - Filtering by status
  - Distributed locks to avoid race conditions

---

## 2. Responsibilities

- Citizens can:
  - Create issues
  - Submit drafted issues
  - View their own issues
- Staff can:
  - View open and assigned issues
  - Accept issues
  - Update issue status with comments
  - Manage distributed locks
- Enforces **role-based access** using Spring Security
- Supports **paginated and filtered responses**

---

## 3. Security & Roles

| Role    | Allowed Actions |
|--------|----------------|
| CITIZEN | Create, submit, view, and list their own issues |
| STAFF   | View open/assigned issues, accept issues, update status, manage locks |

All endpoints are secured via `@PreAuthorize`.

---

## 4. API Endpoints

### Citizen Endpoints

| Endpoint | Method | Description |
|---------|--------|------------|
| `/api/issues` | POST | Create a new issue |
| `/api/issues/{id}/submit` | PUT | Submit a drafted issue |
| `/api/issues/my` | GET | List all issues created by the citizen (paginated) |
| `/api/issues/my/{id}` | GET | View a single issue of the citizen |

### Staff Endpoints

| Endpoint | Method | Description |
|---------|--------|------------|
| `/api/issues/open` | GET | List all open issues (paginated) |
| `/api/issues/open/{id}` | GET | View details of an open issue |
| `/api/issues/assigned` | GET | List assigned issues (paginated) |
| `/api/issues/assigned/{id}` | GET | View assigned issue details |
| `/api/issues/{id}/accept` | PUT | Accept an open issue |
| `/api/issues/{id}/status` | PUT | Update issue status and add staff comments |
| `/api/issues/open/locks` | GET | Check distributed locks for open issues |
| `/api/issues/open/{id}/lock` | DELETE | Release the lock on an open issue |

---

## 5. Issue Locks

- Distributed locking prevents multiple staff members from working on the same issue simultaneously
- Locks can be:
  - Checked via `/open/locks`
  - Released via `/open/{id}/lock`
- Ensures **data consistency** during concurrent access

---

## 6. How It Works

1. **Citizen Flow**
   - Create issue → Draft status
   - Submit issue → Open status
   - View their own issues with pagination and filtering

2. **Staff Flow**
   - View open issues
   - Accept an issue (lock it for work)
   - Update issue status and optionally add comments
   - View assigned issues
   - Check or release locks

3. **Pagination & Sorting**
   - All listing endpoints support `page`, `size`, `sortBy`, and `direction` parameters
   - Filter by issue `status` where applicable

4. **Security**
   - Citizens can only access their own issues
   - Staff can access all open or assigned issues
  
