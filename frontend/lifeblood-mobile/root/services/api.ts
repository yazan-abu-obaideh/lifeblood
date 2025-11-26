import { config } from "../config/config";
import {
  HospitalResponse,
  PageAlertResponse,
} from "../generated-open-api/models/all";

interface ApiResponse {
  name: string;
}

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
  const response = await fetch(`${config.apiBaseUrl}/api/v1/alert?${params}`);
  return response.json();
};

export const getHospitals = async (): Promise<HospitalResponse[]> => {
  const url = `${config.apiBaseUrl}${config.endpoints.hospitals}`;

  console.log("[API] Fetching hospitals");

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    console.log("[API] Get hospitals response status:", response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const errorMessage =
        errorData.message || `HTTP error! status: ${response.status}`;

      console.error("[API] Get hospitals failed:", {
        status: response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, response.status, errorData);
    }

    const data = await response.json();
    console.log("[API] Get hospitals success:", data);

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
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
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ phoneNumber, password, selectedHospitals: hospitalUuids }),
    });

    console.log("[API] Send code response status:", response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const errorMessage =
        errorData.message || `HTTP error! status: ${response.status}`;

      console.error("[API] Send code failed:", {
        status: response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, response.status, errorData);
    }

    const data = await response.json();
    console.log("[API] Send code success:", data);

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
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
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ phoneNumber, verificationCode }),
    });

    console.log("[API] Verify code response status:", response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const errorMessage =
        errorData.message || `HTTP error! status: ${response.status}`;

      console.error("[API] Verify code failed:", {
        status: response.status,
        error: errorMessage,
        data: errorData,
      });

      throw new ApiError(errorMessage, response.status, errorData);
    }

    console.log("[API] Verify code success:");

    return;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    console.error("[API] Network error verifying code:", error);

    throw new ApiError(
      "Network error. Please check your connection and try again.",
      undefined,
      error
    );
  }
};
