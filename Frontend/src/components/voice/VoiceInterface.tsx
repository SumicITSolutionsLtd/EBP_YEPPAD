import { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Mic, MicOff, Volume2, VolumeX, MessageCircle, X, Loader2, Globe } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { supabase } from '@/integrations/supabase/client';

interface VoiceInterfaceProps {
  isOpen: boolean;
  onClose: () => void;
}

interface Language {
  code: string;
  name: string;
  voice: string;
  systemPrompt: string;
}

const languages: Language[] = [
  {
    code: 'en',
    name: 'English',
    voice: 'alloy',
    systemPrompt: `You are a helpful voice assistant for Kwetu Hub, a youth employment platform in Uganda. You help users navigate the app and answer questions about:

1. Finding and applying for jobs
2. Posting skills and talents
3. Asking mentorship questions
4. Learning business skills
5. Understanding the platform features

The app has these main sections:
- Jobs: Browse opportunities, apply for positions
- Skills: Post your talents, set rates, show availability
- Mentorship: Ask questions, get advice from experienced people
- Learning: Business courses, idea validation, planning tools
- Profile: Manage your information, view activity

Keep responses concise, helpful, and encouraging. Speak in a friendly, supportive tone.`
  },
  {
    code: 'lg',
    name: 'Lugbara',
    voice: 'echo',
    systemPrompt: `You are a helpful voice assistant for Kwetu Hub in Lugbara language. Kwetu Hub ni bisnis platform alu aniyo ku youth ya Uganda. Speak primarily in Lugbara but include English terms for technical features. Help users with:

1. Jobs (Adongo): Finding work opportunities
2. Skills (Opi): Showing your talents and abilities  
3. Mentorship (Ayoyo): Getting advice from experienced people
4. Learning (Eristu): Business education and skills
5. Profile: Managing your information

Always be encouraging and speak with respect. Mix Lugbara with English for technical terms users understand.`
  },
  {
    code: 'alr',
    name: 'Alur',
    voice: 'shimmer',
    systemPrompt: `You are a helpful voice assistant for Kwetu Hub in Alur language. Kwetu Hub e platform me bisnis pa lwongo youth ma i Uganda. Speak primarily in Alur but include English terms for app features. Help users with:

1. Jobs (Tic): Finding work opportunities
2. Skills (Neno): Showing your talents and abilities
3. Mentorship (Pwonyo): Getting advice from experienced people  
4. Learning (Nongo): Business education and skills
5. Profile: Managing your information

Always encourage users and speak respectfully. Mix Alur with English for technical app terms.`
  }
];

