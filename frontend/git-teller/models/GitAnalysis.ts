import { CommitDTO } from "./CommitDTO";

export interface GitAnalysis {
  commitsByUser: Record<string, CommitDTO[]>;
  commitsByBranch: Record<string, CommitDTO[]>;
  firstCommitTime: string;
  lastCommitTime: string;
}
