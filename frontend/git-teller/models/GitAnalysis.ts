import { CommitDTO } from "./CommitDTO";
import { ModifiedFile } from "./ModifiedFile";

export interface GitAnalysis {
  commitsByUser: Record<string, CommitDTO[]>;
  commitsByBranch: Record<string, CommitDTO[]>;
  mostModifiedFiles: ModifiedFile[];
  firstCommitTime: string;
  lastCommitTime: string;
}
