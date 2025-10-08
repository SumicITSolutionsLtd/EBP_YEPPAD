import { useState, useEffect } from 'react';
import { Search, Filter, MapPin, Clock, DollarSign, Bookmark, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { supabase } from '@/integrations/supabase/client';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';

interface Opportunity {
  id: string;
  title: string;
  description: string;
  type: string;
  category: string;
  location: string;
  salary_min: number;
  salary_max: number;
  requirements: string[];
  skills_needed: string[];
  deadline: string;
  is_remote: boolean;
  contact_method: string;
  created_at: string;
  profiles: {
    full_name: string;
  };
}

const JobsBoard = () => {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('all');
  const [selectedLocation, setSelectedLocation] = useState('all');
  const [loading, setLoading] = useState(true);
  const [applicationMessage, setApplicationMessage] = useState('');
  const [selectedOpportunity, setSelectedOpportunity] = useState<string | null>(null);
  const [applying, setApplying] = useState(false);

  const { user } = useAuth();
  const { toast } = useToast();

  const types = ['all', 'job', 'contract', 'training', 'loan', 'grant'];

  useEffect(() => {
    fetchOpportunities();
  }, []);

  const fetchOpportunities = async () => {
    try {
      const { data, error } = await supabase
        .from('opportunities')
        .select(`
          id,
          title,
          description,
          type,
          category,
          location,
          salary_min,
          salary_max,
          requirements,
          skills_needed,
          deadline,
          is_remote,
          contact_method,
          created_at,
          profiles (
            full_name
          )
        `)
        .eq('status', 'active')
        .order('created_at', { ascending: false });

      if (error) throw error;
      setOpportunities((data as any) || []);
    } catch (error: any) {
      toast({ title: "Error loading opportunities", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const applyToOpportunity = async (opportunityId: string) => {
    if (!user) return;

    try {
      setApplying(true);
      const { error } = await supabase
        .from('applications')
        .insert([{
          opportunity_id: opportunityId,
          applicant_id: user.id,
          message: applicationMessage
        }]);

      if (error) throw error;

      toast({ title: "Application submitted successfully!" });
      setApplicationMessage('');
      setSelectedOpportunity(null);
    } catch (error: any) {
      toast({ title: "Error submitting application", description: error.message, variant: "destructive" });
    } finally {
      setApplying(false);
    }
  };

  const getTypeColor = (type: string) => {
    const colors = {
      job: 'bg-primary text-primary-foreground',
      contract: 'bg-accent text-accent-foreground',
      training: 'bg-trust text-white',
      loan: 'bg-growth text-white',
      grant: 'bg-energy text-white'
    };
    return colors[type as keyof typeof colors] || 'bg-secondary text-secondary-foreground';
  };

  const filteredOpportunities = opportunities.filter(opp => {
    const matchesSearch = opp.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         opp.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesType = selectedType === 'all' || opp.type === selectedType;
    const matchesLocation = selectedLocation === 'all' || 
                           opp.location.toLowerCase().includes(selectedLocation.toLowerCase()) ||
                           opp.is_remote;
    return matchesSearch && matchesType && matchesLocation;
  });

  if (loading) {
    return (
      <div className="p-4 space-y-4">
        {[...Array(6)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardContent className="p-4">
              <div className="h-4 bg-muted rounded w-3/4 mb-2"></div>
              <div className="h-3 bg-muted rounded w-1/2"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="p-4 pb-20 space-y-4">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-foreground">Job Opportunities</h1>
        <p className="text-muted-foreground">Find jobs, contracts, training & funding</p>
      </div>

      {/* Search and Filters */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search opportunities..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
        
        <div className="grid grid-cols-2 gap-2">
          <Select value={selectedType} onValueChange={setSelectedType}>
            <SelectTrigger>
              <SelectValue placeholder="All Types" />
            </SelectTrigger>
            <SelectContent>
              {types.map((type) => (
                <SelectItem key={type} value={type}>
                  {type === 'all' ? 'All Types' : type.charAt(0).toUpperCase() + type.slice(1)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          
          <Select value={selectedLocation} onValueChange={setSelectedLocation}>
            <SelectTrigger>
              <SelectValue placeholder="All Locations" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Locations</SelectItem>
              <SelectItem value="arua">Arua</SelectItem>
              <SelectItem value="gulu">Gulu</SelectItem>
              <SelectItem value="lira">Lira</SelectItem>
              <SelectItem value="kitgum">Kitgum</SelectItem>
              <SelectItem value="remote">Remote</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Opportunities Grid */}
      <div className="space-y-4">
        {filteredOpportunities.map((opportunity) => (
          <Card key={opportunity.id} className="hover:shadow-md transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <Badge className={getTypeColor(opportunity.type)}>
                      {opportunity.type}
                    </Badge>
                    {opportunity.is_remote && (
                      <Badge variant="outline">Remote</Badge>
                    )}
                  </div>
                  <CardTitle className="text-lg">{opportunity.title}</CardTitle>
                  <CardDescription className="flex items-center mt-1">
                    <MapPin className="h-3 w-3 mr-1" />
                    {opportunity.location}
                  </CardDescription>
                </div>
                <Button variant="ghost" size="sm">
                  <Bookmark className="h-4 w-4" />
                </Button>
              </div>
            </CardHeader>
            
            <CardContent className="space-y-3">
              <p className="text-sm text-muted-foreground line-clamp-2">
                {opportunity.description}
              </p>
              
              {opportunity.skills_needed && opportunity.skills_needed.length > 0 && (
                <div className="flex flex-wrap gap-1">
                  {opportunity.skills_needed.slice(0, 3).map((skill, index) => (
                    <Badge key={index} variant="secondary" className="text-xs">
                      {skill}
                    </Badge>
                  ))}
                  {opportunity.skills_needed.length > 3 && (
                    <Badge variant="secondary" className="text-xs">
                      +{opportunity.skills_needed.length - 3} more
                    </Badge>
                  )}
                </div>
              )}
              
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center text-muted-foreground">
                  <Clock className="h-3 w-3 mr-1" />
                  {opportunity.deadline ? new Date(opportunity.deadline).toLocaleDateString() : 'Ongoing'}
                </div>
                {(opportunity.salary_min || opportunity.salary_max) && (
                  <div className="flex items-center font-semibold text-primary">
                    <DollarSign className="h-3 w-3 mr-1" />
                    {opportunity.salary_min && opportunity.salary_max
                      ? `${opportunity.salary_min.toLocaleString()} - ${opportunity.salary_max.toLocaleString()}`
                      : opportunity.salary_min?.toLocaleString() || opportunity.salary_max?.toLocaleString()
                    } UGX
                  </div>
                )}
              </div>
              
              <div className="flex gap-2">
                {user ? (
                  <Dialog>
                    <DialogTrigger asChild>
                      <Button 
                        size="sm" 
                        className="flex-1"
                        onClick={() => setSelectedOpportunity(opportunity.id)}
                      >
                        Apply Now
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Apply for {opportunity.title}</DialogTitle>
                        <DialogDescription>
                          Send a message with your application
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        <Textarea
                          placeholder="Tell the employer why you're the right fit for this opportunity..."
                          value={applicationMessage}
                          onChange={(e) => setApplicationMessage(e.target.value)}
                          rows={4}
                        />
                        <Button 
                          onClick={() => applyToOpportunity(opportunity.id)} 
                          disabled={applying}
                          className="w-full"
                        >
                          {applying ? 'Submitting...' : 'Submit Application'}
                        </Button>
                      </div>
                    </DialogContent>
                  </Dialog>
                ) : (
                  <Button size="sm" className="flex-1" variant="outline">
                    Sign in to Apply
                  </Button>
                )}
                
                {opportunity.contact_method === 'sms' && (
                  <Button size="sm" variant="outline">
                    <ExternalLink className="h-3 w-3 mr-1" />
                    SMS
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredOpportunities.length === 0 && (
        <Card className="p-8 text-center">
          <p className="text-muted-foreground">No opportunities found matching your criteria.</p>
          <p className="text-sm text-muted-foreground mt-2">
            Try adjusting your search filters or check back later for new opportunities.
          </p>
        </Card>
      )}

      {/* Offline Indicator */}
      <Card className="border-accent/50 bg-accent/5">
        <CardContent className="p-4">
          <div className="text-center">
            <div className="w-8 h-8 bg-accent rounded-full flex items-center justify-center mx-auto mb-2">
              <span className="text-xs text-white font-bold">📱</span>
            </div>
            <p className="text-sm font-medium text-accent">Jobs Cached for Offline</p>
            <p className="text-xs text-muted-foreground mt-1">
              {opportunities.length} opportunities available offline
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default JobsBoard;