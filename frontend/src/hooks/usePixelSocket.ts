import { useEffect, useRef, useState } from "react";
import type {
  BonusZoneActiveEvent,
  BonusZoneClearedEvent,
  EmoteEvent,
  LeaderboardUpdateEvent,
  PixelWarsEvent,
  PixelsCapturedEvent,
  PixelsPlacedEvent,
  TeamPoolTriggeredEvent,
  TeamPoolUpdateEvent,
} from "../types/pixelWars";

const RECONNECT_DELAY_MS = 2000;

export interface PixelSocketHandlers {
  onPixelsPlaced: (event: PixelsPlacedEvent) => void;
  onPixelsCaptured: (event: PixelsCapturedEvent) => void;
  onLeaderboardUpdate: (event: LeaderboardUpdateEvent) => void;
  onBonusZoneActive: (event: BonusZoneActiveEvent) => void;
  onBonusZoneCleared: (event: BonusZoneClearedEvent) => void;
  onTeamPoolUpdate: (event: TeamPoolUpdateEvent) => void;
  onTeamPoolTriggered: (event: TeamPoolTriggeredEvent) => void;
  onEmote: (event: EmoteEvent) => void;
}

export function usePixelSocket(handlers: PixelSocketHandlers) {
  const handlersRef = useRef(handlers);
  handlersRef.current = handlers;
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    let socket: WebSocket | null = null;
    let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
    let cancelled = false;

    function connect() {
      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      socket = new WebSocket(`${protocol}//${window.location.host}/ws/pixels`);

      socket.onopen = () => setConnected(true);

      socket.onmessage = (message) => {
        const event = JSON.parse(message.data) as PixelWarsEvent;
        switch (event.type) {
          case "PIXELS_PLACED":
            handlersRef.current.onPixelsPlaced(event);
            break;
          case "PIXELS_CAPTURED":
            handlersRef.current.onPixelsCaptured(event);
            break;
          case "LEADERBOARD_UPDATE":
            handlersRef.current.onLeaderboardUpdate(event);
            break;
          case "BONUS_ZONE_ACTIVE":
            handlersRef.current.onBonusZoneActive(event);
            break;
          case "BONUS_ZONE_CLEARED":
            handlersRef.current.onBonusZoneCleared(event);
            break;
          case "TEAM_POOL_UPDATE":
            handlersRef.current.onTeamPoolUpdate(event);
            break;
          case "TEAM_POOL_TRIGGERED":
            handlersRef.current.onTeamPoolTriggered(event);
            break;
          case "EMOTE":
            handlersRef.current.onEmote(event);
            break;
        }
      };

      socket.onclose = () => {
        setConnected(false);
        if (!cancelled) {
          reconnectTimer = setTimeout(connect, RECONNECT_DELAY_MS);
        }
      };
    }

    connect();

    return () => {
      cancelled = true;
      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
      }
      socket?.close();
    };
  }, []);

  return { connected };
}
