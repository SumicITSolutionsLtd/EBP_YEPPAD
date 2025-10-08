import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Users, TrendingUp, MapPin, Calendar, FileDown, Filter } from 'lucide-react';
import { supabase } from '@/integrations/supabase/client';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';

interface ImpactMetric {
  id: string;
  metric_type: string;
  metric_value: number;
  region: string;
  recorded_date: string;
  filters: any;
}

interface DashboardStats {
  totalYouth: number;
  ruralYouth: number;
  pwdYouth: number;
  femaleYouth: number;
  jobPlacements: number;
  skillsListed: number;
  mentorshipSessions: number;
  ussdInteractions: number;
}

const NGODashboard = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalYouth: 0,
    ruralYouth: 0,
    pwdYouth: 0,
    femaleYouth: 0,
    jobPlacements: 0,
    skillsListed: 0,
    mentorshipSessions: 0,
    ussdInteractions: 0
  });
  const [metrics, setMetrics] = useState<ImpactMetric[]>([]);
  const [loading, setLoading] = useState(true);
  const [regionFilter, setRegionFilter] = useState<string>('all');
  const [timeFilter, setTimeFilter] = useState<string>('30');

  const { user } = useAuth();
  const { toast } = useToast();

  useEffect(() => {
    if (user) {
      fetchDashboardData();
      fetchImpactMetrics();
    }
  }, [user, regionFilter, timeFilter]);

  const fetchDashboardData = async () => {
    try {
      // Get user demographics
      const { data: profiles } = await supabase
        .from('profiles')
        .select('*');

      if (profiles) {
        const totalYouth = profiles.length;
        const ruralYouth = profiles.filter(p => p.is_rural).length;
        const pwdYouth = profiles.filter(p => p.is_pwd).length;
        const femaleYouth = profiles.filter(p => p.gender === 'female').length;

        // Get job applications (as proxy for placements)
        const { count: jobPlacements } = await supabase
          .from('applications')
          .select('*', { count: 'exact', head: true })
          .eq('status', 'accepted');

        // Get skills count
        const { count: skillsListed } = await supabase
          .from('skills')
          .select('*', { count: 'exact', head: true });

        // Get mentorship sessions
        const { count: mentorshipSessions } = await supabase
          .from('mentorship_sessions')
          .select('*', { count: 'exact', head: true });

        // Get USSD interactions
        const { count: ussdInteractions } = await supabase
          .from('ussd_interactions')
          .select('*', { count: 'exact', head: true });

        setStats({
          totalYouth,
          ruralYouth,
          pwdYouth,
          femaleYouth,
          jobPlacements: jobPlacements || 0,
          skillsListed: skillsListed || 0,
          mentorshipSessions: mentorshipSessions || 0,
          ussdInteractions: ussdInteractions || 0
        });
      }
    } catch (error: any) {
      toast({ title: "Error loading dashboard data", description: error.message, variant: "destructive" });
    }
  };

  const fetchImpactMetrics = async () => {
    try {
      let query = supabase
        .from('impact_metrics')
        .select('*')
        .order('recorded_date', { ascending: false });

      if (regionFilter !== 'all') {
        query = query.eq('region', regionFilter);
      }

      const daysAgo = new Date();
      daysAgo.setDate(daysAgo.getDate() - parseInt(timeFilter));
      query = query.gte('recorded_date', daysAgo.toISOString().split('T')[0]);

      const { data, error } = await query;

      if (error) throw error;
      setMetrics(data || []);
    } catch (error: any) {
      toast({ title: "Error loading metrics", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const exportData = async () => {
    try {
      // Export basic CSV format
      const csvData = [
        ['Metric', 'Value', 'Region', 'Date'],
        ['Total Youth', stats.totalYouth, 'All', new Date().toISOString().split('T')[0]],
        ['Rural Youth', stats.ruralYouth, 'All', new Date().toISOString().split('T')[0]],
        ['PWD Youth', stats.pwdYouth, 'All', new Date().toISOString().split('T')[0]],
        ['Female Youth', stats.femaleYouth, 'All', new Date().toISOString().split('T')[0]],
        ['Job Placements', stats.jobPlacements, 'All', new Date().toISOString().split('T')[0]],
        ['Skills Listed', stats.skillsListed, 'All', new Date().toISOString().split('T')[0]],
        ['Mentorship Sessions', stats.mentorshipSessions, 'All', new Date().toISOString().split('T')[0]],
        ['USSD Interactions', stats.ussdInteractions, 'All', new Date().toISOString().split('T')[0]]
      ];

      const csvContent = csvData.map(row => row.join(',')).join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `luma-link-impact-${new Date().toISOString().split('T')[0]}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);

      toast({ title: "Data exported successfully!" });
    } catch (error: any) {
      toast({ title: "Error exporting data", description: error.message, variant: "destructive" });
    }
  };

  const inclusionPercentage = stats.totalYouth > 0 
    ? Math.round(((stats.ruralYouth + stats.pwdYouth + stats.femaleYouth) / (stats.totalYouth * 3)) * 100)
    : 0;

  if (loading) {
    return (
      <div className="p-4 space-y-4">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-muted rounded w-1/3"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-32 bg-muted rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 pb-20 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Impact Dashboard</h1>
          <p className="text-muted-foreground">Monitor youth inclusion and program effectiveness</p>
        </div>
        
        <div className="flex flex-wrap gap-2">
          <Select value={regionFilter} onValueChange={setRegionFilter}>
            <SelectTrigger className="w-32">
              <Filter className="h-4 w-4 mr-2" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Regions</SelectItem>
              <SelectItem value="Arua">Arua</SelectItem>
              <SelectItem value="Yumbe">Yumbe</SelectItem>
              <SelectItem value="Nebbi">Nebbi</SelectItem>
              <SelectItem value="Adjumani">Adjumani</SelectItem>
            </SelectContent>
          </Select>
          
          <Select value={timeFilter} onValueChange={setTimeFilter}>
            <SelectTrigger className="w-28">
              <Calendar className="h-4 w-4 mr-2" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">7 days</SelectItem>
              <SelectItem value="30">30 days</SelectItem>
              <SelectItem value="90">90 days</SelectItem>
              <SelectItem value="365">1 year</SelectItem>
            </SelectContent>
          </Select>
          
          <Button onClick={exportData} variant="outline" size="sm">
            <FileDown className="h-4 w-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium flex items-center">
              <Users className="h-4 w-4 mr-2 text-primary" />
              Total Youth
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{stats.totalYouth}</div>
            <p className="text-xs text-muted-foreground">Registered users</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium flex items-center">
              <MapPin className="h-4 w-4 mr-2 text-accent" />
              Rural Youth
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-accent">{stats.ruralYouth}</div>
            <p className="text-xs text-muted-foreground">
              {stats.totalYouth > 0 ? Math.round((stats.ruralYouth / stats.totalYouth) * 100) : 0}% of total
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">PWD Youth</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-trust">{stats.pwdYouth}</div>
            <p className="text-xs text-muted-foreground">
              {stats.totalYouth > 0 ? Math.round((stats.pwdYouth / stats.totalYouth) * 100) : 0}% of total
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Female Youth</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-growth">{stats.femaleYouth}</div>
            <p className="text-xs text-muted-foreground">
              {stats.totalYouth > 0 ? Math.round((stats.femaleYouth / stats.totalYouth) * 100) : 0}% of total
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Inclusion Progress */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <TrendingUp className="h-5 w-5 mr-2 text-primary" />
            Inclusion Progress
          </CardTitle>
          <CardDescription>
            Overall inclusion score across rural, PWD, and female demographics
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span>Overall Inclusion Score</span>
              <span className="font-medium">{inclusionPercentage}%</span>
            </div>
            <Progress value={inclusionPercentage} className="h-2" />
          </div>
          
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-sm text-muted-foreground">Rural</p>
              <p className="text-lg font-bold text-accent">
                {stats.totalYouth > 0 ? Math.round((stats.ruralYouth / stats.totalYouth) * 100) : 0}%
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">PWD</p>
              <p className="text-lg font-bold text-trust">
                {stats.totalYouth > 0 ? Math.round((stats.pwdYouth / stats.totalYouth) * 100) : 0}%
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Female</p>
              <p className="text-lg font-bold text-growth">
                {stats.totalYouth > 0 ? Math.round((stats.femaleYouth / stats.totalYouth) * 100) : 0}%
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Program Effectiveness */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Job Placements</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{stats.jobPlacements}</div>
            <p className="text-xs text-muted-foreground">Successful applications</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Skills Listed</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-accent">{stats.skillsListed}</div>
            <p className="text-xs text-muted-foreground">Available talents</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Mentorship</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-trust">{stats.mentorshipSessions}</div>
            <p className="text-xs text-muted-foreground">Questions asked</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">USSD Access</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-growth">{stats.ussdInteractions}</div>
            <p className="text-xs text-muted-foreground">Basic phone interactions</p>
          </CardContent>
        </Card>
      </div>

      {/* Regional Breakdown */}
      <Card>
        <CardHeader>
          <CardTitle>Regional Impact</CardTitle>
          <CardDescription>
            Youth engagement across different regions
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {['Arua', 'Yumbe', 'Nebbi', 'Adjumani'].map((region) => {
              const regionCount = Math.floor(Math.random() * 50) + 20; // Mock data
              const percentage = Math.round((regionCount / stats.totalYouth) * 100) || 0;
              
              return (
                <div key={region} className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="font-medium">{region}</span>
                    <span>{regionCount} youth ({percentage}%)</span>
                  </div>
                  <Progress value={percentage} className="h-2" />
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start">
            <FileDown className="h-4 w-4 mr-2" />
            Generate Detailed Report
          </Button>
          <Button variant="outline" className="w-full justify-start">
            <Users className="h-4 w-4 mr-2" />
            View Individual Profiles
          </Button>
          <Button variant="outline" className="w-full justify-start">
            <TrendingUp className="h-4 w-4 mr-2" />
            Set Impact Targets
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};

export default NGODashboard;