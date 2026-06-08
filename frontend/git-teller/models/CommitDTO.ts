export interface CommitDTO {
  id: string;
  name: string;
  author: string;
  parentCount: number;
  timestamp: number;
  message: string;
  additions: number;
  deletions: number;
}
