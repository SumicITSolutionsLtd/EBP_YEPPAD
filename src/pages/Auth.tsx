import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft, Chrome } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";
import { useToast } from "@/hooks/use-toast";

const Auth = () => {
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  const navigate = useNavigate();

  const handleSignIn = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    
    const formData = new FormData(e.currentTarget);
    const email = formData.get("signin-email") as string;
    const password = formData.get("signin-password") as string;
    
    // Placeholder auth - store in localStorage
    setTimeout(() => {
      localStorage.setItem("user", JSON.stringify({ email }));
      setIsLoading(false);
      toast({
        title: "Signed in successfully!",
        description: "Welcome back to Youth Connect.",
      });
      navigate("/dashboard/user");
    }, 1000);
  };

  const handleSignUp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    
    const formData = new FormData(e.currentTarget);
    const name = formData.get("signup-name") as string;
    const email = formData.get("signup-email") as string;
    const role = formData.get("signup-role") as string;
    
    // Placeholder auth - store in localStorage
    setTimeout(() => {
      localStorage.setItem("user", JSON.stringify({ email, name, role }));
      setIsLoading(false);
      toast({
        title: "Account created!",
        description: "Welcome to Youth Connect.",
      });
      // Navigate based on role
      if (role === "provider") {
        navigate("/dashboard/provider");
      } else if (role === "admin") {
        navigate("/dashboard/admin");
      } else {
        navigate("/dashboard/user");
      }
    }, 1000);
  };

  const handleGoogleSignIn = () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      toast({
        title: "Google sign-in coming soon!",
        description: "Backend integration is needed for Google authentication.",
      });
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary via-secondary to-bright-blue flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <Link to="/" className="inline-flex items-center text-white mb-6 hover:opacity-80 transition-opacity">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Home
        </Link>
        
        <Card className="shadow-2xl">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <img src={ebpLogo} alt="EBP Logo" className="h-16 w-auto" />
            </div>
            <CardTitle className="text-2xl">Welcome to Youth Connect</CardTitle>
            <CardDescription>Sign in or create an account to get started</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="signin" className="w-full">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="signin">Sign In</TabsTrigger>
                <TabsTrigger value="signup">Sign Up</TabsTrigger>
              </TabsList>
              
              <TabsContent value="signin">
                <form onSubmit={handleSignIn} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="signin-email">Email</Label>
                    <Input
                      id="signin-email"
                      name="signin-email"
                      type="email"
                      placeholder="your@email.com"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signin-password">Password</Label>
                    <Input
                      id="signin-password"
                      name="signin-password"
                      type="password"
                      placeholder="••••••••"
                      required
                    />
                  </div>
                  <Button type="submit" className="w-full" disabled={isLoading}>
                    {isLoading ? "Signing in..." : "Sign In"}
                  </Button>
                  
                  <div className="relative my-4">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-card px-2 text-muted-foreground">Or continue with</span>
                    </div>
                  </div>
                  
                  <Button 
                    type="button" 
                    variant="outline" 
                    className="w-full" 
                    onClick={handleGoogleSignIn}
                    disabled={isLoading}
                  >
                    <Chrome className="mr-2 h-4 w-4" />
                    Sign in with Google
                  </Button>
                </form>
              </TabsContent>
              
              <TabsContent value="signup">
                <form onSubmit={handleSignUp} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="signup-name">Full Name</Label>
                    <Input
                      id="signup-name"
                      name="signup-name"
                      type="text"
                      placeholder="John Doe"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signup-email">Email</Label>
                    <Input
                      id="signup-email"
                      name="signup-email"
                      type="email"
                      placeholder="your@email.com"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signup-password">Password</Label>
                    <Input
                      id="signup-password"
                      name="signup-password"
                      type="password"
                      placeholder="••••••••"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signup-role">I am a...</Label>
                    <select
                      id="signup-role"
                      name="signup-role"
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                      required
                    >
                      <option value="">Select your role</option>
                      <option value="youth">Youth Entrepreneur / Student</option>
                      <option value="provider">Service Provider / Mentor</option>
                      <option value="admin">NGO Administrator</option>
                    </select>
                  </div>
                  <Button type="submit" className="w-full" disabled={isLoading}>
                    {isLoading ? "Creating account..." : "Create Account"}
                  </Button>
                  
                  <div className="relative my-4">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-card px-2 text-muted-foreground">Or continue with</span>
                    </div>
                  </div>
                  
                  <Button 
                    type="button" 
                    variant="outline" 
                    className="w-full" 
                    onClick={handleGoogleSignIn}
                    disabled={isLoading}
                  >
                    <Chrome className="mr-2 h-4 w-4" />
                    Sign up with Google
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Auth;
