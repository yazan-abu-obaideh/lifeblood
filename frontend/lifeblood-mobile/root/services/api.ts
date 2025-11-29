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
  const response = await axios.get(
    `${config.apiBaseUrl}/api/v1/alert?${params}`
  );
  return response.data;
};

export const fetchUserDetails = async (
  user: UserContextType
): Promise<VolunteerResponse> => {
  const token = await user.getUserToken();
  const response = await axios.get(
    `${config.apiBaseUrl}${config.endpoints.volunteer.replace(
      "{uuid}",
      user.userUuid!
    )}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};

export const login = async (
  phoneNumber: string,
  password: string
): Promise<LoginResponse> => {
  const response = await axios.post(
    `${config.apiBaseUrl}/api/v1/auth/login`,
    {
      phoneNumber: phoneNumber.trim(),
      password: password.trim(),
    },
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  return response.data;
};

export const fetchRefreshToken = async (): Promise<string> => {
  const response = await axios.post(
    `${config.apiBaseUrl}/api/v1/auth/refresh`,
    {
      phoneNumber: await getFromAsyncStorage("PHONE_NUMBER"),
      refreshToken: await getFromAsyncStorage("REFRESH_TOKEN"),
    },
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  if (response.status !== 200) {
    console.error(`Auth token fetching failed ${response}`);
  }

  return response.data;
};

export const getHospitals = async (): Promise<HospitalResponse[]> => {
  const url = `${config.apiBaseUrl}${config.endpoints.hospitals}`;

  console.log("[API] Fetching hospitals");

  try {
    const response = await axios.get(url, {
      headers: {
        "Content-Type": "application/json",
      },
    });

    console.log("[API] Get hospitals response status:", response.status);

    const data = response.data;
    console.log("[API] Get hospitals success:", data);

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    if (axios.isAxiosError(error) && error.response) {
      const errorData = error.response.data || {};
      const errorMessage =
        errorData.message || `HTTP error! status: ${error.response.status}`;

      console.error("[API] Get hospitals failed:", {
        status: error.response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, error.response.status, errorData);
    }

    console.error("[API] Network error fetching hospitals:", error);

    throw new ApiError(
      "Network error. Please check your connection and try again.",
      undefined,
      error
    );
  }
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
  const url = `${config.apiBaseUrl}${config.endpoints.registerVolunteer}`;

  console.log("[API] Sending verification code to:", phoneNumber);

  try {
    const response = await axios.post(
      url,
      {
        phoneNumber,
        password,
        selectedHospitals: hospitalUuids,
      },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    console.log("[API] Send code response status:", response.status);

    const data = response.data;
    console.log("[API] Send code success:", data);

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    if (axios.isAxiosError(error) && error.response) {
      const errorData = error.response.data || {};
      const errorMessage =
        errorData.message || `HTTP error! status: ${error.response.status}`;

      console.error("[API] Send code failed:", {
        status: error.response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, error.response.status, errorData);
    }

    console.error("[API] Network error sending code:", error);

    throw new ApiError(
      "Network error. Please check your connection and try again.",
      undefined,
      error
    );
  }
};

export const verifyCode = async (
  phoneNumber: string,
  verificationCode: string
): Promise<void> => {
  const url = `${config.apiBaseUrl}${config.endpoints.verifyCode}`;

  console.log("[API] Verifying code for:", phoneNumber);

  try {
    const response = await axios.post(
      url,
      { phoneNumber, verificationCode },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    console.log("[API] Verify code response status:", response.status);

    console.log("[API] Verify code success:");

    return;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    if (axios.isAxiosError(error) && error.response) {
      const errorData = error.response.data || {};
      const errorMessage =
        errorData.message || `HTTP error! status: ${error.response.status}`;

      console.error("[API] Verify code failed:", {
        status: error.response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, error.response.status, errorData);
    }

    console.error("[API] Network error verifying code:", error);

    throw new ApiError(
      "Network error. Please check your connection and try again.",
      undefined,
      error
    );
  }
};
