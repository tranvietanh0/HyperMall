import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import HomePage from './index';

describe('HomePage', () => {
  it('renders the premium hero and key commerce sections', () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    );

    expect(
      screen.getByRole('heading', {
        name: /experience precision commerce./i,
      })
    ).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /explore by category/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /flash sale/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /shop collection/i })).toBeInTheDocument();
  });
});
