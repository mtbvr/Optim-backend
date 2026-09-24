import axios from "axios";
import type { ApiError } from "../types/user";
import i18n from "../i18n";

// Le JWT vit dans un cookie httpOnly : withCredentials permet au navigateur
// de l'envoyer/le recevoir automatiquement, sans jamais exposer le token au JS.
export const httpClient = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

/**
 * Traduit l'erreur via son `code` (cle `errors.<code>`, avec interpolation des `params`) : le
 * backend ne renvoie plus que du francais dans `message`, qui ne sert que de repli si le code
 * est absent/inconnu. `fallbackKey` est une cle de traduction (pas du texte en dur).
 */
export function extractErrorMessage(error: unknown, fallbackKey: string): string {
  if (axios.isAxiosError<ApiError>(error) && error.response?.data) {
    const data = error.response.data;
    if (data.code) {
      const translated = i18n.t(`errors.${data.code}`, { ...data.params, defaultValue: "" });
      if (translated) return translated;
    }
    if (data.message) return data.message;
  }
  return i18n.t(fallbackKey);
}
