export type AnalysisType =
    | 'DEFAULT' | 'SECURITY' | 'QUALITY' | 'TESTS' | 'PERFORMANCE' | 'ARCHITECTURE';

export const ANALYSIS_TYPES: { value: AnalysisType; label: string; description: string }[] = [
    { value: 'DEFAULT',      label: 'Default',      description: 'General review of scope, impact, and top regression risks.' },
    { value: 'SECURITY',     label: 'Security',     description: 'Vulnerabilities, unsafe patterns, and exposed secrets.' },
    { value: 'QUALITY',      label: 'Quality',      description: 'Code smells, maintainability issues, and readability.' },
    { value: 'TESTS',        label: 'Tests',        description: 'Missing coverage, confidence gaps, and test suggestions.' },
    { value: 'PERFORMANCE',  label: 'Performance',  description: 'Inefficiencies, bottlenecks, and complexity introduced.' },
    { value: 'ARCHITECTURE', label: 'Architecture', description: 'Coupling, layer violations, and modularity concerns.' },
];
