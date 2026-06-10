export interface CommitAnalysis {
  id: string;
  name: string;
  author: string;
  parentCount: number;
  timestamp: number;
  message: string;
  additions: number;
  deletions: number;
}
