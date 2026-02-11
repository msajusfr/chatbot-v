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
