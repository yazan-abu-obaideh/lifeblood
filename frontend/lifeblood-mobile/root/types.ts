export interface RegistrationScreenProps {
  registerVolunteer: (
    phoneNumber: string,
    password: string,
    hospitalUuids: string[]
  ) => Promise<void>;
  loading: boolean;
}
