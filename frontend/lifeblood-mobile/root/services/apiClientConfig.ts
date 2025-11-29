import axiosRetry from "axios-retry";
import { config } from "../config/config";
import axios from "axios";

export const apiClient = axios.create({
  baseURL: config.apiBaseUrl,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000, // 10 seconds
});

axiosRetry(apiClient, {
  retries: 1,
  retryCondition: (error) => {
    // Only retry if there's no response (network error) or if it's not a 4xx/5xx status
    return !error.response || error.response.status < 400;
  },
  retryDelay: axiosRetry.exponentialDelay,
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
