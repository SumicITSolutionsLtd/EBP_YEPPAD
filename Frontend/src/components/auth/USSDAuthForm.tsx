import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';
import { supabase } from '@/integrations/supabase/client';
import { Phone, User, CheckCircle } from 'lucide-react';

interface USSDAuthFormProps {
  onClose: () => void;
}

const USSDAuthForm = ({ onClose }: USSDAuthFormProps) => {
  const [step, setStep] = useState<'phone' | 'found' | 'register' | 'login'>('phone');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [ussdData, setUssdData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: ''
  });

  const { signIn, signUp } = useAuth();
  const { toast } = useToast();

  const checkUSSDRegistration = async () => {
    if (!phoneNumber || phoneNumber.length < 10) {
      toast({ title: "Please enter a valid phone number", variant: "destructive" });
      return;
    }

    setLoading(true);
    try {
      // Check localStorage for USSD registration
      const saved = localStorage.getItem('ussd_registered_users');
      const registeredUsers = saved ? JSON.parse(saved) : {};
      
      if (registeredUsers[phoneNumber]) {
        setUssdData(registeredUsers[phoneNumber]);
        
        // Check if user already has an account in Supabase
        const { data: phoneMapping } = await supabase
          .from('phone_user_mapping')
          .select('user_id')
          .eq('phone_number', phoneNumber)
          .single();

        if (phoneMapping) {
          setStep('login');
        } else {
          setStep('found');
        }
      } else {
        toast({ 
          title: "Phone not found", 
          description: "Please register using the USSD simulator first (*123#)", 
          variant: "destructive" 
        });
      }
    } catch (error) {
      console.error('Error checking USSD registration:', error);
      toast({ title: "Error checking registration", variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const createAccountFromUSSD = async () => {
    if (!formData.email || !formData.password) {
      toast({ title: "Please fill in all fields", variant: "destructive" });
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      toast({ title: "Passwords don't match", variant: "destructive" });
      return;
    }

    setLoading(true);
    try {
      // Create Supabase account with USSD data
      await signUp(formData.email, formData.password, {
        full_name: ussdData.name,
        phone: phoneNumber,
        location: ussdData.location,
        age: parseInt(ussdData.age) || null,
        gender: ussdData.gender,
        is_pwd: false,
        is_rural: false,
        preferred_language: 'English'
      });

      // Create phone mapping
      await supabase
        .from('phone_user_mapping')
        .insert({
          phone_number: phoneNumber,
          user_id: (await supabase.auth.getUser()).data.user?.id
        });

      toast({ title: "Account created successfully! Check your email to verify." });
      onClose();
    } catch (error: any) {
      toast({ title: "Error creating account", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const signInExistingUser = async () => {
    if (!formData.email || !formData.password) {
      toast({ title: "Please enter your email and password", variant: "destructive" });
      return;
    }

    setLoading(true);
    try {
      await signIn(formData.email, formData.password);
      toast({ title: "Welcome back!" });
      onClose();
    } catch (error: any) {
      toast({ title: "Sign in failed", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const renderContent = () => {
    switch (step) {
      case 'phone':
        return (
          <div className="space-y-4">
            <div className="text-center mb-6">
              <Phone className="h-12 w-12 mx-auto mb-4 text-primary" />
              <h3 className="text-lg font-semibold">Enter Your Phone Number</h3>
              <p className="text-sm text-muted-foreground">
                We'll check if you're registered via USSD
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                type="tel"
                placeholder="256XXXXXXXXX"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>
            <Button onClick={checkUSSDRegistration} className="w-full" disabled={loading}>
              {loading ? 'Checking...' : 'Check Registration'}
            </Button>
            <div className="text-center">
              <p className="text-sm text-muted-foreground">
                Not registered? Dial *123# to register via USSD first
              </p>
            </div>
          </div>
        );

      case 'found':
        return (
          <div className="space-y-4">
            <div className="text-center mb-6">
              <CheckCircle className="h-12 w-12 mx-auto mb-4 text-green-500" />
              <h3 className="text-lg font-semibold">USSD Registration Found!</h3>
              <p className="text-sm text-muted-foreground">
                Welcome {ussdData?.name}. Create your online account to continue.
              </p>
            </div>
            
            <div className="bg-muted p-4 rounded-lg space-y-2">
              <h4 className="font-medium">Your USSD Profile:</h4>
              <p className="text-sm"><strong>Name:</strong> {ussdData?.name}</p>
              <p className="text-sm"><strong>Age:</strong> {ussdData?.age}</p>
              <p className="text-sm"><strong>Gender:</strong> {ussdData?.gender}</p>
              <p className="text-sm"><strong>Location:</strong> {ussdData?.location}</p>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="your.email@example.com"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Create Password</Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                />
              </div>
            </div>

            <Button onClick={createAccountFromUSSD} className="w-full" disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Online Account'}
            </Button>
            
            <Button variant="outline" onClick={() => setStep('phone')} className="w-full">
              Back
            </Button>
          </div>
        );

      case 'login':
        return (
          <div className="space-y-4">
            <div className="text-center mb-6">
              <User className="h-12 w-12 mx-auto mb-4 text-primary" />
              <h3 className="text-lg font-semibold">Account Found</h3>
              <p className="text-sm text-muted-foreground">
                You already have an account. Please sign in.
              </p>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
              </div>
            </div>

            <Button onClick={signInExistingUser} className="w-full" disabled={loading}>
              {loading ? 'Signing In...' : 'Sign In'}
            </Button>
            
            <Button variant="outline" onClick={() => setStep('phone')} className="w-full">
              Back
            </Button>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle>Access Kwetu Hub</CardTitle>
        <CardDescription>
          Sign in with your USSD registration
        </CardDescription>
      </CardHeader>
      <CardContent>
        {renderContent()}
      </CardContent>
    </Card>
  );
};

export default USSDAuthForm;