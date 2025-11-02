export interface ValidationResult {
  valid: boolean;
  error?: string;
}

export const validatePhoneNumber = (phoneNumber: string): ValidationResult => {
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

export const validateVerificationCode = (code: string): ValidationResult => {
  if (!code || code.trim() === "") {
    return { valid: false, error: "Verification code is required" };
  }

  return { valid: true };
};
