import { VolunteerRegistrationRequest } from "./generated-open-api/models/VolunteerRegistrationRequest";

export interface PhoneInputScreenProps {
  registerVolunteer: (
    phoneNumber: string,
    password: string,
    hospitalUuids: string[]
  ) => Promise<void>;
  loading: boolean;
}

export interface SignUpProps {
  onComplete?: (data: VolunteerRegistrationRequest) => void;
}
