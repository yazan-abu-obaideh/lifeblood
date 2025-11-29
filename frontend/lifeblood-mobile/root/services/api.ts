import axios from "axios";
import { config } from "../config/config";
import {
  HospitalResponse,
  LoginResponse,
  PageAlertResponse,
  VolunteerResponse,
} from "../generated-open-api/models/all";
import { UserContextType } from "../Screens/UserContext";
import { getFromAsyncStorage } from "../utils/asyncStorageUtils";

const apiClient = axios.create({
  baseURL: config.apiBaseUrl,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  console.debug(`[API] ${config.method?.toUpperCase()} ${config.url}`);
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    console.debug(`[API] ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    if (!axios.isAxiosError(error)) {
      console.error("[API] Unknown error:", error);
      throw new ApiError(
        "Something went wrong. Please check your internet connection.",
        undefined,
        error
      );
    }

    if (!error.response) {
      console.error("[API] Network error:", error);
      throw new ApiError(
        "Something went wrong. Please check your internet connection.",
        undefined,
        error
      );
    }

    const { status, data } = error.response;
    const errorData = data || {};

    if (status >= 400 && status < 500) {
      const message =
        errorData.message || errorData.error || "An error occurred";

      if (status === 401) {
        console.warn("[API] Unauthorized:", {
          status,
          message,
          data: errorData,
        });
        throw new ApiError(message, status, errorData);
      }

      if (status === 404) {
        console.warn("[API] Not found:", { status, message, data: errorData });
        throw new ApiError(
          errorData.message || "Resource not found",
          status,
          errorData
        );
      }

      if (status === 408) {
        console.warn("[API] Request timeout:", {
          status,
          message,
          data: errorData,
        });
        throw new ApiError(
          "Request timed out. Please check your internet connection.",
          status,
          errorData
        );
      }

      if (status === 429) {
        console.warn("[API] Too many requests:", {
          status,
          message,
          data: errorData,
        });
        throw new ApiError(
          "Too many requests. Please slow down and try again.",
          status,
          errorData
        );
      }

      console.warn("[API] Client error:", { status, message, data: errorData });
      throw new ApiError(message, status, errorData);
    }

    if (status >= 500) {
      if (status === 503) {
        console.error("[API] Service unavailable:", {
          status,
          data: errorData,
        });
        throw new ApiError(
          "Service temporarily unavailable. Please try again later.",
          status,
          errorData
        );
      }

      console.error("[API] Server error:", { status, data: errorData });
      throw new ApiError(
        "An unknown error occurred. Please try again later or contact us.",
        status,
        errorData
      );
    }

    console.error("[API] Unexpected error:", { status, data: errorData });
    throw new ApiError(
      "An unknown error occurred. Please try again later or contact us.",
      status,
      errorData
    );
  }
);

export class ApiError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public response?: any
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export const getAlerts = async (
  params: URLSearchParams
): Promise<PageAlertResponse> => {
  const response = await apiClient.get("/api/v1/alert", {
    params: params,
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
