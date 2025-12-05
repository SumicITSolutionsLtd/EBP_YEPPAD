import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Link } from "react-router-dom";
import { BookOpen, Clock, Users, Search, Star, Award } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";

const courses = [
  {
    id: 1,
    title: "Digital Marketing Fundamentals",
    description: "Master the basics of digital marketing including SEO, social media, and content marketing.",
    category: "Marketing",
    level: "Beginner",
    duration: "6 weeks",
    students: 234,
    rating: 4.8,
    instructor: "Sarah Johnson",
    thumbnail: "bg-gradient-to-br from-primary to-secondary",
  },
  {
    id: 2,
    title: "Business Financial Planning",
    description: "Learn how to manage business finances, create budgets, and plan for growth.",
    category: "Finance",
    level: "Intermediate",
    duration: "8 weeks",
    students: 189,
    rating: 4.9,
    instructor: "Michael Chen",
    thumbnail: "bg-gradient-to-br from-accent to-bold-orange",
  },
  {
    id: 3,
    title: "Web Development Basics",
    description: "Start your coding journey with HTML, CSS, and JavaScript fundamentals.",
    category: "Technology",
    level: "Beginner",
    duration: "10 weeks",
    students: 456,
    rating: 4.7,
    instructor: "David Omondi",
    thumbnail: "bg-gradient-to-br from-secondary to-bright-blue",
  },
  {
    id: 4,
    title: "Entrepreneurship Essentials",
    description: "From idea to launch - learn the complete process of starting your own business.",
    category: "Business",
    level: "Beginner",
    duration: "4 weeks",
    students: 567,
    rating: 4.9,
    instructor: "Grace Mwangi",
    thumbnail: "bg-gradient-to-br from-primary to-accent",
  },
  {
    id: 5,
    title: "Advanced Social Media Strategy",
    description: "Take your social media marketing to the next level with advanced tactics.",
    category: "Marketing",
    level: "Advanced",
    duration: "6 weeks",
    students: 123,
    rating: 4.6,
    instructor: "Sarah Johnson",
    thumbnail: "bg-gradient-to-br from-bold-orange to-soft-orange",
  },
  {
    id: 6,
    title: "Data Analysis for Business",
    description: "Learn to analyze data and make informed business decisions using Excel and analytics tools.",
    category: "Analytics",
    level: "Intermediate",
    duration: "7 weeks",
    students: 198,
    rating: 4.8,
    instructor: "James Kimani",
    thumbnail: "bg-gradient-to-br from-deep-blue to-royal-blue",
  },
];

const categories = ["All", "Business", "Marketing", "Finance", "Technology", "Analytics"];
const levels = ["All Levels", "Beginner", "Intermediate", "Advanced"];

const TrainingCatalog = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedLevel, setSelectedLevel] = useState("All Levels");

  const filteredCourses = courses.filter((course) => {
    const matchesSearch = course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         course.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === "All" || course.category === selectedCategory;
    const matchesLevel = selectedLevel === "All Levels" || course.level === selectedLevel;
    
    return matchesSearch && matchesCategory && matchesLevel;
  });

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Training Programs</h1>
          <p className="text-muted-foreground">Browse and enroll in courses to build your skills</p>
        </div>

        {/* Search and Filters */}
        <Card>
          <CardContent className="pt-6">
            <div className="space-y-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
                <Input
                  placeholder="Search courses..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              
              <div className="flex flex-wrap gap-4">
                <div className="flex-1 min-w-[200px]">
                  <label className="text-sm font-medium mb-2 block">Category</label>
                  <div className="flex flex-wrap gap-2">
                    {categories.map((category) => (
                      <Button
                        key={category}
                        variant={selectedCategory === category ? "default" : "outline"}
                        size="sm"
                        onClick={() => setSelectedCategory(category)}
                      >
                        {category}
                      </Button>
                    ))}
                  </div>
                </div>
                
                <div className="flex-1 min-w-[200px]">
                  <label className="text-sm font-medium mb-2 block">Level</label>
                  <div className="flex flex-wrap gap-2">
                    {levels.map((level) => (
                      <Button
                        key={level}
                        variant={selectedLevel === level ? "default" : "outline"}
                        size="sm"
                        onClick={() => setSelectedLevel(level)}
                      >
                        {level}
                      </Button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Course Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCourses.map((course) => (
            <Card key={course.id} className="overflow-hidden hover:shadow-lg transition-shadow">
              <div className={`h-40 ${course.thumbnail} flex items-center justify-center`}>
                <BookOpen className="h-16 w-16 text-white/80" />
              </div>
              <CardHeader>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <Badge variant="secondary">{course.category}</Badge>
                  <Badge variant="outline">{course.level}</Badge>
                </div>
                <CardTitle className="line-clamp-2">{course.title}</CardTitle>
                <CardDescription className="line-clamp-2">{course.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center gap-4 text-sm text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <Clock className="h-4 w-4" />
                      <span>{course.duration}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Users className="h-4 w-4" />
                      <span>{course.students}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Star className="h-4 w-4 fill-accent text-accent" />
                      <span>{course.rating}</span>
                    </div>
                  </div>
                  
                  <div className="text-sm text-muted-foreground">
                    by {course.instructor}
                  </div>
                  
                  <Link to={`/dashboard/user/training/${course.id}`}>
                    <Button className="w-full">View Course</Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {filteredCourses.length === 0 && (
          <Card>
            <CardContent className="py-12 text-center">
              <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No courses found</h3>
              <p className="text-muted-foreground">Try adjusting your filters or search query</p>
            </CardContent>
          </Card>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TrainingCatalog;
