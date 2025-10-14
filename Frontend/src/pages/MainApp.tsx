import { useState } from 'react';
import { Dialog, DialogContent } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { User, LogIn, MessageCircle } from 'lucide-react';
import BottomNav from '@/components/navigation/BottomNav';
import JobsBoard from '@/components/jobs/JobsBoard';
import SkillsMarketplace from '@/components/skills/SkillsMarketplace';
import MentorshipWall from '@/components/mentorship/MentorshipWall';
import LearningHub from '@/components/learning/LearningHub';
import UserProfile from '@/components/profile/UserProfile';
import USSDAuthForm from '@/components/auth/USSDAuthForm';
import VoiceInterface from '@/components/voice/VoiceInterface';
import { useAuth } from '@/hooks/useAuth';

const MainApp = () => {
  const [activeTab, setActiveTab] = useState('jobs');
  const [showAuth, setShowAuth] = useState(false);
  const [showVoiceInterface, setShowVoiceInterface] = useState(false);
  
  const { user, loading } = useAuth();

  const renderContent = () => {
    switch (activeTab) {
      case 'jobs':
        return <JobsBoard />;
      case 'skills':
        return <SkillsMarketplace />;
      case 'mentorship':
        return <MentorshipWall />;
      case 'learning':
        return <LearningHub />;
      case 'profile':
        return user ? <UserProfile /> : <GuestProfile />;
      default:
        return <JobsBoard />;
    }
  };

  const GuestProfile = () => (
    <div className="p-4 pb-20 space-y-6">
      <div className="text-center space-y-4">
        <div className="w-20 h-20 bg-muted rounded-full flex items-center justify-center mx-auto">
          <User className="h-10 w-10 text-muted-foreground" />
        </div>
        <div>
          <h2 className="text-xl font-semibold">Welcome to Kwetu Hub</h2>
          <p className="text-muted-foreground">Sign in to access all features</p>
        </div>
        <Button onClick={() => setShowAuth(true)} size="lg">
          <LogIn className="h-4 w-4 mr-2" />
          Sign In / Register
        </Button>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p className="text-muted-foreground">Loading Kwetu Hub...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-40 bg-card border-b border-border">
        <div className="flex items-center justify-between px-4 h-14">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-sm font-bold text-primary-foreground">K</span>
            </div>
            <span className="font-bold text-lg text-foreground">Kwetu Hub</span>
          </div>
          
          {!user && (
            <Button 
              variant="ghost" 
              size="sm"
              onClick={() => setShowAuth(true)}
            >
              <LogIn className="h-4 w-4 mr-2" />
              Sign In
            </Button>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="min-h-[calc(100vh-3.5rem)]">
        {renderContent()}
      </main>

      {/* Bottom Navigation */}
      <BottomNav activeTab={activeTab} onTabChange={setActiveTab} />

      {/* Voice Assistant Button */}
      <Button
        size="lg"
        className="fixed bottom-20 right-4 rounded-full h-14 w-14 shadow-lg z-50"
        onClick={() => setShowVoiceInterface(true)}
      >
        <MessageCircle className="h-6 w-6" />
      </Button>

      {/* Auth Dialog */}
      <Dialog open={showAuth} onOpenChange={setShowAuth}>
        <DialogContent className="sm:max-w-md">
          <USSDAuthForm onClose={() => setShowAuth(false)} />
        </DialogContent>
      </Dialog>

      {/* Voice Interface Modal */}
      <VoiceInterface 
        isOpen={showVoiceInterface} 
        onClose={() => setShowVoiceInterface(false)} 
      />
      
    </div>
  );
};

export default MainApp;