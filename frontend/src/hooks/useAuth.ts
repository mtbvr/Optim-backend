import { useContext } from "react";
import { AuthContext } from "../context/auth-context";

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth doit etre utilise a l'interieur d'un <AuthProvider>");
  }
  return context;
}
