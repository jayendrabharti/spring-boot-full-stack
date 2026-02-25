import { Outlet, Navigate } from "react-router-dom";
import { useAuth } from "@/providers/AuthProvider";
import { Zap } from "lucide-react";

export default function AuthLayout() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-muted/40 px-4">
      <div className="mb-8 flex items-center gap-2 text-2xl font-bold">
        <Zap className="h-7 w-7 text-primary" />
        <span>MyApp</span>
      </div>
      <Outlet />
    </div>
  );
}
