import { useState, useEffect } from 'react';
import { User, MapPin, Phone, Mail, Edit, LogOut, Settings, Trophy, Briefcase } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { supabase } from '@/integrations/supabase/client';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';

interface UserProfile {
  id: string;
  full_name: string;
  email: string;
  phone: string;
  location: string;
  age: number;
  gender: string;
  is_pwd: boolean;
  is_rural: boolean;
  preferred_language: string;
}

const UserProfile = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [editingProfile, setEditingProfile] = useState(false);
  const [stats, setStats] = useState({
    applications: 0,
    skills: 0,
    mentorship_sessions: 0,
    completed_courses: 0
  });

  const { user, signOut } = useAuth();
  const { toast } = useToast();

  useEffect(() => {
    if (user) {
      fetchProfile();
      fetchStats();
    }
  }, [user]);

  const fetchProfile = async () => {
    if (!user) return;

    try {
      const { data, error } = await supabase
        .from('profiles')
        .select('*')
        .eq('user_id', user.id)
        .single();

      if (error) throw error;
      setProfile(data);
    } catch (error: any) {
      toast({ title: "Error loading profile", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    if (!user) return;

    try {
      // Fetch applications count
      const { count: applicationsCount } = await supabase
        .from('applications')
        .select('*', { count: 'exact', head: true })
        .eq('applicant_id', user.id);

      // Fetch skills count
      const { count: skillsCount } = await supabase
        .from('skills')
        .select('*', { count: 'exact', head: true })
        .eq('user_id', user.id);

      // Fetch mentorship sessions count
      const { count: mentorshipCount } = await supabase
        .from('mentorship_sessions')
        .select('*', { count: 'exact', head: true })
        .eq('mentee_id', user.id);

      // Fetch completed courses count
      const { count: coursesCount } = await supabase
        .from('business_progress')
        .select('*', { count: 'exact', head: true })
        .eq('user_id', user.id)
        .eq('completion_percentage', 100);

      setStats({
        applications: applicationsCount || 0,
        skills: skillsCount || 0,
        mentorship_sessions: mentorshipCount || 0,
        completed_courses: coursesCount || 0
      });
    } catch (error: any) {
      console.error('Error fetching stats:', error);
    }
  };

  const updateProfile = async (updatedProfile: Partial<UserProfile>) => {
    if (!user || !profile) return;

    try {
      setEditingProfile(true);
      const { error } = await supabase
        .from('profiles')
        .update(updatedProfile)
        .eq('user_id', user.id);

      if (error) throw error;

      setProfile({ ...profile, ...updatedProfile });
      toast({ title: "Profile updated successfully!" });
    } catch (error: any) {
      toast({ title: "Error updating profile", description: error.message, variant: "destructive" });
    } finally {
      setEditingProfile(false);
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut();
      toast({ title: "Signed out successfully" });
    } catch (error: any) {
      toast({ title: "Error signing out", description: error.message, variant: "destructive" });
    }
  };

  if (loading) {
    return (
      <div className="p-4 space-y-4">
        <Card className="animate-pulse">
          <CardContent className="p-6">
            <div className="h-20 bg-muted rounded-full w-20 mx-auto mb-4"></div>
            <div className="h-4 bg-muted rounded w-1/2 mx-auto"></div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="p-4">
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-muted-foreground">Unable to load profile</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4 pb-20 space-y-6">
      {/* Profile Header */}
      <Card>
        <CardContent className="p-6">
          <div className="flex flex-col items-center text-center space-y-4">
            <Avatar className="h-20 w-20">
              <AvatarFallback className="text-2xl">
                {profile.full_name?.charAt(0) || 'U'}
              </AvatarFallback>
            </Avatar>
            
            <div>
              <h1 className="text-2xl font-bold text-foreground">{profile.full_name}</h1>
              <div className="flex items-center justify-center text-muted-foreground mt-1">
                <MapPin className="h-4 w-4 mr-1" />
                {profile.location || 'Location not set'}
              </div>
            </div>

            <div className="flex flex-wrap gap-2 justify-center">
              {profile.is_rural && (
                <Badge variant="outline">Rural Youth</Badge>
              )}
              {profile.is_pwd && (
                <Badge variant="outline">PWD</Badge>
              )}
              <Badge variant="secondary">{profile.preferred_language}</Badge>
            </div>

            <div className="flex gap-2">
              <Dialog>
                <DialogTrigger asChild>
                  <Button variant="outline" size="sm">
                    <Edit className="h-4 w-4 mr-2" />
                    Edit Profile
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Edit Profile</DialogTitle>
                    <DialogDescription>
                      Update your personal information
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="full_name">Full Name</Label>
                      <Input
                        id="full_name"
                        defaultValue={profile.full_name}
                        onChange={(e) => updateProfile({ full_name: e.target.value })}
                      />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="phone">Phone</Label>
                        <Input
                          id="phone"
                          defaultValue={profile.phone}
                          onChange={(e) => updateProfile({ phone: e.target.value })}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="age">Age</Label>
                        <Input
                          id="age"
                          type="number"
                          defaultValue={profile.age}
                          onChange={(e) => updateProfile({ age: parseInt(e.target.value) || 0 })}
                        />
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <Label htmlFor="location">Location</Label>
                      <Input
                        id="location"
                        defaultValue={profile.location}
                        onChange={(e) => updateProfile({ location: e.target.value })}
                      />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="gender">Gender</Label>
                        <Select 
                          defaultValue={profile.gender} 
                          onValueChange={(value) => updateProfile({ gender: value })}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="male">Male</SelectItem>
                            <SelectItem value="female">Female</SelectItem>
                            <SelectItem value="other">Other</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="language">Preferred Language</Label>
                        <Select 
                          defaultValue={profile.preferred_language} 
                          onValueChange={(value) => updateProfile({ preferred_language: value })}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="English">English</SelectItem>
                            <SelectItem value="Lugbara">Lugbara</SelectItem>
                            <SelectItem value="Alur">Alur</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id="is_rural"
                          defaultChecked={profile.is_rural}
                          onCheckedChange={(checked) => updateProfile({ is_rural: !!checked })}
                        />
                        <Label htmlFor="is_rural">I live in a rural area</Label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id="is_pwd"
                          defaultChecked={profile.is_pwd}
                          onCheckedChange={(checked) => updateProfile({ is_pwd: !!checked })}
                        />
                        <Label htmlFor="is_pwd">Person with disability</Label>
                      </div>
                    </div>
                  </div>
                </DialogContent>
              </Dialog>
              
              <Button variant="outline" size="sm">
                <Settings className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Contact Info */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Contact Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center space-x-3">
            <Mail className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm">{profile.email}</span>
          </div>
          {profile.phone && (
            <div className="flex items-center space-x-3">
              <Phone className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm">{profile.phone}</span>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Activity Stats */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center">
            <Trophy className="h-5 w-5 mr-2 text-primary" />
            Your Activity
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center space-y-1">
              <p className="text-2xl font-bold text-primary">{stats.applications}</p>
              <p className="text-sm text-muted-foreground">Applications</p>
            </div>
            <div className="text-center space-y-1">
              <p className="text-2xl font-bold text-accent">{stats.skills}</p>
              <p className="text-sm text-muted-foreground">Skills Listed</p>
            </div>
            <div className="text-center space-y-1">
              <p className="text-2xl font-bold text-trust">{stats.mentorship_sessions}</p>
              <p className="text-sm text-muted-foreground">Questions Asked</p>
            </div>
            <div className="text-center space-y-1">
              <p className="text-2xl font-bold text-growth">{stats.completed_courses}</p>
              <p className="text-sm text-muted-foreground">Courses Completed</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Quick Actions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start">
            <Briefcase className="h-4 w-4 mr-2" />
            View My Applications
          </Button>
          <Button variant="outline" className="w-full justify-start">
            <User className="h-4 w-4 mr-2" />
            Manage Skills
          </Button>
          <Button variant="outline" className="w-full justify-start">
            <Settings className="h-4 w-4 mr-2" />
            App Settings
          </Button>
        </CardContent>
      </Card>

      {/* Sign Out */}
      <Card className="border-destructive/20">
        <CardContent className="p-4">
          <Button 
            variant="destructive" 
            onClick={handleSignOut}
            className="w-full"
          >
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default UserProfile;