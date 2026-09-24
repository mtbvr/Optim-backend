import { useState } from "react";
import { useTranslation } from "react-i18next";

const EMOJIS = ["👍", "😂", "😡", "🔥", "💀", "🎯"];

export function EmotePicker({ onSend, disabled }: { onSend: (emoji: string) => void; disabled?: boolean }) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);

  return (
    <div className="emote-picker">
      <button type="button" className="btn btn-neutral" onClick={() => setOpen((prev) => !prev)}>
        {t("pixelWars.toolbar.reactions")}
      </button>
      {open && (
        <div className="emote-picker-popover">
          {EMOJIS.map((emoji) => (
            <button
              key={emoji}
              type="button"
              className="emote-picker-btn"
              disabled={disabled}
              onClick={() => {
                onSend(emoji);
                setOpen(false);
              }}
            >
              {emoji}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
