import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Users, Briefcase, TrendingUp, Activity, CheckCircle, AlertCircle } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";

const AdminDashboard = () => {
  return (
    <DashboardLayout userType="admin">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Admin Dashboard</h1>
          <p className="text-muted-foreground">Monitor and manage platform activity</p>
        </div>

        {/* Stats Grid */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Users className="h-5 w-5 text-primary" />
                <span className="text-2xl font-bold">1,234</span>
              </div>
              <p className="text-xs text-muted-foreground mt-1">+12% from last month</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Service Providers</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Briefcase className="h-5 w-5 text-secondary" />
                <span className="text-2xl font-bold">86</span>
              </div>
              <p className="text-xs text-muted-foreground mt-1">+5 new this week</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Connections</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Activity className="h-5 w-5 text-accent" />
                <span className="text-2xl font-bold">432</span>
              </div>
              <p className="text-xs text-muted-foreground mt-1">Mentorships & training</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Success Rate</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-bold-orange" />
                <span className="text-2xl font-bold">87%</span>
              </div>
              <p className="text-xs text-muted-foreground mt-1">Completion rate</p>
            </CardContent>
          </Card>
        </div>

        {/* Main Content Grid */}
        <div className="grid md:grid-cols-2 gap-6">
          {/* Recent Activity */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="h-5 w-5" />
                Recent Platform Activity
              </CardTitle>
              <CardDescription>Latest actions across the platform</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-start gap-3 p-3 border border-border rounded-lg">
                  <CheckCircle className="h-5 w-5 text-primary mt-0.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">New mentorship connection</p>
                    <p className="text-xs text-muted-foreground">Sarah Johnson connected with John Kamau</p>
                    <p className="text-xs text-muted-foreground mt-1">2 hours ago</p>
                  </div>
                </div>
                <div className="flex items-start gap-3 p-3 border border-border rounded-lg">
                  <CheckCircle className="h-5 w-5 text-secondary mt-0.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">Training program completed</p>
                    <p className="text-xs text-muted-foreground">Digital Marketing Course - 15 graduates</p>
                    <p className="text-xs text-muted-foreground mt-1">5 hours ago</p>
                  </div>
                </div>
                <div className="flex items-start gap-3 p-3 border border-border rounded-lg">
                  <CheckCircle className="h-5 w-5 text-accent mt-0.5 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">New service provider joined</p>
                    <p className="text-xs text-muted-foreground">Tech Innovators Ltd registered</p>
                    <p className="text-xs text-muted-foreground mt-1">1 day ago</p>
                  </div>
                </div>
                <Button variant="outline" className="w-full">View All Activity</Button>
              </div>
            </CardContent>
          </Card>

          {/* Pending Approvals */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <AlertCircle className="h-5 w-5" />
                Pending Approvals
              </CardTitle>
              <CardDescription>Items requiring your attention</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">New Provider Application</h4>
                      <p className="text-sm text-muted-foreground">Growth Academy - Training Provider</p>
                    </div>
                    <span className="text-xs bg-accent/10 text-accent px-2 py-1 rounded">Urgent</span>
                  </div>
                  <div className="flex gap-2 mt-3">
                    <Button size="sm">Approve</Button>
                    <Button size="sm" variant="outline">Review</Button>
                  </div>
                </div>
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Job Posting Review</h4>
                      <p className="text-sm text-muted-foreground">Software Developer Internship</p>
                    </div>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">New</span>
                  </div>
                  <div className="flex gap-2 mt-3">
                    <Button size="sm">Approve</Button>
                    <Button size="sm" variant="outline">Review</Button>
                  </div>
                </div>
                <Button variant="outline" className="w-full">View All Pending</Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Platform Statistics */}
        <Card>
          <CardHeader>
            <CardTitle>Platform Overview</CardTitle>
            <CardDescription>Key metrics and performance indicators</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-6">
              <div className="space-y-2">
                <h4 className="text-sm font-medium text-muted-foreground">Youth Engagement</h4>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span>Active users</span>
                    <span className="font-semibold">892</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>New registrations (30d)</span>
                    <span className="font-semibold">143</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Success stories</span>
                    <span className="font-semibold">67</span>
                  </div>
                </div>
              </div>
              <div className="space-y-2">
                <h4 className="text-sm font-medium text-muted-foreground">Opportunities</h4>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span>Active mentorships</span>
                    <span className="font-semibold">234</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Training programs</span>
                    <span className="font-semibold">45</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Job postings</span>
                    <span className="font-semibold">153</span>
                  </div>
                </div>
              </div>
              <div className="space-y-2">
                <h4 className="text-sm font-medium text-muted-foreground">Growth Metrics</h4>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span>Monthly growth</span>
                    <span className="font-semibold text-primary">+12%</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Engagement rate</span>
                    <span className="font-semibold text-secondary">78%</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Satisfaction score</span>
                    <span className="font-semibold text-accent">4.6/5</span>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default AdminDashboard;
