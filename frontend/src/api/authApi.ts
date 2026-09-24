import { httpClient } from "./httpClient";
import type { LoginPayload, SignupPayload, User } from "../types/user";

export const authApi = {
  async signup(payload: SignupPayload): Promise<User> {
    const { data } = await httpClient.post<User>("/auth/signup", payload);
    return data;
  },

  async login(payload: LoginPayload): Promise<User> {
    const { data } = await httpClient.post<User>("/auth/login", payload);
    return data;
  },

  async logout(): Promise<void> {
    await httpClient.post("/auth/logout");
  },

  async me(): Promise<User> {
    const { data } = await httpClient.get<User>("/auth/me");
    return data;
  },
};