const VoiceInterface = ({ isOpen, onClose }: VoiceInterfaceProps) => {
  const [isRecording, setIsRecording] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [selectedLanguage, setSelectedLanguage] = useState<Language>(languages[0]);
  const [conversation, setConversation] = useState<Array<{
    role: 'user' | 'assistant';
    content: string;
    timestamp: Date;
  }>>([]);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const { toast } = useToast();

  useEffect(() => {
    if (isOpen && conversation.length === 0) {
      initializeWelcomeMessage();
    }
  }, [isOpen, selectedLanguage]);

  const getWelcomeMessage = () => {
    switch (selectedLanguage.code) {
      case 'lg':
        return 'Aya! A ni voice assistant pa Kwetu Hub. A rua azi ayiko ku app-ni, riyo jobs, ma skills ani, na ani questions ku platform-ni. Enzi rua azi muni aya?';
      case 'alr':
        return 'Apwoyo! An voice assistant pa Kwetu Hub. Aromo kony i ki app-no, nong tic, keto neno-ni, kadi lagoro lok ki platform-wa. Ere aromo kony-i kwini?';
      default:
        return 'Hello! I\'m your Kwetu Hub voice assistant. I can help you navigate the app, find jobs, post skills, or answer questions about our platform. How can I help you today?';
    }
  };

  const initializeWelcomeMessage = () => {
    const welcomeText = getWelcomeMessage();
    setConversation([{
      role: 'assistant',
      content: welcomeText,
      timestamp: new Date()
    }]);
    speakText(welcomeText);
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          sampleRate: 16000,
          channelCount: 1,
          echoCancellation: true,
          noiseSuppression: true
        }
      });
      
      mediaRecorderRef.current = new MediaRecorder(stream, {
        mimeType: 'audio/webm;codecs=opus'
      });
      
      audioChunksRef.current = [];
      
      mediaRecorderRef.current.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };
      
      mediaRecorderRef.current.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        await processAudio(audioBlob);
        
        // Stop all tracks
        stream.getTracks().forEach(track => track.stop());
      };
      
      mediaRecorderRef.current.start();
      setIsRecording(true);
    } catch (error) {
      console.error('Error starting recording:', error);
      toast({
        title: "Microphone Error",
        description: "Could not access microphone. Please check permissions.",
        variant: "destructive"
      });
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
    }
  };

  const processAudio = async (audioBlob: Blob) => {
    setIsProcessing(true);
    
    try {
      // Convert audio to base64
      const arrayBuffer = await audioBlob.arrayBuffer();
      const base64Audio = btoa(String.fromCharCode(...new Uint8Array(arrayBuffer)));
      
      // Send to speech-to-text service with language
      const { data: transcriptionData, error: transcriptionError } = await supabase.functions.invoke('voice-to-text', {
        body: { 
          audio: base64Audio,
          language: selectedLanguage.code
        }
      });
      
      if (transcriptionError) throw transcriptionError;
      
      const userText = transcriptionData.text;
      
      // Add user message to conversation
      const userMessage = {
        role: 'user' as const,
        content: userText,
        timestamp: new Date()
      };
      
      setConversation(prev => [...prev, userMessage]);
      
      // Get AI response
      const aiResponse = await getAIResponse(userText);
      
      // Add AI response to conversation
      const aiMessage = {
        role: 'assistant' as const,
        content: aiResponse,
        timestamp: new Date()
      };
      
      setConversation(prev => [...prev, aiMessage]);
      
      // Speak the response
      await speakText(aiResponse);
      
    } catch (error) {
      console.error('Error processing audio:', error);
      toast({
        title: "Processing Error",
        description: "Could not process your voice message. Please try again.",
        variant: "destructive"
      });
    } finally {
      setIsProcessing(false);
    }
  };

  const getAIResponse = async (userInput: string): Promise<string> => {
    try {
      const { data, error } = await supabase.functions.invoke('chat-with-ai', {
        body: {
          message: userInput,
          systemPrompt: selectedLanguage.systemPrompt,
          conversationHistory: conversation.slice(-4) // Last 4 messages for context
        }
      });

      if (error) throw error;
      
      return data.response || "I'm here to help! Could you please repeat your question?";
    } catch (error) {
      console.error('Error getting AI response:', error);
      return "I'm having trouble understanding right now. You can also use the regular app features by tapping the tabs below.";
    }
  };

  const speakText = async (text: string) => {
    setIsPlaying(true);
    
    try {
      const { data, error } = await supabase.functions.invoke('text-to-speech', {
        body: { 
          text, 
          voice: selectedLanguage.voice,
          language: selectedLanguage.code
        }
      });

      if (error) throw error;

      // Play the audio
      const audioData = `data:audio/mp3;base64,${data.audioContent}`;
      const audio = new Audio(audioData);
      
      audio.onended = () => setIsPlaying(false);
      audio.onerror = () => {
        setIsPlaying(false);
        console.error('Error playing audio');
      };
      
      await audio.play();
    } catch (error) {
      console.error('Error with text-to-speech:', error);
      setIsPlaying(false);
      
      // Fallback to browser speech synthesis
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.9;
        utterance.pitch = 1;
        utterance.lang = selectedLanguage.code === 'en' ? 'en-US' : selectedLanguage.code;
        utterance.onend = () => setIsPlaying(false);
        speechSynthesis.speak(utterance);
      }
    }
  };

  const clearConversation = () => {
    const welcomeText = getWelcomeMessage();
    setConversation([{
      role: 'assistant',
      content: welcomeText,
      timestamp: new Date()
    }]);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-end sm:items-center sm:justify-center p-4">
      <Card className="w-full max-w-md max-h-[80vh] flex flex-col">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center text-lg">
                <MessageCircle className="h-5 w-5 mr-2 text-primary" />
                Voice Assistant
              </CardTitle>
              <CardDescription>
                Speak to navigate and get help
              </CardDescription>
            </div>
            <Button variant="ghost" size="sm" onClick={onClose}>
              <X className="h-4 w-4" />
            </Button>
          </div>
          
          {/* Language Selector */}
          <div className="flex items-center justify-between mt-3 pt-3 border-t border-border">
            <div className="flex items-center space-x-2">
              <Globe className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium">Language</span>
            </div>
            <Select 
              value={selectedLanguage.code} 
              onValueChange={(value) => {
                const lang = languages.find(l => l.code === value) || languages[0];
                setSelectedLanguage(lang);
              }}
            >
              <SelectTrigger className="w-32 h-8">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {languages.map((lang) => (
                  <SelectItem key={lang.code} value={lang.code}>
                    {lang.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>

        <CardContent className="flex-1 flex flex-col space-y-4">
          {/* Conversation */}
          <div className="flex-1 max-h-60 overflow-y-auto space-y-3">
            {conversation.map((message, index) => (
              <div key={index} className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[80%] p-3 rounded-lg text-sm ${
                  message.role === 'user' 
                    ? 'bg-primary text-primary-foreground' 
                    : 'bg-muted text-muted-foreground'
                }`}>
                  <p>{message.content}</p>
                  <p className="text-xs opacity-70 mt-1">
                    {message.timestamp.toLocaleTimeString()}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {/* Status */}
          <div className="flex justify-center">
            {isProcessing && (
              <Badge variant="secondary" className="animate-pulse">
                <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                Processing...
              </Badge>
            )}
            {isPlaying && (
              <Badge variant="secondary">
                <Volume2 className="h-3 w-3 mr-1" />
                Speaking...
              </Badge>
            )}
            {isRecording && (
              <Badge variant="default" className="animate-pulse">
                <Mic className="h-3 w-3 mr-1" />
                Listening...
              </Badge>
            )}
          </div>

          {/* Controls */}
          <div className="flex justify-center space-x-3">
            <Button
              variant={isRecording ? "destructive" : "default"}
              size="lg"
              className="rounded-full h-16 w-16"
              onClick={isRecording ? stopRecording : startRecording}
              disabled={isProcessing || isPlaying}
            >
              {isRecording ? (
                <MicOff className="h-6 w-6" />
              ) : (
                <Mic className="h-6 w-6" />
              )}
            </Button>
            
            <Button
              variant="outline"
              size="lg"
              className="rounded-full h-16 w-16"
              onClick={() => {
                if (isPlaying) {
                  speechSynthesis.cancel();
                  setIsPlaying(false);
                } else {
                  clearConversation();
                }
              }}
              disabled={isRecording || isProcessing}
            >
              {isPlaying ? (
                <VolumeX className="h-6 w-6" />
              ) : (
                <MessageCircle className="h-6 w-6" />
              )}
            </Button>
          </div>

          <p className="text-xs text-center text-muted-foreground">
            {isRecording ? 'Tap the red button to stop recording' : 'Tap the microphone to start speaking'}
          </p>
        </CardContent>
      </Card>
    </div>
  );
};

export default VoiceInterface;