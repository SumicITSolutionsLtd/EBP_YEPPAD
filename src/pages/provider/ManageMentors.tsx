import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Plus, Edit, Trash2, Users } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";

interface Mentor {
  id: number;
  name: string;
  title: string;
  expertise: string[];
  mentees: number;
  status: string;
}

const ManageMentors = () => {
  const { toast } = useToast();
  const [mentors, setMentors] = useState<Mentor[]>([
    { id: 1, name: "Sarah Johnson", title: "Marketing Director", expertise: ["Digital Marketing", "Branding"], mentees: 5, status: "Active" },
    { id: 2, name: "David Kimani", title: "Tech Lead", expertise: ["Web Dev", "React"], mentees: 3, status: "Active" },
  ]);

  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    title: "",
    expertise: "",
    bio: "",
  });

  const handleSubmit = () => {
    if (formData.name && formData.title) {
      const newMentor: Mentor = {
        id: mentors.length + 1,
        name: formData.name,
        title: formData.title,
        expertise: formData.expertise.split(",").map(e => e.trim()),
        mentees: 0,
        status: "Active",
      };
      setMentors([...mentors, newMentor]);
      setIsDialogOpen(false);
      setFormData({ name: "", title: "", expertise: "", bio: "" });
      toast({
        title: "Mentor Added!",
        description: "The mentor profile has been created.",
      });
    }
  };

  const handleDelete = (id: number) => {
    setMentors(mentors.filter(mentor => mentor.id !== id));
    toast({
      title: "Mentor Removed",
      description: "The mentor profile has been deleted.",
    });
  };

  return (
    <DashboardLayout userType="provider">
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Manage Mentors</h1>
            <p className="text-muted-foreground">Add and manage mentor profiles for your organization</p>
          </div>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Add Mentor
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
              <DialogHeader>
                <DialogTitle>Add New Mentor</DialogTitle>
                <DialogDescription>
                  Create a mentor profile to connect with youth seeking guidance.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="mentor-name">Full Name *</Label>
                  <Input
                    id="mentor-name"
                    placeholder="e.g., Sarah Johnson"
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                  />
                </div>
                <div>
                  <Label htmlFor="mentor-title">Professional Title *</Label>
                  <Input
                    id="mentor-title"
                    placeholder="e.g., Senior Marketing Manager"
                    value={formData.title}
                    onChange={(e) => setFormData({...formData, title: e.target.value})}
                  />
                </div>
                <div>
                  <Label htmlFor="expertise">Areas of Expertise (comma-separated)</Label>
                  <Input
                    id="expertise"
                    placeholder="e.g., Digital Marketing, Brand Strategy, Social Media"
                    value={formData.expertise}
                    onChange={(e) => setFormData({...formData, expertise: e.target.value})}
                  />
                </div>
                <div>
                  <Label htmlFor="bio">Bio</Label>
                  <Textarea
                    id="bio"
                    placeholder="Brief introduction and mentoring philosophy..."
                    rows={4}
                    value={formData.bio}
                    onChange={(e) => setFormData({...formData, bio: e.target.value})}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsDialogOpen(false)}>Cancel</Button>
                <Button onClick={handleSubmit}>Add Mentor</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        {/* Mentor Cards */}
        <div className="grid md:grid-cols-2 gap-6">
          {mentors.map((mentor) => (
            <Card key={mentor.id}>
              <CardHeader>
                <div className="flex gap-4">
                  <Avatar className="h-16 w-16">
                    <AvatarFallback className="bg-primary text-primary-foreground">
                      {mentor.name.split(' ').map(n => n[0]).join('')}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <div className="flex justify-between items-start">
                      <div>
                        <CardTitle className="text-xl">{mentor.name}</CardTitle>
                        <CardDescription className="mt-1">{mentor.title}</CardDescription>
                      </div>
                      <Badge variant={mentor.status === "Active" ? "default" : "secondary"}>
                        {mentor.status}
                      </Badge>
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-wrap gap-2">
                  {mentor.expertise.map((skill, idx) => (
                    <Badge key={idx} variant="secondary">
                      {skill}
                    </Badge>
                  ))}
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Users className="h-4 w-4" />
                  <span><span className="font-semibold text-foreground">{mentor.mentees}</span> active mentees</span>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="flex-1">
                    <Edit className="h-4 w-4 mr-1" />
                    Edit
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => handleDelete(mentor.id)}>
                    <Trash2 className="h-4 w-4 mr-1" />
                    Remove
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ManageMentors;
