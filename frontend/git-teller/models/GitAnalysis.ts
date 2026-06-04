import { CommitDTO } from "./CommitDTO";
import { ModifiedFile } from "./ModifiedFile";
import { SearchInfo } from "./SearchInfo";

export interface GitAnalysis {
  searchInfo: SearchInfo;
  commitsByUser: Record<string, CommitDTO[]>;
  commitsByBranch: Record<string, CommitDTO[]>;
  mostModifiedFiles: ModifiedFile[];
  firstCommitTime: string;
  lastCommitTime: string;
}
