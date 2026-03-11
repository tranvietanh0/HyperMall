import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  formatCurrency,
  formatNumber,
  formatDate,
  formatDateTime,
  formatRelativeTime,
  truncateText,
  formatFileSize,
  formatPhoneNumber,
  calculateDiscount,
} from './format';

describe('format utilities', () => {
  describe('formatCurrency', () => {
    it('should format number as VND currency', () => {
      const result = formatCurrency(1000000);
      expect(result).toContain('1.000.000');
      // VND can be displayed as 'VND' or '₫' depending on locale
      expect(result.includes('VND') || result.includes('₫')).toBe(true);
    });

    it('should handle zero', () => {
      const result = formatCurrency(0);
      expect(result).toContain('0');
    });

    it('should handle negative numbers', () => {
      const result = formatCurrency(-500000);
      expect(result).toContain('500.000');
    });
  });

  describe('formatNumber', () => {
    it('should format number with thousand separators', () => {
      expect(formatNumber(1234567)).toBe('1.234.567');
    });

    it('should handle zero', () => {
      expect(formatNumber(0)).toBe('0');
    });

    it('should handle decimal numbers', () => {
      const result = formatNumber(1234.56);
      expect(result).toContain('1.234');
    });
  });

  describe('formatDate', () => {
    it('should format date string to Vietnamese format', () => {
      const result = formatDate('2026-03-11');
      expect(result).toMatch(/11\/03\/2026|11-03-2026/);
    });

    it('should format Date object', () => {
      const date = new Date(2026, 2, 11); // March 11, 2026
      const result = formatDate(date);
      expect(result).toMatch(/11\/03\/2026|11-03-2026/);
    });
  });

  describe('formatDateTime', () => {
    it('should format date and time', () => {
      const result = formatDateTime('2026-03-11T14:30:00');
      expect(result).toContain('11');
      expect(result).toContain('03');
      expect(result).toContain('2026');
    });
  });

  describe('formatRelativeTime', () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2026-03-11T12:00:00'));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('should return "Vừa xong" for recent times', () => {
      const date = new Date('2026-03-11T11:59:30');
      expect(formatRelativeTime(date)).toBe('Vừa xong');
    });

    it('should return minutes ago', () => {
      const date = new Date('2026-03-11T11:30:00');
      expect(formatRelativeTime(date)).toBe('30 phút trước');
    });

    it('should return hours ago', () => {
      const date = new Date('2026-03-11T09:00:00');
      expect(formatRelativeTime(date)).toBe('3 giờ trước');
    });

    it('should return days ago', () => {
      const date = new Date('2026-03-09T12:00:00');
      expect(formatRelativeTime(date)).toBe('2 ngày trước');
    });

    it('should return months ago', () => {
      const date = new Date('2025-12-11T12:00:00');
      expect(formatRelativeTime(date)).toBe('3 tháng trước');
    });

    it('should return years ago', () => {
      const date = new Date('2024-03-11T12:00:00');
      expect(formatRelativeTime(date)).toBe('2 năm trước');
    });
  });

  describe('truncateText', () => {
    it('should return original text if shorter than maxLength', () => {
      expect(truncateText('Hello', 10)).toBe('Hello');
    });

    it('should truncate text with ellipsis', () => {
      expect(truncateText('Hello World', 8)).toBe('Hello...');
    });

    it('should handle exact length', () => {
      expect(truncateText('Hello', 5)).toBe('Hello');
    });

    it('should handle empty string', () => {
      expect(truncateText('', 10)).toBe('');
    });
  });

  describe('formatFileSize', () => {
    it('should return "0 Bytes" for zero', () => {
      expect(formatFileSize(0)).toBe('0 Bytes');
    });

    it('should format bytes', () => {
      expect(formatFileSize(500)).toBe('500 Bytes');
    });

    it('should format kilobytes', () => {
      expect(formatFileSize(1024)).toBe('1 KB');
    });

    it('should format megabytes', () => {
      expect(formatFileSize(1048576)).toBe('1 MB');
    });

    it('should format gigabytes', () => {
      expect(formatFileSize(1073741824)).toBe('1 GB');
    });

    it('should handle decimal values', () => {
      expect(formatFileSize(1536)).toBe('1.5 KB');
    });
  });

  describe('formatPhoneNumber', () => {
    it('should format 10-digit Vietnamese phone number', () => {
      expect(formatPhoneNumber('0987654321')).toBe('0987 654 321');
    });

    it('should return original for non-10-digit numbers', () => {
      expect(formatPhoneNumber('123456')).toBe('123456');
    });

    it('should handle numbers with non-digit characters', () => {
      expect(formatPhoneNumber('098-765-4321')).toBe('0987 654 321');
    });
  });

  describe('calculateDiscount', () => {
    it('should calculate discount percentage correctly', () => {
      expect(calculateDiscount(100000, 80000)).toBe(20);
    });

    it('should return 0 for invalid original price', () => {
      expect(calculateDiscount(0, 50000)).toBe(0);
      expect(calculateDiscount(-100, 50000)).toBe(0);
    });

    it('should return 0 if sale price >= original price', () => {
      expect(calculateDiscount(100000, 100000)).toBe(0);
      expect(calculateDiscount(100000, 120000)).toBe(0);
    });

    it('should round to nearest integer', () => {
      expect(calculateDiscount(100000, 66666)).toBe(33);
    });
  });
});
