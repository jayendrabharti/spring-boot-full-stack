import { useAuth } from "@/providers/AuthProvider";

export default function HomePage() {
  const { user, isAuthenticated } = useAuth();

  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-2xl mx-auto text-center space-y-6">
        <h1 className="text-4xl font-bold tracking-tight">Welcome to MyApp</h1>
        {isAuthenticated ? (
          <p className="text-lg text-muted-foreground">
            You're logged in as{" "}
            <span className="font-medium text-foreground">{user?.email}</span>
          </p>
        ) : (
          <p className="text-lg text-muted-foreground">
            Log in or sign up to get started.
          </p>
        )}
      </div>
    </div>
  );
}
