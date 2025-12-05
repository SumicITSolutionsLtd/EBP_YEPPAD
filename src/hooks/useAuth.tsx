import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useToast } from "@/hooks/use-toast";

// Placeholder auth hook - replace with real auth when backend is connected
export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    // Check if user is "logged in" (placeholder using localStorage)
    const checkAuth = () => {
      const user = localStorage.getItem("user");
      setIsAuthenticated(!!user);
      setIsLoading(false);
    };
    
    // Simulate async auth check
    setTimeout(checkAuth, 100);
  }, []);

  const login = (email: string, password: string) => {
    // Placeholder login - store user in localStorage
    localStorage.setItem("user", JSON.stringify({ email }));
    setIsAuthenticated(true);
    toast({
      title: "Signed in successfully!",
      description: "Welcome back to Youth Connect.",
    });
    return true;
  };

  const signup = (email: string, password: string, name: string) => {
    // Placeholder signup
    localStorage.setItem("user", JSON.stringify({ email, name }));
    setIsAuthenticated(true);
    toast({
      title: "Account created!",
      description: "Welcome to Youth Connect.",
    });
    return true;
  };

  const logout = () => {
    localStorage.removeItem("user");
    setIsAuthenticated(false);
    navigate("/");
    toast({
      title: "Signed out",
      description: "You have been logged out.",
    });
  };

  const requireAuth = () => {
    if (!isLoading && !isAuthenticated) {
      toast({
        title: "Authentication required",
        description: "Please sign in to access this page.",
        variant: "destructive",
      });
      navigate("/auth");
      return false;
    }
    return true;
  };

  return {
    isAuthenticated,
    isLoading,
    login,
    signup,
    logout,
    requireAuth,
  };
};
