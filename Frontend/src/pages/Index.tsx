import { useState } from "react";
import { Users, Building, BarChart3, ArrowRight, Smartphone, Globe, MessageSquare } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useLanguage } from "@/contexts/LanguageContext";
import { Badge } from "@/components/ui/badge";
import Header from "@/components/Header";
import UserTypeCard from "@/components/UserTypeCard";
import ChatBot from "@/components/ChatBot";
import YouthDashboard from "./YouthDashboard";
import NGODashboard from "./NGODashboard";
import GovernmentDashboard from "./GovernmentDashboard";
import USSDSimulator from "@/components/ussd/USSDSimulator";
import heroImage from "@/assets/hero-image.jpg";

type UserType = "youth" | "ngo" | "government" | null;

const Index = () => {
  const [selectedUserType, setSelectedUserType] = useState<UserType>(null);
  const navigate = useNavigate();
  const { t } = useLanguage();

  if (selectedUserType === "youth") {
    return <YouthDashboard onBack={() => setSelectedUserType(null)} />;
  }

  if (selectedUserType === "ngo") {
    return <NGODashboard />;
  }

  if (selectedUserType === "government") {
    return <GovernmentDashboard />;
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary via-primary-glow to-accent py-20 lg:py-32 overflow-hidden">
        <div className="absolute inset-0 bg-black/20"></div>
        <div className="absolute inset-0">
          <img 
            src={heroImage} 
            alt="Rural youth using technology for entrepreneurship" 
            className="w-full h-full object-cover opacity-30"
          />
        </div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-white">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              {t('bridgingRuralYouth')}
              <span className="block text-accent-light">{t('entrepreneurship')}</span>
            </h1>
            <p className="text-xl md:text-2xl mb-8 max-w-3xl mx-auto opacity-90">
              {t('connectingOpportunities')}
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button size="lg" variant="hero" className="text-lg px-8 py-4" onClick={() => navigate('/auth')}>
                {t('getStarted')}
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
              <Button size="lg" variant="outline" className="text-lg px-8 py-4 border-white text-white hover:bg-white hover:text-primary" onClick={() => navigate('/about')}>
                {t('learnMore')}
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-muted/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-foreground mb-4">
              Empowering Rural Communities
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Offline-first technology ensuring access to opportunities regardless of connectivity
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-primary to-accent rounded-lg flex items-center justify-center mb-4">
                  <Smartphone className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Offline-First Access</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Cached job listings, peer mentorship voice notes, and skills marketplace 
                  accessible even without internet connectivity
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-accent to-accent-light rounded-lg flex items-center justify-center mb-4">
                  <Globe className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Multi-Language Support</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Voice-enabled learning in English, Lugbara, and Alur for true inclusivity 
                  across Northern Uganda's diverse communities
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-trust to-blue-500 rounded-lg flex items-center justify-center mb-4">
                  <MessageSquare className="h-6 w-6 text-white" />
                </div>
                <CardTitle>USSD/SMS Gateway</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Employer and youth portal accessible via basic phones through USSD/SMS 
                  for maximum reach and accessibility
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* User Type Selection */}
      <section className="py-20 bg-background">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-foreground mb-4">
              {t('chooseYourDashboard')}
            </h2>
            <p className="text-xl text-muted-foreground">
              {t('selectYourRole')}
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <UserTypeCard
              title="Youth Portal"
              description="Access opportunities, mentorship, and skills development"
              icon={Users}
              variant="youth"
              features={[
                "Browse loans, grants, and training opportunities",
                "Voice-enabled learning in local languages", 
                "Peer mentorship connections",
                "Skills marketplace profile",
                "Offline access to cached content"
              ]}
              buttonText="Enter Youth Portal"
              onSelect={() => setSelectedUserType("youth")}
            />

            <UserTypeCard
              title="NGO Dashboard"
              description="Support and mentor the next generation of entrepreneurs"
              icon={Building}
              variant="ngo"
              features={[
                "Manage mentorship programs",
                "Track mentee progress and outcomes",
                "Create and monitor training programs",
                "Access impact analytics",
                "Coordinate with government partners"
              ]}
              buttonText="Access NGO Dashboard"
              onSelect={() => setSelectedUserType("ngo")}
            />

            <UserTypeCard
              title="Government Portal"
              description="Monitor impact and track inclusion across all programs"
              icon={BarChart3}
              variant="government"
              features={[
                "Track women and PWD inclusion metrics",
                "Monitor regional program performance",
                "Generate impact reports",
                "Oversight of NGO partnerships",
                "Policy insights and recommendations"
              ]}
              buttonText="View Government Portal"
              onSelect={() => setSelectedUserType("government")}
            />
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-muted/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="text-center">
              <div className="text-4xl font-bold text-primary mb-2">3,400+</div>
              <div className="text-muted-foreground">Youth Reached</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-accent mb-2">68%</div>
              <div className="text-muted-foreground">Women Participation</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-trust mb-2">87%</div>
              <div className="text-muted-foreground">Program Success Rate</div>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-green-600 mb-2">12%</div>
              <div className="text-muted-foreground">PWD Inclusion</div>
            </div>
          </div>
        </div>
      </section>

      {/* Call to Action */}
      <section className="py-20 bg-gradient-to-r from-primary to-accent">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-white mb-4">
            Ready to Bridge the Gap?
          </h2>
          <p className="text-xl text-white/90 mb-8">
            Join thousands of rural youth accessing entrepreneurship opportunities across Northern Uganda
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" variant="secondary" className="text-lg px-8 py-4" onClick={() => navigate('/auth')}>
              Start Your Journey
            </Button>
            <Button size="lg" variant="outline" className="text-lg px-8 py-4 border-white text-white hover:bg-white hover:text-primary" onClick={() => navigate('/partner')}>
              Partner With Us
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-card border-t border-border py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-3 mb-4 md:mb-0">
              <div className="w-8 h-8 bg-gradient-to-r from-primary to-accent rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">K</span>
              </div>
              <span className="font-bold text-foreground">Kwetu Hub</span>
            </div>
            <div className="text-sm text-muted-foreground">
              Bridging rural youth to entrepreneurship opportunities across Northern Uganda
            </div>
          </div>
        </div>
      </footer>
      <ChatBot />
      <USSDSimulator />
    </div>
  );
};

export default Index;
