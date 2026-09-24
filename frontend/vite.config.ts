import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// En dev, le backend tourne sur :8080 ; on proxifie /api pour reproduire
// le comportement "meme origine" de nginx en production (cookies, pas de CORS).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/ws": {
        target: "ws://localhost:8080",
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
