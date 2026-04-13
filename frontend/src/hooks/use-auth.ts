import { useState, useEffect, useCallback } from "react";
import { getToken, clearToken } from "@/lib/api";

export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

  useEffect(() => {
    setIsAuthenticated(!!getToken());
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setIsAuthenticated(false);
    window.location.href = "/login";
  }, []);

  return { isAuthenticated, logout };
}
