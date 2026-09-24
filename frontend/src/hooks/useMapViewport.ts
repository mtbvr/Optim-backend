import { useCallback, useEffect, useRef, useState } from "react";

const MAX_ZOOM_FACTOR = 6;
const WHEEL_ZOOM_STEP = 1.15;

interface Viewport {
  zoom: number;
  panX: number;
  panY: number;
}

/**
 * Controle de carte classique : molette pour zoomer (centre sur le curseur), clic droit +
 * deplacement pour se deplacer (pan). Le contenu (canvas + overlay) doit etre place dans un
 * enfant avec `transform: translate(panX, panY) scale(zoom); transform-origin: 0 0`, a
 * l'interieur d'un conteneur `overflow: hidden` auquel `containerRef` est attache.
 */
export function useMapViewport(contentWidth: number, contentHeight: number) {
  const containerRef = useRef<HTMLDivElement>(null);
  const minZoomRef = useRef(1);
  const [viewport, setViewport] = useState<Viewport>({ zoom: 1, panX: 0, panY: 0 });
  const [isPanning, setIsPanning] = useState(false);
  const isPanningRef = useRef(false);
  const lastPointerRef = useRef({ x: 0, y: 0 });

  // Empeche de faire glisser la carte au-dela de ses bords : si le contenu (a l'echelle
  // actuelle) est plus grand que le conteneur sur un axe, borne le pan pour qu'il ne reste
  // jamais de zone vide visible ; sinon centre le contenu sur cet axe.
  const clampAxis = useCallback((pan: number, zoom: number, viewportSize: number, contentSize: number) => {
    const scaledContent = contentSize * zoom;
    if (scaledContent <= viewportSize) {
      return (viewportSize - scaledContent) / 2;
    }
    const minPan = viewportSize - scaledContent;
    return Math.min(0, Math.max(minPan, pan));
  }, []);

  const clampViewport = useCallback(
    (next: Viewport): Viewport => {
      const el = containerRef.current;
      if (!el) return next;
      const rect = el.getBoundingClientRect();
      return {
        zoom: next.zoom,
        panX: clampAxis(next.panX, next.zoom, rect.width, contentWidth),
        panY: clampAxis(next.panY, next.zoom, rect.height, contentHeight),
      };
    },
    [clampAxis, contentWidth, contentHeight]
  );

  const fitToContainer = useCallback(() => {
    const el = containerRef.current;
    if (!el || contentWidth === 0 || contentHeight === 0) return;
    const rect = el.getBoundingClientRect();
    const fitZoom = Math.min(rect.width / contentWidth, rect.height / contentHeight);
    minZoomRef.current = fitZoom;
    setViewport({
      zoom: fitZoom,
      panX: (rect.width - contentWidth * fitZoom) / 2,
      panY: (rect.height - contentHeight * fitZoom) / 2,
    });
  }, [contentWidth, contentHeight]);

  // Ajustement initial uniquement : un re-fit sur chaque resize ecraserait la navigation
  // manuelle du joueur (le bouton "Recentrer" couvre ce besoin explicitement).
  useEffect(() => {
    fitToContainer();
  }, [fitToContainer]);

  // La molette doit bloquer le scroll de la page : le `onWheel` synthetique de React est
  // passif par defaut et ne peut pas appeler preventDefault, d'ou un listener natif.
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    function handleWheel(event: WheelEvent) {
      event.preventDefault();
      const rect = el!.getBoundingClientRect();
      const cursorX = event.clientX - rect.left;
      const cursorY = event.clientY - rect.top;
      const factor = event.deltaY < 0 ? WHEEL_ZOOM_STEP : 1 / WHEEL_ZOOM_STEP;

      setViewport((prev) => {
        const maxZoom = minZoomRef.current * MAX_ZOOM_FACTOR;
        const nextZoom = Math.min(maxZoom, Math.max(minZoomRef.current, prev.zoom * factor));
        const contentX = (cursorX - prev.panX) / prev.zoom;
        const contentY = (cursorY - prev.panY) / prev.zoom;
        return clampViewport({
          zoom: nextZoom,
          panX: cursorX - contentX * nextZoom,
          panY: cursorY - contentY * nextZoom,
        });
      });
    }

    el.addEventListener("wheel", handleWheel, { passive: false });
    return () => el.removeEventListener("wheel", handleWheel);
    // Le conteneur n'existe dans le DOM qu'une fois la carte chargee (rendu conditionnel) :
    // sans ces dependances, cet effet s'execute une seule fois sur `containerRef.current`
    // encore null (premier rendu = ecran de chargement) et ne se rattache jamais au vrai
    // element une fois monte.
  }, [contentWidth, contentHeight, clampViewport]);

  function onPointerDown(event: React.PointerEvent) {
    if (event.button !== 2) return; // clic droit uniquement
    event.preventDefault();
    isPanningRef.current = true;
    setIsPanning(true);
    lastPointerRef.current = { x: event.clientX, y: event.clientY };
    (event.currentTarget as Element).setPointerCapture(event.pointerId);
  }

  function onPointerMove(event: React.PointerEvent) {
    if (!isPanningRef.current) return;
    const dx = event.clientX - lastPointerRef.current.x;
    const dy = event.clientY - lastPointerRef.current.y;
    lastPointerRef.current = { x: event.clientX, y: event.clientY };
    setViewport((prev) => clampViewport({ ...prev, panX: prev.panX + dx, panY: prev.panY + dy }));
  }

  function onPointerUp() {
    isPanningRef.current = false;
    setIsPanning(false);
  }

  function onContextMenu(event: React.MouseEvent) {
    event.preventDefault();
  }

  return {
    containerRef,
    viewport,
    isPanning,
    onPointerDown,
    onPointerMove,
    onPointerUp,
    onContextMenu,
    resetView: fitToContainer,
  };
}
