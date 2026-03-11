import { describe, it, expect } from 'vitest';
import {
  emailSchema,
  passwordSchema,
  phoneSchema,
  loginSchema,
  registerSchema,
  isValidEmail,
  isValidVietnamesePhone,
} from './validation';

describe('validation utilities', () => {
  describe('emailSchema', () => {
    it('should accept valid email', async () => {
      await expect(emailSchema.validate('test@example.com')).resolves.toBe(
        'test@example.com'
      );
    });

    it('should reject invalid email', async () => {
      await expect(emailSchema.validate('invalid-email')).rejects.toThrow();
    });

    it('should reject empty email', async () => {
      await expect(emailSchema.validate('')).rejects.toThrow();
    });
  });

  describe('passwordSchema', () => {
    it('should accept valid password', async () => {
      await expect(passwordSchema.validate('Password1@')).resolves.toBe(
        'Password1@'
      );
    });

    it('should reject password shorter than 8 characters', async () => {
      await expect(passwordSchema.validate('Pass1@')).rejects.toThrow(
        'ít nhất 8 ký tự'
      );
    });

    it('should reject password without lowercase', async () => {
      await expect(passwordSchema.validate('PASSWORD1@')).rejects.toThrow(
        'chữ thường'
      );
    });

    it('should reject password without uppercase', async () => {
      await expect(passwordSchema.validate('password1@')).rejects.toThrow(
        'chữ hoa'
      );
    });

    it('should reject password without number', async () => {
      await expect(passwordSchema.validate('Password@')).rejects.toThrow('số');
    });

    it('should reject password without special character', async () => {
      await expect(passwordSchema.validate('Password1')).rejects.toThrow(
        'ký tự đặc biệt'
      );
    });
  });

  describe('phoneSchema', () => {
    it('should accept valid Vietnamese phone starting with 0', async () => {
      await expect(phoneSchema.validate('0987654321')).resolves.toBe(
        '0987654321'
      );
    });

    it('should accept valid Vietnamese phone starting with +84', async () => {
      await expect(phoneSchema.validate('+84987654321')).resolves.toBe(
        '+84987654321'
      );
    });

    it('should reject invalid phone number', async () => {
      await expect(phoneSchema.validate('1234567890')).rejects.toThrow();
    });

    it('should reject phone with wrong length', async () => {
      await expect(phoneSchema.validate('098765432')).rejects.toThrow();
    });
  });

  describe('loginSchema', () => {
    it('should accept valid login data', async () => {
      const data = { email: 'test@example.com', password: 'password123' };
      await expect(loginSchema.validate(data)).resolves.toEqual(data);
    });

    it('should reject missing email', async () => {
      await expect(
        loginSchema.validate({ password: 'password123' })
      ).rejects.toThrow();
    });

    it('should reject missing password', async () => {
      await expect(
        loginSchema.validate({ email: 'test@example.com' })
      ).rejects.toThrow();
    });
  });

  describe('registerSchema', () => {
    const validData = {
      email: 'test@example.com',
      password: 'Password1@',
      confirmPassword: 'Password1@',
      fullName: 'John Doe',
    };

    it('should accept valid registration data', async () => {
      await expect(registerSchema.validate(validData)).resolves.toBeDefined();
    });

    it('should reject mismatched passwords', async () => {
      const data = { ...validData, confirmPassword: 'Different1@' };
      await expect(registerSchema.validate(data)).rejects.toThrow('không khớp');
    });

    it('should reject short full name', async () => {
      const data = { ...validData, fullName: 'A' };
      await expect(registerSchema.validate(data)).rejects.toThrow(
        'ít nhất 2 ký tự'
      );
    });

    it('should accept optional phone', async () => {
      await expect(registerSchema.validate(validData)).resolves.toBeDefined();
    });

    it('should validate phone if provided', async () => {
      const dataWithPhone = { ...validData, phone: '0987654321' };
      await expect(
        registerSchema.validate(dataWithPhone)
      ).resolves.toBeDefined();
    });
  });

  describe('isValidEmail', () => {
    it('should return true for valid email', () => {
      expect(isValidEmail('test@example.com')).toBe(true);
      expect(isValidEmail('user.name@domain.co')).toBe(true);
      expect(isValidEmail('user+tag@example.org')).toBe(true);
    });

    it('should return false for invalid email', () => {
      expect(isValidEmail('invalid')).toBe(false);
      expect(isValidEmail('invalid@')).toBe(false);
      expect(isValidEmail('@domain.com')).toBe(false);
      expect(isValidEmail('')).toBe(false);
    });
  });

  describe('isValidVietnamesePhone', () => {
    it('should return true for valid Vietnamese phone numbers', () => {
      expect(isValidVietnamesePhone('0987654321')).toBe(true);
      expect(isValidVietnamesePhone('0312345678')).toBe(true);
      expect(isValidVietnamesePhone('0512345678')).toBe(true);
      expect(isValidVietnamesePhone('0712345678')).toBe(true);
      expect(isValidVietnamesePhone('0812345678')).toBe(true);
      expect(isValidVietnamesePhone('+84987654321')).toBe(true);
    });

    it('should return false for invalid phone numbers', () => {
      expect(isValidVietnamesePhone('0123456789')).toBe(false); // Invalid prefix
      expect(isValidVietnamesePhone('098765432')).toBe(false); // Too short
      expect(isValidVietnamesePhone('09876543210')).toBe(false); // Too long
      expect(isValidVietnamesePhone('1234567890')).toBe(false); // Invalid start
      expect(isValidVietnamesePhone('')).toBe(false);
    });
  });
});
