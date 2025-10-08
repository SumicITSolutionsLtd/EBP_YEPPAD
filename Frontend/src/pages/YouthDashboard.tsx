import { useState } from "react";
import { ArrowLeft, Search, Filter, MessageCircle, Mic, BookOpen, Briefcase, Target, LogIn } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useLanguage } from "@/contexts/LanguageContext";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import Header from "@/components/Header";
import OpportunityCard from "@/components/OpportunityCard";
import USSDAuthForm from "@/components/auth/USSDAuthForm";
import { useAuth } from "@/hooks/useAuth";

interface YouthDashboardProps {
  onBack: () => void;
}

const YouthDashboard = ({ onBack }: YouthDashboardProps) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("all");
  const [showAuth, setShowAuth] = useState(false);
  const { user } = useAuth();
  const { t } = useLanguage();

  const opportunities = [
    {
      id: 1,
      title: "Youth Agriculture Loan Program",
      description: "Low-interest loans for young farmers to start or expand agricultural businesses",
      type: "loan" as const,
      location: "Arua District",
      deadline: "March 30, 2024",
      amount: "$500 - $5,000"
    },
    {
      id: 2,
      title: "Digital Skills Training",
      description: "Free 3-month course on web development and digital marketing",
      type: "training" as const,
      location: "Gulu City",
      deadline: "February 15, 2024",
      participants: 120
    },
    {
      id: 3,
      title: "Women Entrepreneurs Grant",
      description: "Non-repayable grants for women-led startups",
      type: "grant" as const,
      location: "Lira District",
      deadline: "April 5, 2024",
      amount: "$1,000 - $3,000"
    },
    {
      id: 4,
      title: "Regional Market Access Program",
      description: "Connect your products with regional buyers and distributors",
      type: "market" as const,
      location: "Northern Uganda",
      deadline: "Ongoing",
      participants: 85
    }
  ];

  const voiceLearningModules = [
    { title: "Business Planning", language: "English", duration: "15 min" },
    { title: "Okwanya ku Bisnis", language: "Lugbara", duration: "12 min" },
    { title: "Limo Adongo", language: "Alur", duration: "18 min" }
  ];

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-3">
            <Button variant="ghost" onClick={() => window.location.href = '/'}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              {t('backToHome')}
            </Button>
            <div>
              <h1 className="text-2xl font-bold text-foreground">{t('youthDashboard')}</h1>
              <p className="text-muted-foreground">{t('discoverOpportunities')}</p>
            </div>
          </div>
          <div className="flex gap-3">
            {user ? (
              <Button 
                variant="hero"
                onClick={() => window.location.href = '/app'}
              >
                <Briefcase className="h-4 w-4 mr-2" />
                {t('fullPlatform')}
              </Button>
            ) : (
              <Button 
                variant="hero"
                onClick={() => setShowAuth(true)}
              >
                <LogIn className="h-4 w-4 mr-2" />
                {t('signInForFullAccess')}
              </Button>
            )}
            <Button 
              variant="hero"
              onClick={() => window.location.href = '/activities'}
            >
              <Target className="h-4 w-4 mr-2" />
              Activities & Skills
            </Button>
            <Button variant="accent">
              <MessageCircle className="h-4 w-4 mr-2" />
              Mentorship
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-3 space-y-6">
            {/* Quick Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Card className="bg-gradient-to-r from-primary to-primary-glow border-0">
                <CardContent className="p-4">
                  <div className="text-white">
                    <p className="text-sm opacity-90">Available Opportunities</p>
                    <p className="text-2xl font-bold">247</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="bg-gradient-to-r from-accent to-accent-light border-0">
                <CardContent className="p-4">
                  <div className="text-white">
                    <p className="text-sm opacity-90">Applications Sent</p>
                    <p className="text-2xl font-bold">3</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="bg-gradient-to-r from-trust to-blue-500 border-0">
                <CardContent className="p-4">
                  <div className="text-white">
                    <p className="text-sm opacity-90">Mentorship Hours</p>
                    <p className="text-2xl font-bold">12</p>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Search and Filters */}
            <Card>
              <CardContent className="p-4">
                <div className="flex flex-col sm:flex-row gap-4">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search opportunities..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                  <div className="flex gap-2">
                    {["all", "loans", "grants", "training", "markets"].map((filter) => (
                      <Button
                        key={filter}
                        variant={activeFilter === filter ? "default" : "outline"}
                        size="sm"
                        onClick={() => setActiveFilter(filter)}
                      >
                        {filter.charAt(0).toUpperCase() + filter.slice(1)}
                      </Button>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Opportunities Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {opportunities.map((opportunity) => (
                <OpportunityCard
                  key={opportunity.id}
                  {...opportunity}
                  onBookmark={() => console.log("Bookmarked:", opportunity.id)}
                  onApply={() => console.log("Applied to:", opportunity.id)}
                />
              ))}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Voice Learning */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <Mic className="h-5 w-5 mr-2 text-primary" />
                  Voice Learning
                </CardTitle>
                <CardDescription>
                  Learn in your preferred language
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                {voiceLearningModules.map((module, index) => (
                  <div key={index} className="flex items-center justify-between p-3 bg-muted rounded-lg">
                    <div>
                      <p className="font-medium text-sm">{module.title}</p>
                      <p className="text-xs text-muted-foreground">{module.language} • {module.duration}</p>
                    </div>
                    <Button size="sm" variant="outline">
                      <BookOpen className="h-3 w-3" />
                    </Button>
                  </div>
                ))}
              </CardContent>
            </Card>

            {/* Skills Marketplace */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <Briefcase className="h-5 w-5 mr-2 text-primary" />
                  Skills Marketplace
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm">Your Profile Views</span>
                    <Badge variant="secondary">23 this week</Badge>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm">Skills Listed</span>
                    <Badge variant="secondary">5</Badge>
                  </div>
                  <Button className="w-full" variant="outline">
                    Update Profile
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* Offline Status */}
            <Card className="border-accent/50 bg-accent/5">
              <CardContent className="p-4">
                <div className="text-center">
                  <div className="w-8 h-8 bg-accent rounded-full flex items-center justify-center mx-auto mb-2">
                    <span className="text-xs text-white font-bold">📱</span>
                  </div>
                  <p className="text-sm font-medium text-accent">Offline Mode Ready</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    Your data is cached locally for offline access
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      {/* Auth Dialog */}
      <Dialog open={showAuth} onOpenChange={setShowAuth}>
        <DialogContent className="sm:max-w-md">
          <USSDAuthForm onClose={() => setShowAuth(false)} />
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default YouthDashboard;