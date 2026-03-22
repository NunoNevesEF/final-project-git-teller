export interface CommitDTO {
  id: string;
  name: string;
  author: string;
  parentCount: number;
  timestamp: string;
  message: string;
}
