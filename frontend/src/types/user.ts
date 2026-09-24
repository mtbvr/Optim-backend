import type { Team } from "./pixelWars";

export interface User {
  id: string;
  email: string;
  fullName: string;
  team: Team;
  createdAt: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface SignupPayload {
  email: string;
  password: string;
  fullName: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  code?: string;
  params?: Record<string, string | number>;
}
