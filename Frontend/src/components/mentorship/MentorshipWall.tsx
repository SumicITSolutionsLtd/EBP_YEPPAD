import { useState, useEffect } from 'react';
import { MessageCircle, Mic, Play, Pause, Plus, Filter, Heart, Reply } from 'lucide-react';
import { Button } from '@/components/ui/button';
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

interface MentorshipSession {
  id: string;
  question: string;
  question_audio_url: string | null;
  answer: string | null;
  answer_audio_url: string | null;
  category: string;
  status: string;
  created_at: string;
  answered_at: string | null;
  mentee_profile: {
    full_name: string;
    location: string;
  };
  mentor_profile?: {
    full_name: string;
    location: string;
  };
}

const MentorshipWall = () => {
  const [sessions, setSessions] = useState<MentorshipSession[]>([]);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [loading, setLoading] = useState(true);
  const [isAskingQuestion, setIsAskingQuestion] = useState(false);
  const [newQuestion, setNewQuestion] = useState({
    question: '',
    category: 'business'
  });
  const [isRecording, setIsRecording] = useState(false);
  const [playingAudio, setPlayingAudio] = useState<string | null>(null);

  const { user } = useAuth();
  const { toast } = useToast();

  const categories = [
    'all', 'business', 'skills', 'funding', 'marketing', 'legal', 'technology', 'farming', 'other'
  ];

  useEffect(() => {
    fetchSessions();
  }, []);

  const fetchSessions = async () => {
    try {
      const { data, error } = await supabase
        .from('mentorship_sessions')
        .select(`
          id,
          question,
          question_audio_url,
          answer,
          answer_audio_url,
          category,
          status,
          created_at,
          answered_at,
          mentee_profile:profiles!mentorship_sessions_mentee_id_fkey (
            full_name,
            location
          ),
          mentor_profile:profiles!mentorship_sessions_mentor_id_fkey (
            full_name,
            location
          )
        `)
        .eq('is_public', true)
        .order('created_at', { ascending: false });

      if (error) throw error;
      setSessions((data as any) || []);
    } catch (error: any) {
      toast({ title: "Error loading mentorship sessions", description: error.message, variant: "destructive" });
    } finally {
      setLoading(false);
    }
  };

  const askQuestion = async () => {
    if (!user) return;

    try {
      setIsAskingQuestion(true);
      const { error } = await supabase
        .from('mentorship_sessions')
        .insert([{
          ...newQuestion,
          mentee_id: user.id
        }]);

      if (error) throw error;

      toast({ title: "Question posted successfully!" });
      setNewQuestion({ question: '', category: 'business' });
      fetchSessions();
    } catch (error: any) {
      toast({ title: "Error posting question", description: error.message, variant: "destructive" });
    } finally {
      setIsAskingQuestion(false);
    }
  };

  const getCategoryColor = (category: string) => {
    const colors = {
      business: 'bg-primary text-primary-foreground',
      skills: 'bg-accent text-accent-foreground',
      funding: 'bg-trust text-white',
      marketing: 'bg-growth text-white',
      legal: 'bg-energy text-white',
      technology: 'bg-secondary text-secondary-foreground',
      farming: 'bg-earth text-white',
      other: 'bg-muted text-muted-foreground'
    };
    return colors[category as keyof typeof colors] || 'bg-secondary text-secondary-foreground';
  };

  const formatTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    
    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${diffInHours}h ago`;
    return `${Math.floor(diffInHours / 24)}d ago`;
  };

  const filteredSessions = sessions.filter(session => {
    return selectedCategory === 'all' || session.category === selectedCategory;
  });

  if (loading) {
    return (
      <div className="p-4 space-y-4">
        {[...Array(4)].map((_, i) => (
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
          <h1 className="text-2xl font-bold text-foreground">Mentorship Wall</h1>
          <p className="text-muted-foreground">Ask questions & share knowledge</p>
        </div>
        
        {user && (
          <Dialog>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Ask Question
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Ask the Community</DialogTitle>
                <DialogDescription>
                  Get help from experienced mentors and peers
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="category">Category</Label>
                  <Select 
                    value={newQuestion.category} 
                    onValueChange={(value) => setNewQuestion({ ...newQuestion, category: value })}
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
                  <Label htmlFor="question">Your Question</Label>
                  <Textarea
                    id="question"
                    value={newQuestion.question}
                    onChange={(e) => setNewQuestion({ ...newQuestion, question: e.target.value })}
                    placeholder="What would you like to know?"
                    rows={4}
                  />
                </div>
                
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    onClick={() => setIsRecording(!isRecording)}
                    className="flex-1"
                  >
                    <Mic className={`h-4 w-4 mr-2 ${isRecording ? 'text-red-500' : ''}`} />
                    {isRecording ? 'Stop Recording' : 'Record Voice'}
                  </Button>
                  
                  <Button onClick={askQuestion} disabled={isAskingQuestion} className="flex-1">
                    {isAskingQuestion ? 'Posting...' : 'Post Question'}
                  </Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {/* Category Filter */}
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

      {/* Sessions Feed */}
      <div className="space-y-4">
        {filteredSessions.map((session) => (
          <Card key={session.id} className="hover:shadow-sm transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <Avatar className="h-8 w-8">
                    <AvatarFallback className="text-xs">
                      {session.mentee_profile?.full_name?.charAt(0) || 'U'}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium">{session.mentee_profile?.full_name || 'Anonymous'}</p>
                    <p className="text-xs text-muted-foreground">
                      {session.mentee_profile?.location} • {formatTimeAgo(session.created_at)}
                    </p>
                  </div>
                </div>
                <Badge className={getCategoryColor(session.category)}>
                  {session.category}
                </Badge>
              </div>
            </CardHeader>
            
            <CardContent className="space-y-4">
              {/* Question */}
              <div className="space-y-2">
                <p className="text-sm">{session.question}</p>
                {session.question_audio_url && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setPlayingAudio(playingAudio === session.id ? null : session.id)}
                  >
                    {playingAudio === session.id ? (
                      <Pause className="h-3 w-3 mr-1" />
                    ) : (
                      <Play className="h-3 w-3 mr-1" />
                    )}
                    Voice Question
                  </Button>
                )}
              </div>

              {/* Answer */}
              {session.status === 'answered' && session.answer && (
                <div className="bg-muted/50 rounded-lg p-3 space-y-2">
                  <div className="flex items-center space-x-2">
                    <Avatar className="h-6 w-6">
                      <AvatarFallback className="text-xs">
                        {session.mentor_profile?.full_name?.charAt(0) || 'M'}
                      </AvatarFallback>
                    </Avatar>
                    <p className="text-xs font-medium text-primary">
                      {session.mentor_profile?.full_name || 'Mentor'}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {session.answered_at && formatTimeAgo(session.answered_at)}
                    </p>
                  </div>
                  <p className="text-sm">{session.answer}</p>
                  {session.answer_audio_url && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setPlayingAudio(playingAudio === `${session.id}-answer` ? null : `${session.id}-answer`)}
                    >
                      {playingAudio === `${session.id}-answer` ? (
                        <Pause className="h-3 w-3 mr-1" />
                      ) : (
                        <Play className="h-3 w-3 mr-1" />
                      )}
                      Voice Answer
                    </Button>
                  )}
                </div>
              )}

              {/* Actions */}
              <div className="flex items-center justify-between">
                <div className="flex gap-2">
                  <Button variant="ghost" size="sm">
                    <Heart className="h-3 w-3 mr-1" />
                    Helpful
                  </Button>
                  {session.status === 'open' && user && (
                    <Button variant="ghost" size="sm">
                      <Reply className="h-3 w-3 mr-1" />
                      Answer
                    </Button>
                  )}
                </div>
                
                {session.status === 'open' && (
                  <Badge variant="outline" className="text-xs">
                    Looking for answer
                  </Badge>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredSessions.length === 0 && (
        <Card className="p-8 text-center">
          <MessageCircle className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
          <p className="text-muted-foreground">No questions in this category yet.</p>
          {user && (
            <p className="text-sm text-muted-foreground mt-2">
              Be the first to ask a question!
            </p>
          )}
        </Card>
      )}

      {/* Voice Learning Banner */}
      <Card className="border-trust/50 bg-trust/5">
        <CardContent className="p-4">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-trust rounded-full flex items-center justify-center">
              <Mic className="h-5 w-5 text-white" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-trust">Voice-Enabled Learning</p>
              <p className="text-xs text-muted-foreground">
                Ask and answer in English, Lugbara, or Alur
              </p>
            </div>
            <Button size="sm" variant="outline">
              Learn More
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default MentorshipWall;