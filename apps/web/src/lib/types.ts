export type SqlArtifact = {
  type: 'sql.query';
  title: string;
  sql: string;
  result: {
    columns: string[];
    rows: Array<Array<string | number>>;
    isFictional: boolean;
    notes?: string;
  };
  presentationHints: {
    primary: 'bar' | 'scatter' | 'line' | 'pie' | 'kpi' | 'table';
    secondary?: 'bar' | 'scatter' | 'line' | 'pie' | 'kpi' | 'table';
    formatting?: string[];
  };
};
