import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Phone, PhoneCall, X } from 'lucide-react';
import { useLanguage } from '@/contexts/LanguageContext';

interface USSDSession {
  phoneNumber: string;
  currentMenu: string;
  userData?: {
    name: string;
    age: string;
    gender: string;
    location: string;
  };
  sessionHistory: string[];
}

const USSDSimulator: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [currentInput, setCurrentInput] = useState('');
  const [session, setSession] = useState<USSDSession | null>(null);
  const [displayText, setDisplayText] = useState('');
  const [registeredUsers, setRegisteredUsers] = useState<{[key: string]: any}>({});
  const { t } = useLanguage();

  // Load registered users from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('ussd_registered_users');
    if (saved) {
      setRegisteredUsers(JSON.parse(saved));
    }
  }, []);

  const saveUser = (phone: string, userData: any) => {
    const updated = { ...registeredUsers, [phone]: userData };
    setRegisteredUsers(updated);
    localStorage.setItem('ussd_registered_users', JSON.stringify(updated));
  };

  const generateMenu = (menuType: string, userData?: any): string => {
    switch (menuType) {
      case 'welcome':
        return `CON ${t('welcomeToKwetuHub')}
1. ${t('registerNewUser')}
2. ${t('existingUser')}`;

      case 'register_name':
        return `CON ${t('registration')}
${t('enterYourName')}:`;

      case 'register_age':
        return `CON ${t('registration')}
${t('enterYourAge')}:`;

      case 'register_gender':
        return `CON ${t('registration')}
${t('selectGender')}:
1. ${t('male')}
2. ${t('female')}
3. ${t('other')}`;

      case 'register_location':
        return `CON ${t('registration')}
${t('enterYourLocation')}:`;

      case 'main_menu':
        return `CON ${t('welcome')} ${userData?.name || t('user')}!
1. ${t('opportunities')}
2. ${t('mentorship')}
3. ${t('businessTools')}
4. ${t('marketAccess')}
5. ${t('jobsInternships')}
6. ${t('myProfile')}
0. ${t('exit')}`;

      case 'opportunities':
        return `CON ${t('opportunities')}
1. ${t('skills')} - Arua (50k/month)
2. ${t('farming')} - Yumbe (80k/month)
3. ${t('tailoring')} - Nebbi (40k/month)
4. ${t('cooking')} - Adjumani (30k/day)
5. ${t('weaving')} - Moyo (25k/day)
0. ${t('backToMainMenu')}`;

      case 'mentorship':
        return `CON ${t('mentorship')}
1. ${t('askQuestion')}
2. ${t('viewAnswers')}
3. ${t('businessAdvice')}
4. ${t('skillsTraining')}
0. ${t('backToMainMenu')}`;

      case 'business_tools':
        return `CON ${t('businessTools')}
1. ${t('businessPlan')}
2. ${t('financialPlanning')}
3. ${t('marketAnalysis')}
4. ${t('recordKeeping')}
0. ${t('backToMainMenu')}`;

      case 'market_access':
        return `CON ${t('marketAccess')}
1. ${t('localMarkets')}
2. ${t('onlineMarkets')}
3. ${t('exportMarkets')}
4. ${t('priceInformation')}
0. ${t('backToMainMenu')}`;

      case 'jobs_internships':
        return `CON ${t('jobsInternships')}
1. ${t('viewJobs')}
2. ${t('viewInternships')}
3. ${t('applyForJob')}
4. ${t('careerGuidance')}
0. ${t('backToMainMenu')}`;

      case 'profile':
        return `CON ${t('yourProfile')}
${t('name')}: ${userData?.name}
${t('age')}: ${userData?.age}
${t('gender')}: ${userData?.gender}
${t('location')}: ${userData?.location}
0. ${t('backToMainMenu')}`;

      default:
        return `END ${t('invalidSelection')}. ${t('pleaseDialAgain')}.`;
    }
  };

  const processInput = (input: string) => {
    if (!session) return;

    const newSession = { ...session };
    newSession.sessionHistory.push(input);

    switch (session.currentMenu) {
      case 'welcome':
        if (input === '1') {
          newSession.currentMenu = 'register_name';
        } else if (input === '2') {
          const user = registeredUsers[session.phoneNumber];
          if (user) {
            newSession.userData = user;
            newSession.currentMenu = 'main_menu';
          } else {
            setDisplayText(`END ${t('phoneNotRegistered')}. ${t('pleaseRegisterFirst')}.`);
            return;
          }
        } else {
          setDisplayText(`END ${t('invalidSelection')}.`);
          return;
        }
        break;

      case 'register_name':
        if (input.length > 1) {
          newSession.userData = { ...newSession.userData, name: input };
          newSession.currentMenu = 'register_age';
        } else {
          setDisplayText(`END ${t('pleaseEnterValidName')}.`);
          return;
        }
        break;

      case 'register_age':
        const age = parseInt(input);
        if (age > 0 && age < 100) {
          newSession.userData = { ...newSession.userData, age: input };
          newSession.currentMenu = 'register_gender';
        } else {
          setDisplayText(`END ${t('pleaseEnterValidAge')}.`);
          return;
        }
        break;

      case 'register_gender':
        let gender = '';
        if (input === '1') gender = t('male');
        else if (input === '2') gender = t('female');
        else if (input === '3') gender = t('other');
        else {
          setDisplayText(`END ${t('invalidSelection')}.`);
          return;
        }
        newSession.userData = { ...newSession.userData, gender };
        newSession.currentMenu = 'register_location';
        break;

      case 'register_location':
        if (input.length > 1) {
          newSession.userData = { ...newSession.userData, location: input };
          saveUser(session.phoneNumber, newSession.userData);
          newSession.currentMenu = 'main_menu';
          setDisplayText(`CON ${t('registrationComplete')}!\n${generateMenu('main_menu', newSession.userData)}`);
          setSession(newSession);
          return;
        } else {
          setDisplayText(`END ${t('pleaseEnterValidLocation')}.`);
          return;
        }
        break;

      case 'main_menu':
        if (input === '1') newSession.currentMenu = 'opportunities';
        else if (input === '2') newSession.currentMenu = 'mentorship';
        else if (input === '3') newSession.currentMenu = 'business_tools';
        else if (input === '4') newSession.currentMenu = 'market_access';
        else if (input === '5') newSession.currentMenu = 'jobs_internships';
        else if (input === '6') newSession.currentMenu = 'profile';
        else if (input === '0') {
          setDisplayText(`END ${t('thankYou')}!`);
          return;
        } else {
          setDisplayText(`END ${t('invalidSelection')}.`);
          return;
        }
        break;

      default:
        if (input === '0') {
          newSession.currentMenu = 'main_menu';
        } else {
          setDisplayText(`END ${t('featureComingSoon')}.`);
          return;
        }
        break;
    }

    setSession(newSession);
    setDisplayText(generateMenu(newSession.currentMenu, newSession.userData));
  };

  const startSession = () => {
    if (!phoneNumber || phoneNumber.length < 10) {
      alert(t('pleaseEnterValidPhoneNumber'));
      return;
    }

    const newSession: USSDSession = {
      phoneNumber,
      currentMenu: 'welcome',
      sessionHistory: [],
    };

    setSession(newSession);
    setDisplayText(generateMenu('welcome'));
  };

  const endSession = () => {
    setSession(null);
    setDisplayText('');
    setCurrentInput('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (currentInput.trim()) {
      processInput(currentInput.trim());
      setCurrentInput('');
    }
  };

  return (
    <>
      <Button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-20 left-4 z-50 rounded-full h-12 w-12 bg-primary hover:bg-primary/90 shadow-lg"
        size="icon"
        title="USSD Simulator"
      >
        <Phone className="h-6 w-6 text-primary-foreground" />
      </Button>

      {isOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <Card className="w-full max-w-md bg-background border-2">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-lg font-semibold">{t('ussdSimulator')}</CardTitle>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setIsOpen(false)}
                className="h-8 w-8"
              >
                <X className="h-4 w-4" />
              </Button>
            </CardHeader>
            <CardContent className="space-y-4">
              {!session ? (
                <div className="space-y-4">
                  <div>
                    <label className="text-sm font-medium">{t('phoneNumber')}</label>
                    <Input
                      type="tel"
                      placeholder="256XXXXXXXXX"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                      className="mt-1"
                    />
                  </div>
                  <Button onClick={startSession} className="w-full">
                    <PhoneCall className="h-4 w-4 mr-2" />
                    {t('dialUssd')}
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="bg-black text-green-400 p-3 rounded font-mono text-sm">
                    <ScrollArea className="h-40">
                      <pre className="whitespace-pre-wrap">{displayText}</pre>
                    </ScrollArea>
                  </div>
                  
                  {!displayText.startsWith('END') && (
                    <form onSubmit={handleSubmit} className="space-y-2">
                      <Input
                        value={currentInput}
                        onChange={(e) => setCurrentInput(e.target.value)}
                        placeholder={t('enterYourChoice')}
                        className="font-mono"
                        autoFocus
                      />
                      <div className="flex gap-2">
                        <Button type="submit" className="flex-1">
                          {t('send')}
                        </Button>
                        <Button type="button" variant="outline" onClick={endSession}>
                          {t('endCall')}
                        </Button>
                      </div>
                    </form>
                  )}
                  
                  {displayText.startsWith('END') && (
                    <Button onClick={endSession} className="w-full" variant="outline">
                      {t('endCall')}
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </>
  );
};

export default USSDSimulator;