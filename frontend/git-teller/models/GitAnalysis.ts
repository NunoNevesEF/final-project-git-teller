import { CommitAnalysis } from "./CommitAnalysis";
import { ModifiedFile } from "./ModifiedFile";
import { SearchInfo } from "./SearchInfo";

export interface GitAnalysis {
  searchInfo: SearchInfo;
  commitsByUser: Record<string, CommitAnalysis[]>;
  commitsByBranch: Record<string, CommitAnalysis[]>;
  mostModifiedFiles: ModifiedFile[];
  firstCommitTime: string;
  lastCommitTime: string;
  llmAnalysis: string;
}