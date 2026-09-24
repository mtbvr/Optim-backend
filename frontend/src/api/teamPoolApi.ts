import { httpClient } from "./httpClient";
import type { ContributeResult } from "../types/pixelWars";

export const teamPoolApi = {
  async contribute(amount: number): Promise<ContributeResult> {
    const { data } = await httpClient.post<ContributeResult>("/team-pool/contribute", { amount });
    return data;
  },
};
