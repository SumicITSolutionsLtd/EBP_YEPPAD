import { useState } from "react";
import { ArrowLeft, Target, Users, BookOpen, Award, Lightbulb, Hammer, Palette, Laptop } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import Header from "@/components/Header";
import SkillsMarketplace from "@/components/skills/SkillsMarketplace";
import LearningHub from "@/components/learning/LearningHub";

interface ActivitiesPageProps {
  onBack: () => void;
}

const ActivitiesPage = ({ onBack }: ActivitiesPageProps) => {
  const [activeTab, setActiveTab] = useState("overview");

  const skillCategories = [
    {
      title: "Technical Skills",
      description: "Digital and technical competencies for the modern economy",
      icon: Laptop,
      skills: ["Web Development", "Mobile App Development", "Digital Marketing", "Data Analysis", "Graphic Design"],
      color: "from-blue-500 to-cyan-500"
    },
    {
      title: "Craft & Trade",
      description: "Traditional and modern craftsmanship skills",
      icon: Hammer,
      skills: ["Tailoring", "Carpentry", "Welding", "Plumbing", "Electrical Work"],
      color: "from-orange-500 to-red-500"
    },
    {
      title: "Creative Arts",
      description: "Artistic and creative expression skills",
      icon: Palette,
      skills: ["Photography", "Video Production", "Music Production", "Art & Design", "Creative Writing"],
      color: "from-purple-500 to-pink-500"
    },
    {
      title: "Business & Entrepreneurship",
      description: "Skills for starting and running businesses",
      icon: Target,
      skills: ["Business Planning", "Financial Literacy", "Sales & Marketing", "Leadership", "Project Management"],
      color: "from-green-500 to-emerald-500"
    }
  ];

  const learningPaths = [
    {
      title: "Beginner Entrepreneur",
      description: "Start your entrepreneurial journey with basic business skills",
      duration: "4 weeks",
      modules: 8,
      difficulty: "Beginner",
      skills: ["Business Ideas", "Market Research", "Basic Finance", "Customer Service"]
    },
    {
      title: "Digital Creator",
      description: "Learn to create and monetize digital content",
      duration: "6 weeks", 
      modules: 12,
      difficulty: "Intermediate",
      skills: ["Content Creation", "Social Media", "Online Marketing", "Brand Building"]
    },
    {
      title: "Skilled Craftsperson",
      description: "Master traditional trades with modern techniques",
      duration: "8 weeks",
      modules: 16,
      difficulty: "Intermediate",
      skills: ["Quality Control", "Tool Mastery", "Customer Relations", "Pricing Strategy"]
    },
    {
      title: "Community Leader",
      description: "Develop leadership skills for community impact",
      duration: "5 weeks",
      modules: 10,
      difficulty: "Advanced",
      skills: ["Team Management", "Public Speaking", "Problem Solving", "Network Building"]
    }
  ];

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Header */}
        <div className="flex items-center space-x-3 mb-6">
          <Button variant="ghost" onClick={onBack}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Dashboard
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">Activities & Skills Hub</h1>
            <p className="text-muted-foreground">Discover, learn, and showcase your talents</p>
          </div>
        </div>

        {/* Tabs Navigation */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="learning">Learning Paths</TabsTrigger>
            <TabsTrigger value="marketplace">Skills Marketplace</TabsTrigger>
            <TabsTrigger value="community">Community Hub</TabsTrigger>
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
            {/* Quick Stats */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Card className="bg-gradient-to-r from-primary to-primary-glow border-0">
                <CardContent className="p-4 text-center">
                  <div className="text-white">
                    <Target className="h-8 w-8 mx-auto mb-2" />
                    <p className="text-2xl font-bold">150+</p>
                    <p className="text-sm opacity-90">Skill Categories</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="bg-gradient-to-r from-accent to-accent-light border-0">
                <CardContent className="p-4 text-center">
                  <div className="text-white">
                    <Users className="h-8 w-8 mx-auto mb-2" />
                    <p className="text-2xl font-bold">2,500+</p>
                    <p className="text-sm opacity-90">Active Learners</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="bg-gradient-to-r from-trust to-blue-500 border-0">
                <CardContent className="p-4 text-center">
                  <div className="text-white">
                    <BookOpen className="h-8 w-8 mx-auto mb-2" />
                    <p className="text-2xl font-bold">45</p>
                    <p className="text-sm opacity-90">Learning Paths</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="bg-gradient-to-r from-purple-500 to-pink-500 border-0">
                <CardContent className="p-4 text-center">
                  <div className="text-white">
                    <Award className="h-8 w-8 mx-auto mb-2" />
                    <p className="text-2xl font-bold">890</p>
                    <p className="text-sm opacity-90">Certificates Earned</p>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Skill Categories Grid */}
            <div>
              <h2 className="text-2xl font-bold text-foreground mb-4">Explore Skill Categories</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {skillCategories.map((category, index) => {
                  const Icon = category.icon;
                  return (
                    <Card key={index} className="hover:shadow-lg transition-all duration-300 border-border">
                      <CardHeader>
                        <div className="flex items-center space-x-3">
                          <div className={`w-12 h-12 bg-gradient-to-r ${category.color} rounded-lg flex items-center justify-center`}>
                            <Icon className="h-6 w-6 text-white" />
                          </div>
                          <div>
                            <CardTitle className="text-xl">{category.title}</CardTitle>
                            <CardDescription>{category.description}</CardDescription>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-3">
                          <div className="flex flex-wrap gap-2">
                            {category.skills.map((skill, skillIndex) => (
                              <Badge key={skillIndex} variant="secondary" className="text-xs">
                                {skill}
                              </Badge>
                            ))}
                          </div>
                          <Button className="w-full" variant="outline">
                            <Lightbulb className="h-4 w-4 mr-2" />
                            Explore Skills
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </div>

            {/* Featured Learning Paths */}
            <div>
              <h2 className="text-2xl font-bold text-foreground mb-4">Popular Learning Paths</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {learningPaths.slice(0, 2).map((path, index) => (
                  <Card key={index} className="hover:shadow-md transition-shadow">
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-lg">{path.title}</CardTitle>
                        <Badge variant={path.difficulty === "Beginner" ? "secondary" : path.difficulty === "Intermediate" ? "default" : "destructive"}>
                          {path.difficulty}
                        </Badge>
                      </div>
                      <CardDescription>{path.description}</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex justify-between text-sm text-muted-foreground">
                          <span>{path.duration}</span>
                          <span>{path.modules} modules</span>
                        </div>
                        <div className="flex flex-wrap gap-1">
                          {path.skills.slice(0, 3).map((skill, skillIndex) => (
                            <Badge key={skillIndex} variant="outline" className="text-xs">
                              {skill}
                            </Badge>
                          ))}
                          {path.skills.length > 3 && (
                            <Badge variant="outline" className="text-xs">
                              +{path.skills.length - 3} more
                            </Badge>
                          )}
                        </div>
                        <Button className="w-full" size="sm">Start Learning</Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          </TabsContent>

          {/* Learning Paths Tab */}
          <TabsContent value="learning" className="space-y-6">
            <div>
              <h2 className="text-2xl font-bold text-foreground mb-4">All Learning Paths</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {learningPaths.map((path, index) => (
                  <Card key={index} className="hover:shadow-md transition-shadow">
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-lg">{path.title}</CardTitle>
                        <Badge variant={path.difficulty === "Beginner" ? "secondary" : path.difficulty === "Intermediate" ? "default" : "destructive"}>
                          {path.difficulty}
                        </Badge>
                      </div>
                      <CardDescription>{path.description}</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex justify-between text-sm text-muted-foreground">
                          <span>{path.duration}</span>
                          <span>{path.modules} modules</span>
                        </div>
                        <div className="flex flex-wrap gap-1">
                          {path.skills.map((skill, skillIndex) => (
                            <Badge key={skillIndex} variant="outline" className="text-xs">
                              {skill}
                            </Badge>
                          ))}
                        </div>
                        <Button className="w-full" size="sm">Start Learning</Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
            
            <LearningHub />
          </TabsContent>

          {/* Skills Marketplace Tab */}
          <TabsContent value="marketplace">
            <SkillsMarketplace />
          </TabsContent>

          {/* Community Hub Tab */}
          <TabsContent value="community" className="space-y-6">
            <div className="text-center py-12">
              <Users className="h-16 w-16 mx-auto text-muted-foreground mb-4" />
              <h3 className="text-xl font-semibold text-foreground mb-2">Community Hub Coming Soon</h3>
              <p className="text-muted-foreground max-w-md mx-auto">
                Connect with fellow learners, share experiences, and collaborate on projects. 
                This feature will be available soon!
              </p>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
};

export default ActivitiesPage;