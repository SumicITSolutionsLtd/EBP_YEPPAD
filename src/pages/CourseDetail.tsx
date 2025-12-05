import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookOpen, Clock, Users, Star, Award, CheckCircle, PlayCircle, ArrowLeft } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";

const courseData = {
  1: {
    id: 1,
    title: "Digital Marketing Fundamentals",
    description: "Master the basics of digital marketing including SEO, social media, and content marketing. This comprehensive course will take you from beginner to confident marketer.",
    category: "Marketing",
    level: "Beginner",
    duration: "6 weeks",
    students: 234,
    rating: 4.8,
    instructor: "Sarah Johnson",
    instructorBio: "Digital Marketing Expert with 10+ years experience",
    thumbnail: "bg-gradient-to-br from-primary to-secondary",
    curriculum: [
      {
        week: 1,
        title: "Introduction to Digital Marketing",
        lessons: [
          { id: 1, title: "What is Digital Marketing?", duration: "15 min", completed: false },
          { id: 2, title: "Digital Marketing Channels Overview", duration: "20 min", completed: false },
          { id: 3, title: "Setting Marketing Goals", duration: "18 min", completed: false },
        ],
      },
      {
        week: 2,
        title: "Search Engine Optimization (SEO)",
        lessons: [
          { id: 4, title: "SEO Fundamentals", duration: "25 min", completed: false },
          { id: 5, title: "Keyword Research", duration: "30 min", completed: false },
          { id: 6, title: "On-Page SEO", duration: "22 min", completed: false },
        ],
      },
      {
        week: 3,
        title: "Social Media Marketing",
        lessons: [
          { id: 7, title: "Social Media Strategy", duration: "20 min", completed: false },
          { id: 8, title: "Content Creation", duration: "28 min", completed: false },
          { id: 9, title: "Community Management", duration: "18 min", completed: false },
        ],
      },
      {
        week: 4,
        title: "Content Marketing",
        lessons: [
          { id: 10, title: "Content Strategy", duration: "25 min", completed: false },
          { id: 11, title: "Blog Writing", duration: "30 min", completed: false },
          { id: 12, title: "Video Marketing", duration: "22 min", completed: false },
        ],
      },
    ],
    learningOutcomes: [
      "Understand digital marketing fundamentals and strategies",
      "Create and execute SEO campaigns",
      "Develop social media marketing plans",
      "Build content marketing strategies",
      "Measure and analyze marketing performance",
    ],
    prerequisites: "No prior experience required",
    certificateOffered: true,
  },
};

