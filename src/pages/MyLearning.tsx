import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Link } from "react-router-dom";
import { BookOpen, Award, Clock, TrendingUp, Download, CheckCircle } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";

const enrolledCourses = [
  {
    id: 1,
    title: "Digital Marketing Fundamentals",
    category: "Marketing",
    progress: 45,
    totalLessons: 24,
    completedLessons: 11,
    lastAccessed: "2 hours ago",
    thumbnail: "bg-gradient-to-br from-primary to-secondary",
  },
  {
    id: 2,
    title: "Business Financial Planning",
    category: "Finance",
    progress: 20,
    totalLessons: 32,
    completedLessons: 6,
    lastAccessed: "1 day ago",
    thumbnail: "bg-gradient-to-br from-accent to-bold-orange",
  },
  {
    id: 4,
    title: "Entrepreneurship Essentials",
    category: "Business",
    progress: 80,
    totalLessons: 16,
    completedLessons: 13,
    lastAccessed: "3 hours ago",
    thumbnail: "bg-gradient-to-br from-primary to-accent",
  },
];

const completedCourses = [
  {
    id: 5,
    title: "Introduction to Business",
    category: "Business",
    completedDate: "2024-01-15",
    certificateId: "CERT-2024-001",
    thumbnail: "bg-gradient-to-br from-deep-blue to-royal-blue",
  },
];

const certificates = [
  {
    id: 1,
    courseTitle: "Introduction to Business",
    issuedDate: "January 15, 2024",
    certificateId: "CERT-2024-001",
    instructor: "Grace Mwangi",
  },
];

