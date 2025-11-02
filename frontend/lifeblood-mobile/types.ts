export interface SignUpData {
  phoneNumber: string;
  verified: boolean;
  token?: string;
  selectedHospitalIds: string[];
}

export interface PhoneInputScreenProps {
  sendVerificationCode: (
    phoneNumber: string,
    hospitalUuids: string[]
  ) => Promise<void>;
  loading: boolean;
}

export interface VerificationScreenProps {
  phoneNumber: string;
  verifyCode: (code: string) => Promise<void>;
  goBack: () => void;
  loading: boolean;
}

export interface SignUpProps {
  onComplete?: (data: SignUpData) => void;
}
