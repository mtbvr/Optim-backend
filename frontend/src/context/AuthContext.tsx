import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { authApi } from "../api/authApi";
import type { LoginPayload, SignupPayload, User } from "../types/user";
import { AuthContext, type AuthStatus } from "./auth-context";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<AuthStatus>("checking");

  // Le token est dans un cookie httpOnly (invisible au JS) : au chargement de l'app,
  // on demande au backend "qui suis-je ?" pour savoir si une session est deja active.
  useEffect(() => {
    authApi
      .me()
      .then((current) => {
        setUser(current);
        setStatus("authenticated");
      })
      .catch(() => {
        setUser(null);
        setStatus("anonymous");
      });
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    const loggedInUser = await authApi.login(payload);
    setUser(loggedInUser);
    setStatus("authenticated");
  }, []);

  const signup = useCallback(async (payload: SignupPayload) => {
    const createdUser = await authApi.signup(payload);
    setUser(createdUser);
    setStatus("authenticated");
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
    setStatus("anonymous");
  }, []);

  const value = useMemo(
    () => ({ user, status, login, signup, logout }),
    [user, status, login, signup, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
