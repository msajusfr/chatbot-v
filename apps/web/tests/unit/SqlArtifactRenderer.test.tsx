import { render, screen } from '@testing-library/react';
import { SqlArtifactRenderer } from '@/components/SqlArtifactRenderer';

it('renders sql artifact with fictional disclaimer', () => {
  render(
    <SqlArtifactRenderer
      artifact={{
        type: 'sql.query',
        title: 'Top produits',
        sql: 'select * from sales',
        result: { columns: ['name', 'revenue'], rows: [['A', 100]], isFictional: true },
        presentationHints: { primary: 'bar' }
      }}
    />
  );

  expect(screen.getByText('Top produits')).toBeInTheDocument();
  expect(screen.getByText(/fictives/i)).toBeInTheDocument();
});


it('renders pie visualization for sql-like categorical result', () => {
  render(
    <SqlArtifactRenderer
      artifact={{
        type: 'sql.query',
        title: 'Répartition',
        sql: 'select category, value from mock_result',
        result: { columns: ['category', 'value'], rows: [['A', 10], ['B', 20], ['C', 30]], isFictional: true },
        presentationHints: { primary: 'pie' }
      }}
    />
  );

  expect(screen.getByLabelText('Pie chart')).toBeInTheDocument();
  expect(screen.getByText('A: 10')).toBeInTheDocument();
  expect(screen.getByText('B: 20')).toBeInTheDocument();
  expect(screen.getByText('C: 30')).toBeInTheDocument();
});
