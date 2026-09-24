import { httpClient } from "./httpClient";
import type { PlayerState, SkinCatalog, SkinId, SkinPurchaseResult } from "../types/pixelWars";

export const skinsApi = {
  async getSkins(): Promise<SkinCatalog> {
    const { data } = await httpClient.get<SkinCatalog>("/skins");
    return data;
  },

  async purchaseSkin(skinId: SkinId): Promise<SkinPurchaseResult> {
    const { data } = await httpClient.post<SkinPurchaseResult>(`/skins/${skinId}/purchase`);
    return data;
  },

  async selectSkin(skinId: SkinId | null): Promise<PlayerState> {
    const { data } = await httpClient.post<PlayerState>("/skins/select", { skinId });
    return data;
  },
};
