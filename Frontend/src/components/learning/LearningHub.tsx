import { useState, useEffect } from 'react';
import { Play, Pause, BookOpen, Trophy, Clock, CheckCircle, Mic, Volume2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { supabase } from '@/integrations/supabase/client';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/hooks/use-toast';

interface BusinessTool {
  id: string;
  title: string;
  description: string;
  type: string;
  duration: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  modules: number;
  progress?: number;
}

const LearningHub = () => {
  const [selectedLanguage, setSelectedLanguage] = useState('English');
  const [selectedTool, setSelectedTool] = useState<string | null>(null);
  const [userProgress, setUserProgress] = useState<Record<string, number>>({});
  const [isPlaying, setIsPlaying] = useState<string | null>(null);

  const { user } = useAuth();
  const { toast } = useToast();

  const businessTools: BusinessTool[] = [
    {
      id: 'idea_validation',
      title: 'Business Idea Validation',
      description: 'Learn how to test and validate your business idea before investing time and money',
      type: 'idea_validation',
      duration: '45 min',
      difficulty: 'beginner',
      modules: 6
    },
    {
      id: 'business_plan',
      title: 'Business Planning',
      description: 'Create a comprehensive business plan with financial projections',
      type: 'business_plan',
      duration: '2 hours',
      difficulty: 'intermediate',
      modules: 8
    },
    {
      id: 'savings_guide',
      title: 'Savings & Financial Management',
      description: 'Learn money management, budgeting, and saving strategies',
      type: 'savings_guide',
      duration: '1 hour',
      difficulty: 'beginner',
      modules: 5
    },
    {
      id: 'market_research',
      title: 'Market Research',
      description: 'Understand your customers and competition',
      type: 'market_research',
      duration: '1.5 hours',
      difficulty: 'intermediate',
      modules: 7
    }
  ];

  const voiceLearningModules = [
    { 
      title: 'Business Planning Basics', 
      titleLugbara: 'Okwanya ku Bisnis', 
      titleAlur: 'Limo Adongo',
      language: selectedLanguage,
      duration: '15 min',
      type: 'audio'
    },
    { 
      title: 'Customer Service', 
      titleLugbara: 'Ayoyo Ayikani', 
      titleAlur: 'Tic pa Lulupunu',
      language: selectedLanguage,
      duration: '12 min',
      type: 'audio'
    },
    { 
      title: 'Financial Literacy', 
      titleLugbara: 'Opi pa Pesa', 
      titleAlur: 'Winyo pa Pesa',
      language: selectedLanguage,
      duration: '18 min',
      type: 'audio'
    }
  ];

  useEffect(() => {
    if (user) {
      fetchUserProgress();
    }
  }, [user]);

  const fetchUserProgress = async () => {
    if (!user) return;

    try {
      const { data, error } = await supabase
        .from('business_progress')
        .select('tool_type, completion_percentage')
        .eq('user_id', user.id);

      if (error) throw error;

      const progressMap = data.reduce((acc, item) => {
        acc[item.tool_type] = item.completion_percentage;
        return acc;
      }, {} as Record<string, number>);

      setUserProgress(progressMap);
    } catch (error: any) {
      console.error('Error fetching progress:', error);
    }
  };

  const updateProgress = async (toolType: string, progress: number) => {
    if (!user) return;

    try {
      const { error } = await supabase
        .from('business_progress')
        .upsert({
          user_id: user.id,
          tool_type: toolType,
          completion_percentage: progress,
          progress_data: {},
          ...(progress === 100 && { completed_at: new Date().toISOString() })
        });

      if (error) throw error;

      setUserProgress(prev => ({ ...prev, [toolType]: progress }));
      
      if (progress === 100) {
        toast({ title: "Congratulations! Course completed!" });
      }
    } catch (error: any) {
      toast({ title: "Error updating progress", description: error.message, variant: "destructive" });
    }
  };

  const startLearning = (toolId: string) => {
    setSelectedTool(toolId);
    // Simulate starting a module - in real app this would navigate to content
    const currentProgress = userProgress[toolId] || 0;
    const newProgress = Math.min(currentProgress + 20, 100);
    updateProgress(toolId, newProgress);
  };

  const getDifficultyColor = (difficulty: string) => {
    const colors = {
      beginner: 'bg-growth text-white',
      intermediate: 'bg-energy text-white',
      advanced: 'bg-accent text-accent-foreground'
    };
    return colors[difficulty as keyof typeof colors] || 'bg-secondary text-secondary-foreground';
  };

  const getModuleTitle = (module: any) => {
    switch (selectedLanguage) {
      case 'Lugbara':
        return module.titleLugbara || module.title;
      case 'Alur':
        return module.titleAlur || module.title;
      default:
        return module.title;
    }
  };

  return (
    <div className="p-4 pb-20 space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-foreground">Learning Hub</h1>
        <p className="text-muted-foreground">Build business skills & knowledge</p>
      </div>

      {/* Language Selector */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Volume2 className="h-5 w-5 text-primary" />
              <span className="font-medium">Learning Language</span>
            </div>
            <Select value={selectedLanguage} onValueChange={setSelectedLanguage}>
              <SelectTrigger className="w-40">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="English">English</SelectItem>
                <SelectItem value="Lugbara">Lugbara</SelectItem>
                <SelectItem value="Alur">Alur</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Business Literacy Tools */}
      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-foreground">Business Literacy Tools</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {businessTools.map((tool) => {
            const progress = userProgress[tool.type] || 0;
            const isCompleted = progress === 100;
            
            return (
              <Card key={tool.id} className="hover:shadow-md transition-shadow">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <Badge className={getDifficultyColor(tool.difficulty)}>
                          {tool.difficulty}
                        </Badge>
                        {isCompleted && (
                          <Badge variant="outline" className="text-green-600 border-green-600">
                            <CheckCircle className="h-3 w-3 mr-1" />
                            Completed
                          </Badge>
                        )}
                      </div>
                      <CardTitle className="text-lg">{tool.title}</CardTitle>
                      <CardDescription className="mt-1">
                        {tool.description}
                      </CardDescription>
                    </div>
                  </div>
                </CardHeader>
                
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between text-sm text-muted-foreground">
                    <div className="flex items-center">
                      <Clock className="h-3 w-3 mr-1" />
                      {tool.duration}
                    </div>
                    <div className="flex items-center">
                      <BookOpen className="h-3 w-3 mr-1" />
                      {tool.modules} modules
                    </div>
                  </div>
                  
                  {progress > 0 && (
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Progress</span>
                        <span>{progress}%</span>
                      </div>
                      <Progress value={progress} className="h-2" />
                    </div>
                  )}
                  
                  <Button 
                    onClick={() => startLearning(tool.type)}
                    className="w-full"
                    variant={isCompleted ? "outline" : "default"}
                  >
                    {progress === 0 ? 'Start Learning' : isCompleted ? 'Review' : 'Continue'}
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Voice Learning Modules */}
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <Mic className="h-5 w-5 text-primary" />
          <h2 className="text-xl font-semibold text-foreground">Voice Learning Modules</h2>
        </div>
        
        <div className="space-y-3">
          {voiceLearningModules.map((module, index) => (
            <Card key={index} className="hover:shadow-sm transition-shadow">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <h3 className="font-medium">{getModuleTitle(module)}</h3>
                    <p className="text-sm text-muted-foreground">
                      {selectedLanguage} • {module.duration}
                    </p>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setIsPlaying(isPlaying === `voice-${index}` ? null : `voice-${index}`)}
                    >
                      {isPlaying === `voice-${index}` ? (
                        <Pause className="h-3 w-3" />
                      ) : (
                        <Play className="h-3 w-3" />
                      )}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Achievement Summary */}
      {user && Object.keys(userProgress).length > 0 && (
        <Card className="border-growth/50 bg-growth/5">
          <CardHeader>
            <CardTitle className="flex items-center text-growth">
              <Trophy className="h-5 w-5 mr-2" />
              Your Learning Progress
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-4 text-center">
              <div>
                <p className="text-2xl font-bold text-growth">
                  {Object.values(userProgress).filter(p => p === 100).length}
                </p>
                <p className="text-sm text-muted-foreground">Courses Completed</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-growth">
                  {Math.round(Object.values(userProgress).reduce((a, b) => a + b, 0) / Object.values(userProgress).length || 0)}%
                </p>
                <p className="text-sm text-muted-foreground">Average Progress</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Offline Learning */}
      <Card className="border-accent/50 bg-accent/5">
        <CardContent className="p-4">
          <div className="text-center">
            <div className="w-8 h-8 bg-accent rounded-full flex items-center justify-center mx-auto mb-2">
              <BookOpen className="h-4 w-4 text-white" />
            </div>
            <p className="text-sm font-medium text-accent">Offline Learning Ready</p>
            <p className="text-xs text-muted-foreground mt-1">
              All courses available without internet connection
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default LearningHub;