const MyLearning = () => {
  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">My Learning</h1>
          <p className="text-muted-foreground">Track your progress and access your courses</p>
        </div>

        {/* Stats Overview */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Courses in Progress</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <BookOpen className="h-5 w-5 text-primary" />
                <span className="text-2xl font-bold">{enrolledCourses.length}</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Completed Courses</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <CheckCircle className="h-5 w-5 text-secondary" />
                <span className="text-2xl font-bold">{completedCourses.length}</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Certificates Earned</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Award className="h-5 w-5 text-accent" />
                <span className="text-2xl font-bold">{certificates.length}</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Hours Learned</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-bold-orange" />
                <span className="text-2xl font-bold">47</span>
              </div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="inprogress">
          <TabsList>
            <TabsTrigger value="inprogress">In Progress</TabsTrigger>
            <TabsTrigger value="completed">Completed</TabsTrigger>
            <TabsTrigger value="certificates">Certificates</TabsTrigger>
          </TabsList>

          <TabsContent value="inprogress" className="space-y-4">
            {enrolledCourses.map((course) => (
              <Card key={course.id}>
                <div className="md:flex">
                  <div className={`${course.thumbnail} md:w-48 h-32 md:h-auto flex items-center justify-center`}>
                    <BookOpen className="h-12 w-12 text-white/80" />
                  </div>
                  <div className="flex-1">
                    <CardHeader>
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <Badge variant="secondary" className="mb-2">{course.category}</Badge>
                          <CardTitle className="mb-2">{course.title}</CardTitle>
                          <CardDescription>Last accessed {course.lastAccessed}</CardDescription>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <div className="flex justify-between text-sm">
                            <span className="font-medium">Progress</span>
                            <span className="text-muted-foreground">
                              {course.completedLessons} of {course.totalLessons} lessons
                            </span>
                          </div>
                          <Progress value={course.progress} />
                          <div className="text-sm font-medium text-primary">{course.progress}% complete</div>
                        </div>
                        <Link to={`/dashboard/user/training/${course.id}`}>
                          <Button>Continue Learning</Button>
                        </Link>
                      </div>
                    </CardContent>
                  </div>
                </div>
              </Card>
            ))}

            {enrolledCourses.length === 0 && (
              <Card>
                <CardContent className="py-12 text-center">
                  <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-semibold mb-2">No courses in progress</h3>
                  <p className="text-muted-foreground mb-4">Browse our catalog to start learning</p>
                  <Link to="/dashboard/user/training">
                    <Button>Browse Courses</Button>
                  </Link>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          <TabsContent value="completed" className="space-y-4">
            {completedCourses.map((course) => (
              <Card key={course.id}>
                <div className="md:flex">
                  <div className={`${course.thumbnail} md:w-48 h-32 md:h-auto flex items-center justify-center`}>
                    <CheckCircle className="h-12 w-12 text-white/80" />
                  </div>
                  <div className="flex-1">
                    <CardHeader>
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <Badge variant="secondary" className="mb-2">{course.category}</Badge>
                          <CardTitle className="mb-2 flex items-center gap-2">
                            {course.title}
                            <CheckCircle className="h-5 w-5 text-primary" />
                          </CardTitle>
                          <CardDescription>Completed on {new Date(course.completedDate).toLocaleDateString()}</CardDescription>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="flex gap-2">
                        <Button variant="outline">
                          <Award className="h-4 w-4 mr-2" />
                          View Certificate
                        </Button>
                        <Link to={`/dashboard/user/training/${course.id}`}>
                          <Button variant="ghost">Review Course</Button>
                        </Link>
                      </div>
                    </CardContent>
                  </div>
                </div>
              </Card>
            ))}

            {completedCourses.length === 0 && (
              <Card>
                <CardContent className="py-12 text-center">
                  <CheckCircle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-semibold mb-2">No completed courses yet</h3>
                  <p className="text-muted-foreground">Keep learning to earn your first certificate!</p>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          <TabsContent value="certificates" className="space-y-4">
            {certificates.map((cert) => (
              <Card key={cert.id} className="overflow-hidden">
                <div className="bg-gradient-to-br from-primary via-secondary to-accent p-8 text-white">
                  <div className="flex items-start justify-between mb-6">
                    <Award className="h-12 w-12" />
                    <Badge className="bg-white/20 text-white border-white/30">Verified</Badge>
                  </div>
                  <h3 className="text-2xl font-bold mb-2">Certificate of Completion</h3>
                  <p className="text-white/90 mb-4">This certifies that you have successfully completed</p>
                  <h2 className="text-3xl font-bold mb-6">{cert.courseTitle}</h2>
                  <div className="flex items-center justify-between text-sm text-white/80">
                    <div>
                      <p>Instructor: {cert.instructor}</p>
                      <p>Issued: {cert.issuedDate}</p>
                    </div>
                    <div className="text-right">
                      <p>Certificate ID</p>
                      <p className="font-mono">{cert.certificateId}</p>
                    </div>
                  </div>
                </div>
                <CardContent className="pt-6">
                  <div className="flex gap-2">
                    <Button>
                      <Download className="h-4 w-4 mr-2" />
                      Download PDF
                    </Button>
                    <Button variant="outline">Share Certificate</Button>
                  </div>
                </CardContent>
              </Card>
            ))}

            {certificates.length === 0 && (
              <Card>
                <CardContent className="py-12 text-center">
                  <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-semibold mb-2">No certificates yet</h3>
                  <p className="text-muted-foreground mb-4">Complete courses to earn certificates</p>
                  <Link to="/dashboard/user/training">
                    <Button>Browse Courses</Button>
                  </Link>
                </CardContent>
              </Card>
            )}
          </TabsContent>
        </Tabs>

        {/* Learning Stats */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5" />
              Your Learning Journey
            </CardTitle>
            <CardDescription>Your progress this month</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-6">
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Lessons Completed</p>
                <p className="text-3xl font-bold text-primary">23</p>
                <p className="text-sm text-muted-foreground">+8 from last month</p>
              </div>
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Learning Streak</p>
                <p className="text-3xl font-bold text-secondary">7 days</p>
                <p className="text-sm text-muted-foreground">Keep it up!</p>
              </div>
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Next Milestone</p>
                <p className="text-3xl font-bold text-accent">2 courses</p>
                <p className="text-sm text-muted-foreground">Until next certificate</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default MyLearning;
