import { httpClient } from "./httpClient";
import type { Profile } from "../types/pixelWars";

export const profileApi = {
  async getProfile(): Promise<Profile> {
    const { data } = await httpClient.get<Profile>("/profile");
    return data;
  },
};