const CourseDetail = () => {
  const { courseId } = useParams();
  const { toast } = useToast();
  const [isEnrolled, setIsEnrolled] = useState(false);
  
  const course = courseData[Number(courseId)] || courseData[1];

  const handleEnroll = () => {
    setIsEnrolled(true);
    toast({
      title: "Successfully Enrolled!",
      description: `You've been enrolled in ${course.title}. Start learning now!`,
    });
  };

  const totalLessons = course.curriculum.reduce((acc, week) => acc + week.lessons.length, 0);
  const completedLessons = course.curriculum.reduce(
    (acc, week) => acc + week.lessons.filter((lesson) => lesson.completed).length,
    0
  );
  const progressPercentage = (completedLessons / totalLessons) * 100;

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <Link to="/dashboard/user/training" className="inline-flex items-center text-primary hover:underline">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Catalog
        </Link>

        {/* Course Header */}
        <div className={`${course.thumbnail} rounded-lg p-8 text-white`}>
          <div className="max-w-3xl">
            <div className="flex flex-wrap gap-2 mb-4">
              <Badge className="bg-white/20 text-white border-white/30">{course.category}</Badge>
              <Badge className="bg-white/20 text-white border-white/30">{course.level}</Badge>
            </div>
            <h1 className="text-4xl font-bold mb-4">{course.title}</h1>
            <p className="text-xl text-white/90 mb-6">{course.description}</p>
            
            <div className="flex flex-wrap items-center gap-6 text-white/90">
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5" />
                <span>{course.duration}</span>
              </div>
              <div className="flex items-center gap-2">
                <Users className="h-5 w-5" />
                <span>{course.students} students</span>
              </div>
              <div className="flex items-center gap-2">
                <Star className="h-5 w-5 fill-current" />
                <span>{course.rating} rating</span>
              </div>
              {course.certificateOffered && (
                <div className="flex items-center gap-2">
                  <Award className="h-5 w-5" />
                  <span>Certificate included</span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            <Tabs defaultValue="overview">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="overview">Overview</TabsTrigger>
                <TabsTrigger value="curriculum">Curriculum</TabsTrigger>
                <TabsTrigger value="instructor">Instructor</TabsTrigger>
              </TabsList>

              <TabsContent value="overview" className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>What You'll Learn</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="space-y-3">
                      {course.learningOutcomes.map((outcome, index) => (
                        <li key={index} className="flex items-start gap-3">
                          <CheckCircle className="h-5 w-5 text-primary mt-0.5 flex-shrink-0" />
                          <span>{outcome}</span>
                        </li>
                      ))}
                    </ul>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Prerequisites</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">{course.prerequisites}</p>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="curriculum" className="space-y-4">
                {isEnrolled && (
                  <Card>
                    <CardContent className="pt-6">
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span className="font-medium">Your Progress</span>
                          <span className="text-muted-foreground">
                            {completedLessons} of {totalLessons} lessons
                          </span>
                        </div>
                        <Progress value={progressPercentage} />
                      </div>
                    </CardContent>
                  </Card>
                )}

                {course.curriculum.map((week) => (
                  <Card key={week.week}>
                    <CardHeader>
                      <CardTitle className="text-lg">Week {week.week}: {week.title}</CardTitle>
                      <CardDescription>{week.lessons.length} lessons</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2">
                        {week.lessons.map((lesson) => (
                          <div
                            key={lesson.id}
                            className="flex items-center justify-between p-3 border border-border rounded-lg hover:bg-muted/50 transition-colors"
                          >
                            <div className="flex items-center gap-3">
                              {lesson.completed ? (
                                <CheckCircle className="h-5 w-5 text-primary flex-shrink-0" />
                              ) : (
                                <PlayCircle className="h-5 w-5 text-muted-foreground flex-shrink-0" />
                              )}
                              <span className={lesson.completed ? "line-through text-muted-foreground" : ""}>
                                {lesson.title}
                              </span>
                            </div>
                            <span className="text-sm text-muted-foreground">{lesson.duration}</span>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </TabsContent>

              <TabsContent value="instructor">
                <Card>
                  <CardHeader>
                    <CardTitle>About the Instructor</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-start gap-4">
                      <div className="bg-primary/10 rounded-full p-4">
                        <Users className="h-8 w-8 text-primary" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold mb-2">{course.instructor}</h3>
                        <p className="text-muted-foreground mb-4">{course.instructorBio}</p>
                        <div className="flex items-center gap-4 text-sm">
                          <div className="flex items-center gap-2">
                            <BookOpen className="h-4 w-4 text-muted-foreground" />
                            <span>5 courses</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Users className="h-4 w-4 text-muted-foreground" />
                            <span>1,234 students</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Star className="h-4 w-4 fill-accent text-accent" />
                            <span>4.8 rating</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>

          {/* Sidebar */}
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Enroll in this Course</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {!isEnrolled ? (
                  <>
                    <Button className="w-full" size="lg" onClick={handleEnroll}>
                      Enroll Now
                    </Button>
                    <p className="text-sm text-center text-muted-foreground">
                      Free for all Youth Connect members
                    </p>
                  </>
                ) : (
                  <>
                    <div className="bg-primary/10 p-4 rounded-lg text-center">
                      <CheckCircle className="h-8 w-8 text-primary mx-auto mb-2" />
                      <p className="font-semibold text-primary">You're Enrolled!</p>
                    </div>
                    <Link to="/dashboard/user/my-learning">
                      <Button className="w-full" variant="outline">
                        Continue Learning
                      </Button>
                    </Link>
                  </>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">This course includes:</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-primary" />
                    <span>{totalLessons} video lessons</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-primary" />
                    <span>Lifetime access</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-primary" />
                    <span>Downloadable resources</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-primary" />
                    <span>Assignments & quizzes</span>
                  </li>
                  {course.certificateOffered && (
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-primary" />
                      <span>Certificate of completion</span>
                    </li>
                  )}
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default CourseDetail;
