import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BarChart3, TrendingUp, Users, Briefcase, BookOpen, Award } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";

const Analytics = () => {
  return (
    <DashboardLayout userType="admin">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Platform Analytics</h1>
          <p className="text-muted-foreground">Comprehensive insights and performance metrics</p>
        </div>

        <Tabs defaultValue="overview" className="space-y-4">
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="users">Users</TabsTrigger>
            <TabsTrigger value="engagement">Engagement</TabsTrigger>
            <TabsTrigger value="growth">Growth</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-4">
            {/* Key Metrics */}
            <div className="grid md:grid-cols-4 gap-4">
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-3xl font-bold">1,234</span>
                    <Users className="h-8 w-8 text-primary" />
                  </div>
                  <p className="text-xs text-green-500 mt-2">↑ 12% from last month</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">Active Jobs</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-3xl font-bold">156</span>
                    <Briefcase className="h-8 w-8 text-secondary" />
                  </div>
                  <p className="text-xs text-green-500 mt-2">↑ 8% from last month</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">Training Programs</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-3xl font-bold">89</span>
                    <BookOpen className="h-8 w-8 text-accent" />
                  </div>
                  <p className="text-xs text-green-500 mt-2">↑ 15% from last month</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">Mentorships</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-3xl font-bold">234</span>
                    <Award className="h-8 w-8 text-bold-orange" />
                  </div>
                  <p className="text-xs text-green-500 mt-2">↑ 20% from last month</p>
                </CardContent>
              </Card>
            </div>

            {/* Performance Charts */}
            <div className="grid md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5" />
                    User Growth
                  </CardTitle>
                  <CardDescription>Monthly active users over time</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-64 flex items-center justify-center border border-border rounded-lg bg-muted/20">
                    <p className="text-muted-foreground">User growth chart visualization</p>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BarChart3 className="h-5 w-5" />
                    Platform Activity
                  </CardTitle>
                  <CardDescription>Daily active users and interactions</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-64 flex items-center justify-center border border-border rounded-lg bg-muted/20">
                    <p className="text-muted-foreground">Activity chart visualization</p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="users" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>User Demographics</CardTitle>
                <CardDescription>Breakdown of user types and activity</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <div className="flex justify-between mb-2">
                      <span className="text-sm font-medium">Youth Users</span>
                      <span className="text-sm text-muted-foreground">876 (71%)</span>
                    </div>
                    <div className="w-full bg-muted rounded-full h-2">
                      <div className="bg-primary h-2 rounded-full" style={{ width: '71%' }}></div>
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between mb-2">
                      <span className="text-sm font-medium">Service Providers</span>
                      <span className="text-sm text-muted-foreground">298 (24%)</span>
                    </div>
                    <div className="w-full bg-muted rounded-full h-2">
                      <div className="bg-secondary h-2 rounded-full" style={{ width: '24%' }}></div>
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between mb-2">
                      <span className="text-sm font-medium">Administrators</span>
                      <span className="text-sm text-muted-foreground">60 (5%)</span>
                    </div>
                    <div className="w-full bg-muted rounded-full h-2">
                      <div className="bg-accent h-2 rounded-full" style={{ width: '5%' }}></div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="engagement" className="space-y-4">
            <div className="grid md:grid-cols-3 gap-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm">Job Applications</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-bold">2,456</p>
                  <p className="text-xs text-muted-foreground mt-1">Total applications submitted</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm">Course Enrollments</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-bold">1,893</p>
                  <p className="text-xs text-muted-foreground mt-1">Active course enrollments</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm">Mentorship Sessions</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-bold">567</p>
                  <p className="text-xs text-muted-foreground mt-1">Sessions completed this month</p>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="growth" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Growth Metrics</CardTitle>
                <CardDescription>Month-over-month platform growth</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-80 flex items-center justify-center border border-border rounded-lg bg-muted/20">
                  <p className="text-muted-foreground">Growth trends visualization</p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default Analytics;
