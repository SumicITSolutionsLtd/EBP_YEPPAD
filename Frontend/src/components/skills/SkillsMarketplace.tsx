import { useState, useEffect } from 'react';
import { Plus, Search, Filter, Star, MapPin, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { supabase } from '@/integrations/supabase/client';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';

interface Skill {
  id: string;
  skill_name: string;
  skill_category: string;
  description: string;
  experience_years: number;
  hourly_rate: number;
  is_available: boolean;
  profiles: {
    full_name: string;
    location: string;
  };
}

const SkillsMarketplace = () => {
  const [skills, setSkills] = useState<Skill[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [loading, setLoading] = useState(true);
  const [isAddingSkill, setIsAddingSkill] = useState(false);
  const [newSkill, setNewSkill] = useState({
    skill_name: '',
    skill_category: '',
    description: '',
    experience_years: 0,
    hourly_rate: 0
  });

  const { user } = useAuth();
  const { toast } = useToast();

  const categories = [
    'all', 'tailoring', 'welding', 'crafts', 'carpentry', 'plumbing', 
    'electrical', 'cooking', 'farming', 'mechanics', 'beauty', 'other'
  ];

  useEffect(() => {
    fetchSkills();
  }, []);

  const fetchSkills = async () => {
    try {
      const { data, error } = await supabase
        .from('skills')
        .select(`
          id,
          skill_name,
          skill_category,
          description,
          experience_years,
          hourly_rate,
          is_available,
          profiles (
            full_name,
            location
          )
        `)
        .eq('is_available', true)
        .order('created_at', { ascending: false });

      if (error) throw error;
      setSkills((data as any) || []);
    } catch (error: any) {
      toast({ title: "Error loading skills", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const addSkill = async () => {
    if (!user) return;

    try {
      setIsAddingSkill(true);
      const { error } = await supabase
        .from('skills')
        .insert([{
          ...newSkill,
          user_id: user.id
        }]);

      if (error) throw error;

      toast({ title: "Skill added successfully!" });
      setNewSkill({
        skill_name: '',
        skill_category: '',
        description: '',
        experience_years: 0,
        hourly_rate: 0
      });
      fetchSkills();
    } catch (error: any) {
      toast({ title: "Error adding skill", description: error.message, variant: "destructive" });
    } finally {
      setIsAddingSkill(false);
    }
  };

  const filteredSkills = skills.filter(skill => {
    const matchesSearch = skill.skill_name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         skill.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || skill.skill_category === selectedCategory;
    return matchesSearch && matchesCategory;
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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Skills Marketplace</h1>
          <p className="text-muted-foreground">Showcase your talents & find skilled workers</p>
        </div>
        
        {user && (
          <Dialog>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Add Skill
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Add Your Skill</DialogTitle>
                <DialogDescription>
                  Share your talents with the community
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="skill_name">Skill Name</Label>
                  <Input
                    id="skill_name"
                    value={newSkill.skill_name}
                    onChange={(e) => setNewSkill({ ...newSkill, skill_name: e.target.value })}
                    placeholder="e.g., Custom Tailoring"
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="category">Category</Label>
                  <Select 
                    value={newSkill.skill_category} 
                    onValueChange={(value) => setNewSkill({ ...newSkill, skill_category: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.filter(cat => cat !== 'all').map((category) => (
                        <SelectItem key={category} value={category}>
                          {category.charAt(0).toUpperCase() + category.slice(1)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={newSkill.description}
                    onChange={(e) => setNewSkill({ ...newSkill, description: e.target.value })}
                    placeholder="Describe your skill and experience..."
                    rows={3}
                  />
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="experience">Years of Experience</Label>
                    <Input
                      id="experience"
                      type="number"
                      value={newSkill.experience_years}
                      onChange={(e) => setNewSkill({ ...newSkill, experience_years: parseInt(e.target.value) || 0 })}
                      min="0"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="rate">Hourly Rate (UGX)</Label>
                    <Input
                      id="rate"
                      type="number"
                      value={newSkill.hourly_rate}
                      onChange={(e) => setNewSkill({ ...newSkill, hourly_rate: parseFloat(e.target.value) || 0 })}
                      min="0"
                      step="1000"
                    />
                  </div>
                </div>
                
                <Button onClick={addSkill} disabled={isAddingSkill} className="w-full">
                  {isAddingSkill ? 'Adding...' : 'Add Skill'}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {/* Search and Filters */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search skills..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
        
        <div className="flex gap-2 overflow-x-auto pb-2">
          {categories.map((category) => (
            <Button
              key={category}
              variant={selectedCategory === category ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedCategory(category)}
              className="whitespace-nowrap"
            >
              {category.charAt(0).toUpperCase() + category.slice(1)}
            </Button>
          ))}
        </div>
      </div>

      {/* Skills Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredSkills.map((skill) => (
          <Card key={skill.id} className="hover:shadow-md transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <Avatar className="h-10 w-10">
                    <AvatarFallback>
                      {skill.profiles?.full_name?.charAt(0) || 'U'}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <CardTitle className="text-lg">{skill.skill_name}</CardTitle>
                    <CardDescription className="flex items-center">
                      <MapPin className="h-3 w-3 mr-1" />
                      {skill.profiles?.location || 'Location not specified'}
                    </CardDescription>
                  </div>
                </div>
                <Badge variant="secondary">{skill.skill_category}</Badge>
              </div>
            </CardHeader>
            
            <CardContent className="space-y-3">
              <p className="text-sm text-muted-foreground line-clamp-2">
                {skill.description}
              </p>
              
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center text-muted-foreground">
                  <Clock className="h-3 w-3 mr-1" />
                  {skill.experience_years} years exp.
                </div>
                <div className="font-semibold text-primary">
                  UGX {skill.hourly_rate.toLocaleString()}/hr
                </div>
              </div>
              
              <div className="flex gap-2">
                <Button size="sm" className="flex-1">Contact</Button>
                <Button size="sm" variant="outline">View Profile</Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredSkills.length === 0 && (
        <Card className="p-8 text-center">
          <p className="text-muted-foreground">No skills found matching your criteria.</p>
          {user && (
            <p className="text-sm text-muted-foreground mt-2">
              Be the first to add a skill in this category!
            </p>
          )}
        </Card>
      )}
    </div>
  );
};

export default SkillsMarketplace;