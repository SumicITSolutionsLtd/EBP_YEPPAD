import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Input } from "@/components/ui/input";
import { Plus, Award, TrendingUp, Target } from "lucide-react";
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
import { Label } from "@/components/ui/label";

interface Skill {
  id: number;
  name: string;
  level: string;
  progress: number;
  category: string;
}

const MySkills = () => {
  const { toast } = useToast();
  const [skills, setSkills] = useState<Skill[]>([
    { id: 1, name: "Digital Marketing", level: "Intermediate", progress: 65, category: "Marketing" },
    { id: 2, name: "Web Development", level: "Beginner", progress: 30, category: "Technical" },
    { id: 3, name: "Business Strategy", level: "Advanced", progress: 85, category: "Business" },
    { id: 4, name: "Graphic Design", level: "Intermediate", progress: 55, category: "Creative" },
  ]);

  const [newSkill, setNewSkill] = useState("");
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const handleAddSkill = () => {
    if (newSkill.trim()) {
      const skill: Skill = {
        id: skills.length + 1,
        name: newSkill,
        level: "Beginner",
        progress: 10,
        category: "Other",
      };
      setSkills([...skills, skill]);
      setNewSkill("");
      setIsDialogOpen(false);
      toast({
        title: "Skill Added!",
        description: `${newSkill} has been added to your profile.`,
      });
    }
  };

  const getLevelColor = (level: string) => {
    switch (level) {
      case "Beginner": return "bg-blue-500";
      case "Intermediate": return "bg-yellow-500";
      case "Advanced": return "bg-green-500";
      default: return "bg-gray-500";
    }
  };

  const categories = [...new Set(skills.map(s => s.category))];

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-foreground">My Skills</h1>
            <p className="text-muted-foreground">Track and develop your professional skills</p>
          </div>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Add Skill
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Add New Skill</DialogTitle>
                <DialogDescription>
                  Add a skill you want to develop or track your progress on.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="skill-name">Skill Name</Label>
                  <Input
                    id="skill-name"
                    placeholder="e.g., Project Management"
                    value={newSkill}
                    onChange={(e) => setNewSkill(e.target.value)}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsDialogOpen(false)}>Cancel</Button>
                <Button onClick={handleAddSkill}>Add Skill</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        {/* Stats Overview */}
        <div className="grid md:grid-cols-3 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Skills</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Award className="h-5 w-5 text-primary" />
                <span className="text-2xl font-bold">{skills.length}</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Average Progress</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-secondary" />
                <span className="text-2xl font-bold">
                  {Math.round(skills.reduce((acc, s) => acc + s.progress, 0) / skills.length)}%
                </span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Categories</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Target className="h-5 w-5 text-accent" />
                <span className="text-2xl font-bold">{categories.length}</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Skills by Category */}
        {categories.map((category) => (
          <Card key={category}>
            <CardHeader>
              <CardTitle>{category}</CardTitle>
              <CardDescription>Skills in this category</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {skills.filter(s => s.category === category).map((skill) => (
                  <div key={skill.id} className="space-y-2">
                    <div className="flex justify-between items-center">
                      <div className="flex items-center gap-3">
                        <h4 className="font-semibold">{skill.name}</h4>
                        <Badge variant="outline" className={`${getLevelColor(skill.level)} text-white`}>
                          {skill.level}
                        </Badge>
                      </div>
                      <span className="text-sm text-muted-foreground">{skill.progress}%</span>
                    </div>
                    <Progress value={skill.progress} className="h-2" />
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        ))}

        {/* Recommended Skills */}
        <Card>
          <CardHeader>
            <CardTitle>Recommended Skills to Learn</CardTitle>
            <CardDescription>Based on your career interests and current skills</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              <Badge variant="secondary" className="cursor-pointer hover:bg-secondary/80">
                Data Analysis
              </Badge>
              <Badge variant="secondary" className="cursor-pointer hover:bg-secondary/80">
                Public Speaking
              </Badge>
              <Badge variant="secondary" className="cursor-pointer hover:bg-secondary/80">
                Content Writing
              </Badge>
              <Badge variant="secondary" className="cursor-pointer hover:bg-secondary/80">
                Financial Literacy
              </Badge>
              <Badge variant="secondary" className="cursor-pointer hover:bg-secondary/80">
                Leadership
              </Badge>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default MySkills;
