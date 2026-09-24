import { httpClient } from "./httpClient";
import type { GlobalStats } from "../types/pixelWars";

export const statsApi = {
  async getGlobalStats(): Promise<GlobalStats> {
    const { data } = await httpClient.get<GlobalStats>("/stats/global");
    return data;
  },
};
