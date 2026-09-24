import { httpClient } from "./httpClient";

export const emotesApi = {
  async send(emoji: string): Promise<void> {
    await httpClient.post("/emotes", { emoji });
  },
};
