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
    primary: 'bar' | 'scatter' | 'line' | 'kpi' | 'table';
    secondary?: 'bar' | 'scatter' | 'line' | 'kpi' | 'table';
    formatting?: string[];
  };
};
