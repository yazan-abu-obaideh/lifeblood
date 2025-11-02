const ENV = "development";

const BASE_URLS: Record<string, string> = {
  development: "http://10.0.2.2:8050",
  staging: "https://staging-api.example.com",
  production: "https://api.example.com",
};

export const endpoints = {
  registerVolunteer: "/api/v1/volunteer",
  verifyCode: "/api/v1/volunteer/verify-phone-number",
  hospitals: "/api/v1/hospital",
} as const;

export const config = {
  apiBaseUrl: BASE_URLS[ENV],
  endpoints,
};
