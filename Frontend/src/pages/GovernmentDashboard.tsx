import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent } from '@/components/ui/dialog';
import { BarChart3, Users, TrendingUp, MapPin, Calendar, FileDown, Filter, Globe, Smartphone, ArrowLeft, LogIn } from 'lucide-react';
import { supabase } from '@/integrations/supabase/client';
import { useLanguage } from '@/contexts/LanguageContext';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';
import USSDAuthForm from '@/components/auth/USSDAuthForm';

interface PolicyMetric {
  category: string;
  target: number;
  current: number;
  trend: 'up' | 'down' | 'stable';
}

const GovernmentDashboard = () => {
  const [showAuth, setShowAuth] = useState(false);
  const [policyMetrics, setPolicyMetrics] = useState<PolicyMetric[]>([
    { category: 'Youth Employment', target: 75, current: 68, trend: 'up' },
    { category: 'Rural Inclusion', target: 60, current: 45, trend: 'up' },
    { category: 'PWD Participation', target: 25, current: 18, trend: 'stable' },
    { category: 'Gender Parity', target: 50, current: 52, trend: 'up' },
    { category: 'Digital Access', target: 80, current: 72, trend: 'up' },
    { category: 'Skills Training', target: 90, current: 85, trend: 'up' }
  ]);

  const [regionalData, setRegionalData] = useState([
    { region: 'Arua', youth: 1250, employed: 380, rural: 85, pwd: 12 },
    { region: 'Yumbe', youth: 890, employed: 245, rural: 92, pwd: 8 },
    { region: 'Nebbi', youth: 1100, employed: 420, rural: 78, pwd: 15 },
    { region: 'Adjumani', youth: 670, employed: 185, rural: 88, pwd: 6 }
  ]);

  const [loading, setLoading] = useState(true);
  const [timeFilter, setTimeFilter] = useState<string>('30');

  const { user } = useAuth();
  const { toast } = useToast();
  const { t } = useLanguage();

  useEffect(() => {
    if (user) {
      fetchGovernmentData();
    }
  }, [user, timeFilter]);

  const fetchGovernmentData = async () => {
    try {
      // Fetch aggregated statistics
      const { data: profiles } = await supabase
        .from('profiles')
        .select('*');

      if (profiles) {
        // Update regional data with real statistics
        const regions = ['Arua', 'Yumbe', 'Nebbi', 'Adjumani'];
        const updatedRegionalData = regions.map(region => {
          const regionProfiles = profiles.filter(p => p.location?.includes(region));
          return {
            region,
            youth: regionProfiles.length,
            employed: Math.floor(regionProfiles.length * 0.3), // Mock employment rate
            rural: regionProfiles.filter(p => p.is_rural).length,
            pwd: regionProfiles.filter(p => p.is_pwd).length
          };
        });
        setRegionalData(updatedRegionalData);
      }

      // Fetch job market data
      const { count: totalJobs } = await supabase
        .from('opportunities')
        .select('*', { count: 'exact', head: true })
        .eq('status', 'active');

      const { count: applications } = await supabase
        .from('applications')
        .select('*', { count: 'exact', head: true });

      // Update policy metrics with real data
      const totalYouth = profiles?.length || 0;
      const employmentRate = totalJobs && totalYouth > 0 ? Math.min((totalJobs / totalYouth) * 100, 100) : 0;
      
      setPolicyMetrics(prev => prev.map(metric => {
        if (metric.category === 'Youth Employment') {
          return { ...metric, current: Math.round(employmentRate) };
        }
        return metric;
      }));

    } catch (error: any) {
      toast({ title: "Error loading government data", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const totalYouth = regionalData.reduce((sum, region) => sum + region.youth, 0);
  const totalEmployed = regionalData.reduce((sum, region) => sum + region.employed, 0);
  const totalRural = regionalData.reduce((sum, region) => sum + region.rural, 0);
  const totalPwd = regionalData.reduce((sum, region) => sum + region.pwd, 0);

  const employmentRate = totalYouth > 0 ? Math.round((totalEmployed / totalYouth) * 100) : 0;
  const ruralRate = totalYouth > 0 ? Math.round((totalRural / totalYouth) * 100) : 0;
  const pwdRate = totalYouth > 0 ? Math.round((totalPwd / totalYouth) * 100) : 0;

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
        <div className="flex items-center space-x-3">
          <Button variant="ghost" onClick={() => window.location.href = '/'}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            {t('backToHome')}
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-foreground">{t('governmentDashboard')}</h1>
            <p className="text-muted-foreground">{t('nationalYouthDevelopment')}</p>
          </div>
          {!user && (
            <Button 
              variant="hero"
              onClick={() => setShowAuth(true)}
            >
              <LogIn className="h-4 w-4 mr-2" />
              {t('signInForFullAccess')}
            </Button>
          )}
        </div>
        
        <div className="flex flex-wrap gap-2">
          <Select value={timeFilter} onValueChange={setTimeFilter}>
            <SelectTrigger className="w-32">
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
          
          <Button variant="outline" size="sm">
            <FileDown className="h-4 w-4 mr-2" />
            Export Report
          </Button>
        </div>
      </div>

      {/* National Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium flex items-center">
              <Users className="h-4 w-4 mr-2 text-primary" />
              Total Youth
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{totalYouth.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">Registered on platform</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium flex items-center">
              <BarChart3 className="h-4 w-4 mr-2 text-accent" />
              Employment Rate
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-accent">{employmentRate}%</div>
            <p className="text-xs text-muted-foreground">{totalEmployed.toLocaleString()} employed</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium flex items-center">
              <MapPin className="h-4 w-4 mr-2 text-trust" />
              Rural Inclusion
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-trust">{ruralRate}%</div>
            <p className="text-xs text-muted-foreground">{totalRural.toLocaleString()} rural youth</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">PWD Participation</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-growth">{pwdRate}%</div>
            <p className="text-xs text-muted-foreground">{totalPwd.toLocaleString()} PWD youth</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="policy" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="policy">Policy Targets</TabsTrigger>
          <TabsTrigger value="regional">Regional Analysis</TabsTrigger>
          <TabsTrigger value="digital">Digital Access</TabsTrigger>
        </TabsList>

        <TabsContent value="policy" className="space-y-4">
          {/* Policy Targets */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <TrendingUp className="h-5 w-5 mr-2 text-primary" />
                National Policy Targets vs Current Performance
              </CardTitle>
              <CardDescription>
                Progress towards national youth development goals
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {policyMetrics.map((metric) => (
                <div key={metric.category} className="space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="font-medium">{metric.category}</span>
                    <div className="flex items-center gap-2">
                      <Badge variant={metric.current >= metric.target ? "default" : "secondary"}>
                        {metric.current}% / {metric.target}%
                      </Badge>
                      <div className={`w-2 h-2 rounded-full ${
                        metric.trend === 'up' ? 'bg-green-500' : 
                        metric.trend === 'down' ? 'bg-red-500' : 'bg-yellow-500'
                      }`}></div>
                    </div>
                  </div>
                  <Progress value={(metric.current / metric.target) * 100} className="h-2" />
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="regional" className="space-y-4">
          {/* Regional Breakdown */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <MapPin className="h-5 w-5 mr-2 text-primary" />
                Regional Performance Dashboard
              </CardTitle>
              <CardDescription>
                Youth development metrics by district
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {regionalData.map((region) => (
                  <Card key={region.region} className="p-4">
                    <div className="flex justify-between items-center mb-3">
                      <h3 className="font-semibold text-lg">{region.region} District</h3>
                      <Badge variant="outline">{region.youth.toLocaleString()} youth</Badge>
                    </div>
                    
                    <div className="grid grid-cols-3 gap-4 text-center">
                      <div>
                        <p className="text-sm text-muted-foreground">Employment</p>
                        <p className="text-xl font-bold text-accent">
                          {Math.round((region.employed / region.youth) * 100)}%
                        </p>
                        <p className="text-xs text-muted-foreground">{region.employed} employed</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Rural</p>
                        <p className="text-xl font-bold text-trust">
                          {Math.round((region.rural / region.youth) * 100)}%
                        </p>
                        <p className="text-xs text-muted-foreground">{region.rural} rural</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">PWD</p>
                        <p className="text-xl font-bold text-growth">
                          {Math.round((region.pwd / region.youth) * 100)}%
                        </p>
                        <p className="text-xs text-muted-foreground">{region.pwd} PWD</p>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="digital" className="space-y-4">
          {/* Digital Access Analysis */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <Globe className="h-5 w-5 mr-2 text-primary" />
                  Digital Platform Usage
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Web App Access</span>
                    <span className="font-medium">85%</span>
                  </div>
                  <Progress value={85} className="h-2" />
                </div>
                
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Mobile App Usage</span>
                    <span className="font-medium">72%</span>
                  </div>
                  <Progress value={72} className="h-2" />
                </div>
                
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>Multi-language Access</span>
                    <span className="font-medium">68%</span>
                  </div>
                  <Progress value={68} className="h-2" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <Smartphone className="h-5 w-5 mr-2 text-primary" />
                  USSD/SMS Access
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-primary">15%</div>
                  <p className="text-sm text-muted-foreground">Basic phone users</p>
                </div>
                
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-sm">USSD Menu Access</span>
                    <span className="text-sm font-medium">235 sessions</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm">SMS Job Posts</span>
                    <span className="text-sm font-medium">58 posts</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm">SMS Applications</span>
                    <span className="text-sm font-medium">142 applications</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>

      {/* Policy Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle>Policy Recommendations</CardTitle>
          <CardDescription>
            AI-generated insights based on current data trends
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="p-3 border-l-4 border-primary bg-primary/5 rounded">
            <p className="text-sm font-medium">Rural Employment Initiative</p>
            <p className="text-xs text-muted-foreground">
              Rural employment lags at {ruralRate}%. Consider targeted rural job creation programs.
            </p>
          </div>
          
          <div className="p-3 border-l-4 border-accent bg-accent/5 rounded">
            <p className="text-sm font-medium">PWD Inclusion Programs</p>
            <p className="text-xs text-muted-foreground">
              PWD participation at {pwdRate}%. Recommend accessibility improvements and targeted support.
            </p>
          </div>
          
          <div className="p-3 border-l-4 border-trust bg-trust/5 rounded">
            <p className="text-sm font-medium">Digital Infrastructure</p>
            <p className="text-xs text-muted-foreground">
              15% still rely on USSD/SMS. Continue investing in mobile data accessibility.
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Auth Dialog */}
      <Dialog open={showAuth} onOpenChange={setShowAuth}>
        <DialogContent className="sm:max-w-md">
          <USSDAuthForm onClose={() => setShowAuth(false)} />
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default GovernmentDashboard;