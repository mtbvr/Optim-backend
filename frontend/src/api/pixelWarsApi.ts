import { httpClient } from "./httpClient";
import type { Board, PerkCatalog, PerkPurchaseResult, PerkType, PlacePixelResult } from "../types/pixelWars";

export const pixelWarsApi = {
  async getBoard(): Promise<Board> {
    const { data } = await httpClient.get<Board>("/pixels");
    return data;
  },

  async placePixel(x: number, y: number, usePerk?: PerkType): Promise<PlacePixelResult> {
    const { data } = await httpClient.post<PlacePixelResult>("/pixels", { x, y, usePerk });
    return data;
  },

  async getPerks(): Promise<PerkCatalog> {
    const { data } = await httpClient.get<PerkCatalog>("/perks");
    return data;
  },

  async purchasePerk(type: PerkType): Promise<PerkPurchaseResult> {
    const { data } = await httpClient.post<PerkPurchaseResult>(`/perks/${type}/purchase`);
    return data;
  },
};
