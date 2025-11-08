import { VolunteerRegistrationRequest } from "./generated-open-api/models/VolunteerRegistrationRequest";

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
  onComplete?: (data: VolunteerRegistrationRequest) => void;
}
