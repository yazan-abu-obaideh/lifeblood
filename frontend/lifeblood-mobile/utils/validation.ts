/**
 * Validation utilities
 */

export interface ValidationResult {
  valid: boolean;
  error?: string;
}

/**
 * Validate phone number
 * TODO: Replace with generated validation from OpenAPI spec
 */
export const validatePhoneNumber = (phoneNumber: string): ValidationResult => {
  // Stub validation - replace with generated code
  if (!phoneNumber || phoneNumber.trim() === "") {
    return { valid: false, error: "Phone number is required" };
  }

  if (phoneNumber.length < 10) {
    return { valid: false, error: "Phone number must be at least 10 digits" };
  }

  if (phoneNumber.length > 15) {
    return { valid: false, error: "Phone number must be at most 15 digits" };
  }

  return { valid: true };
};

/**
 * Validate verification code
 * TODO: Replace with generated validation from OpenAPI spec
 */
export const validateVerificationCode = (code: string): ValidationResult => {
  // Stub validation - replace with generated code
  if (!code || code.trim() === "") {
    return { valid: false, error: "Verification code is required" };
  }

  return { valid: true };
};
