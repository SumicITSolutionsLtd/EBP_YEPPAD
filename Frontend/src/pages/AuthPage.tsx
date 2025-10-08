import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ArrowLeft, Eye, EyeOff, Users, Building, Globe } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { toast } from "sonner";
import { useLanguage } from "@/contexts/LanguageContext";
import UserTypeCard from "@/components/UserTypeCard";

type UserType = "youth" | "ngo" | "government";

const AuthPage = () => {
  const [step, setStep] = useState<"select" | "auth">("select");
  const [userType, setUserType] = useState<UserType>("youth");
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  
  const { user, signIn, signUp } = useAuth();
  const navigate = useNavigate();
  const { t } = useLanguage();

  useEffect(() => {
    if (user) {
      // Route to appropriate dashboard based on user type
      const routeMap = {
        youth: '/youth',
        ngo: '/ngo',
        government: '/government'
      };
      navigate(routeMap[userType]);
    }
  }, [user, navigate, userType]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (isLogin) {
        await signIn(email, password);
        toast.success(`Welcome back to your ${userType} portal!`);
        const routeMap = {
          youth: '/youth',
          ngo: '/ngo',
          government: '/government'
        };
        navigate(routeMap[userType]);
      } else {
        if (password !== confirmPassword) {
          toast.error("Passwords don't match");
          setLoading(false);
          return;
        }
        
        await signUp(email, password, { user_type: userType });
        toast.success(`${userType.charAt(0).toUpperCase() + userType.slice(1)} account created! Check your email to confirm.`);
      }
    } catch (error: any) {
      toast.error(error.message || "An unexpected error occurred");
    } finally {
      setLoading(false);
    }
  };

  const userTypeOptions = [
    {
      type: "youth" as const,
      title: "Youth Entrepreneur",
      description: "Access opportunities, mentorship, and resources",
      icon: Users,
      features: [
        "Browse job opportunities",
        "Access training programs",
        "Connect with mentors",
        "Voice learning in local languages"
      ],
      buttonText: "Join as Youth"
    },
    {
      type: "ngo" as const,
      title: "NGO Partner",
      description: "Monitor impact and support youth development",
      icon: Building,
      features: [
        "Track youth engagement",
        "Monitor inclusion metrics",
        "Generate impact reports",
        "Coordinate programs"
      ],
      buttonText: "Join as NGO"
    },
    {
      type: "government" as const,
      title: "Government Official",
      description: "Policy monitoring and strategic oversight",
      icon: Globe,
      features: [
        "National policy tracking",
        "Regional performance analysis",
        "Digital access monitoring",
        "Strategic recommendations"
      ],
      buttonText: "Join as Official"
    }
  ];

  if (step === "select") {
    return (
      <div className="min-h-screen bg-gradient-to-br from-primary via-primary-glow to-accent flex items-center justify-center p-4">
        <div className="w-full max-w-6xl">
          <Button
            variant="ghost"
            onClick={() => navigate('/')}
            className="mb-6 text-white hover:bg-white/10"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Home
          </Button>
          
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-white mb-2">Choose Your Portal</h1>
            <p className="text-white/80">Select the type of account that best describes you</p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {userTypeOptions.map((option) => (
              <UserTypeCard
                key={option.type}
                title={option.title}
                description={option.description}
                icon={option.icon}
                features={option.features}
                buttonText={option.buttonText}
                variant={option.type}
                onSelect={() => {
                  setUserType(option.type);
                  setStep("auth");
                }}
              />
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary via-primary-glow to-accent flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <Button
          variant="ghost"
          onClick={() => setStep("select")}
          className="mb-6 text-white hover:bg-white/10"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Portal Selection
        </Button>
        
        <Card className="w-full bg-white/95 backdrop-blur-sm shadow-2xl">
          <CardHeader className="text-center">
            <div className="mx-auto w-12 h-12 bg-gradient-to-r from-primary to-accent rounded-lg flex items-center justify-center mb-4">
              {userType === "youth" && <Users className="h-6 w-6 text-white" />}
              {userType === "ngo" && <Building className="h-6 w-6 text-white" />}
              {userType === "government" && <Globe className="h-6 w-6 text-white" />}
            </div>
            <CardTitle className="text-2xl font-bold">
              {isLogin ? `Welcome Back` : `Join as ${userType.charAt(0).toUpperCase() + userType.slice(1)}`}
            </CardTitle>
            <CardDescription>
              {isLogin 
                ? `Sign in to your ${userType} portal` 
                : `Create your ${userType} account to get started`
              }
            </CardDescription>
          </CardHeader>
          
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </Button>
                </div>
              </div>
              
              {!isLogin && (
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    placeholder="Confirm your password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                </div>
              )}
              
              <Button 
                type="submit" 
                className="w-full" 
                disabled={loading}
                variant={userType === "youth" ? "hero" : userType === "ngo" ? "accent" : "trust"}
              >
                {loading ? "Please wait..." : (isLogin ? `Sign In to ${userType.charAt(0).toUpperCase() + userType.slice(1)} Portal` : `Create ${userType.charAt(0).toUpperCase() + userType.slice(1)} Account`)}
              </Button>
            </form>
            
            <div className="mt-6 text-center space-y-2">
              <Button
                variant="ghost"
                onClick={() => setIsLogin(!isLogin)}
                className="text-sm"
              >
                {isLogin 
                  ? `Don't have a ${userType} account? Sign up` 
                  : `Already have a ${userType} account? Sign in`
                }
              </Button>
              <div className="text-xs text-muted-foreground">
                Selected: {userType.charAt(0).toUpperCase() + userType.slice(1)} Portal
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AuthPage;