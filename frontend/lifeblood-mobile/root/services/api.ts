import { config } from "../config/config";
import {
  HospitalResponse,
  LoginResponse,
  PageAlertResponse,
  VolunteerResponse,
} from "../generated-open-api/models/all";
import { UserContextType } from "../Screens/UserContext";
import { getFromAsyncStorage } from "../utils/asyncStorageUtils";
import { apiClient } from "./apiClientConfig";

export const getAlerts = async (
  params: URLSearchParams
): Promise<PageAlertResponse> => {
  const response = await apiClient.get("/api/v1/alert", {
    params: params,
    timeout: 15000, // 15 second timeout for alerts
  });
  return response.data;
};

export const fetchUserDetails = async (
  user: UserContextType
): Promise<VolunteerResponse> => {
  const token = await user.getUserToken();
  const endpoint = config.endpoints.volunteer.replace("{uuid}", user.userUuid!);
  const response = await apiClient.get(endpoint, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
};

export const login = async (
  phoneNumber: string,
  password: string
): Promise<LoginResponse> => {
  const response = await apiClient.post("/api/v1/auth/login", {
    phoneNumber: phoneNumber.trim(),
    password: password.trim(),
  });

  return response.data;
};

export const fetchRefreshToken = async (): Promise<string> => {
  const response = await apiClient.post("/api/v1/auth/refresh", {
    phoneNumber: await getFromAsyncStorage("PHONE_NUMBER"),
    refreshToken: await getFromAsyncStorage("REFRESH_TOKEN"),
  });

  return response.data;
};

export const getHospitals = async (): Promise<HospitalResponse[]> => {
  const response = await apiClient.get(config.endpoints.hospitals);
  return response.data;
};

interface SendVerificationCodeResponse {
  success: boolean;
  message?: string;
}

export const registerVolunteer = async (
  phoneNumber: string,
  password: string,
  hospitalUuids: string[]
): Promise<SendVerificationCodeResponse> => {
  const response = await apiClient.post(config.endpoints.registerVolunteer, {
    phoneNumber,
    password,
    selectedHospitals: hospitalUuids,
  });

  return response.data;
};

export const verifyCode = async (
  phoneNumber: string,
  verificationCode: string
): Promise<void> => {
  await apiClient.post(config.endpoints.verifyCode, {
    phoneNumber,
    verificationCode,
  });
};
