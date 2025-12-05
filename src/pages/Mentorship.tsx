import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Search, Star, MapPin, Briefcase } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const mentors = [
  {
    id: 1,
    name: "Sarah Johnson",
    title: "Senior Marketing Manager",
    company: "Tech Solutions Inc.",
    expertise: ["Digital Marketing", "Brand Strategy", "Social Media"],
    location: "Nairobi, Kenya",
    rating: 4.9,
    sessions: 45,
    bio: "Passionate about helping young entrepreneurs build their personal brands.",
    available: true,
  },
  {
    id: 2,
    name: "David Kimani",
    title: "Full Stack Developer",
    company: "Innovation Labs",
    expertise: ["Web Development", "React", "Node.js"],
    location: "Remote",
    rating: 4.8,
    sessions: 32,
    bio: "Love teaching coding and helping beginners transition into tech careers.",
    available: true,
  },
  {
    id: 3,
    name: "Grace Mwangi",
    title: "Business Consultant",
    company: "Growth Partners Ltd",
    expertise: ["Business Development", "Strategy", "Finance"],
    location: "Kampala, Uganda",
    rating: 5.0,
    sessions: 58,
    bio: "Dedicated to empowering youth with business acumen and financial literacy.",
    available: false,
  },
  {
    id: 4,
    name: "James Omondi",
    title: "UX/UI Designer",
    company: "Creative Studio",
    expertise: ["Design Thinking", "UI/UX", "Product Design"],
    location: "Remote",
    rating: 4.7,
    sessions: 28,
    bio: "Helping aspiring designers build stunning portfolios and land their dream jobs.",
    available: true,
  },
];

const Mentorship = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [expertise, setExpertise] = useState("all");
  const { toast } = useToast();

  const handleRequestMentor = (mentorName: string) => {
    toast({
      title: "Mentorship Request Sent!",
      description: `Your request to connect with ${mentorName} has been submitted.`,
    });
  };

  const filteredMentors = mentors.filter((mentor) => {
    const matchesSearch = mentor.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         mentor.title.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesExpertise = expertise === "all" || mentor.expertise.some(e => 
      e.toLowerCase().includes(expertise.toLowerCase())
    );
    return matchesSearch && matchesExpertise;
  });

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Find a Mentor</h1>
          <p className="text-muted-foreground">Connect with experienced professionals to guide your journey</p>
        </div>

        {/* Search and Filters */}
        <Card>
          <CardContent className="pt-6">
            <div className="grid md:grid-cols-3 gap-4">
              <div className="md:col-span-2 relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search mentors by name or expertise..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Select value={expertise} onValueChange={setExpertise}>
                <SelectTrigger>
                  <SelectValue placeholder="Expertise Area" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Areas</SelectItem>
                  <SelectItem value="marketing">Marketing</SelectItem>
                  <SelectItem value="development">Development</SelectItem>
                  <SelectItem value="business">Business</SelectItem>
                  <SelectItem value="design">Design</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {/* Mentor Cards */}
        <div className="grid md:grid-cols-2 gap-6">
          {filteredMentors.map((mentor) => (
            <Card key={mentor.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex gap-4">
                  <Avatar className="h-16 w-16">
                    <AvatarImage src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${mentor.name}`} />
                    <AvatarFallback>{mentor.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <CardTitle className="text-xl mb-1">{mentor.name}</CardTitle>
                    <CardDescription className="flex items-center gap-1 mb-2">
                      <Briefcase className="h-4 w-4" />
                      {mentor.title}
                    </CardDescription>
                    <p className="text-sm text-muted-foreground">{mentor.company}</p>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground text-sm">{mentor.bio}</p>
                
                <div className="flex items-center gap-4 text-sm">
                  <span className="flex items-center gap-1">
                    <Star className="h-4 w-4 text-yellow-500 fill-current" />
                    {mentor.rating}
                  </span>
                  <span className="text-muted-foreground">{mentor.sessions} sessions</span>
                  <span className="flex items-center gap-1 text-muted-foreground">
                    <MapPin className="h-4 w-4" />
                    {mentor.location}
                  </span>
                </div>

                <div className="flex flex-wrap gap-2">
                  {mentor.expertise.map((skill, idx) => (
                    <Badge key={idx} variant="secondary">
                      {skill}
                    </Badge>
                  ))}
                </div>

                <div className="flex gap-2">
                  <Button 
                    className="flex-1" 
                    disabled={!mentor.available}
                    onClick={() => handleRequestMentor(mentor.name)}
                  >
                    {mentor.available ? "Request Mentorship" : "Currently Unavailable"}
                  </Button>
                  <Button variant="outline">View Profile</Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Mentorship;
