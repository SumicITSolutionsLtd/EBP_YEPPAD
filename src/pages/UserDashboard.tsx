import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Users, BookOpen, Briefcase, TrendingUp, Calendar, Award } from "lucide-react";
import { Link } from "react-router-dom";
import DashboardLayout from "@/components/DashboardLayout";

const UserDashboard = () => {
  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Welcome Back!</h1>
          <p className="text-muted-foreground">Here's your entrepreneurial journey overview</p>
        </div>

        {/* Stats Grid */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Mentorships</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Users className="h-5 w-5 text-primary" />
                <span className="text-2xl font-bold">3</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Courses Enrolled</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <BookOpen className="h-5 w-5 text-secondary" />
                <span className="text-2xl font-bold">5</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Job Applications</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Briefcase className="h-5 w-5 text-accent" />
                <span className="text-2xl font-bold">8</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Skills Earned</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Award className="h-5 w-5 text-bold-orange" />
                <span className="text-2xl font-bold">12</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Main Content Grid */}
        <div className="grid md:grid-cols-2 gap-6">
          {/* Available Opportunities */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <TrendingUp className="h-5 w-5" />
                Available Opportunities
              </CardTitle>
              <CardDescription>New matches for your profile</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border border-border rounded-lg hover:bg-muted/50 transition-colors cursor-pointer">
                  <h4 className="font-semibold mb-1">Digital Marketing Internship</h4>
                  <p className="text-sm text-muted-foreground mb-2">Tech Startup Inc. • Remote</p>
                  <div className="flex gap-2">
                    <span className="text-xs bg-accent/10 text-accent px-2 py-1 rounded">Internship</span>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">Part-time</span>
                  </div>
                </div>
                <Link to="/dashboard/user/training/2" className="block p-4 border border-border rounded-lg hover:bg-muted/50 transition-colors cursor-pointer">
                  <h4 className="font-semibold mb-1">Business Development Training</h4>
                  <p className="text-sm text-muted-foreground mb-2">Growth Academy • Online</p>
                  <div className="flex gap-2">
                    <span className="text-xs bg-secondary/10 text-secondary px-2 py-1 rounded">Training</span>
                    <span className="text-xs bg-bold-orange/10 text-bold-orange px-2 py-1 rounded">4 weeks</span>
                  </div>
                </Link>
                <Link to="/dashboard/user/training">
                  <Button variant="outline" className="w-full">View All Opportunities</Button>
                </Link>
              </div>
            </CardContent>
          </Card>

          {/* Upcoming Sessions */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5" />
                Upcoming Sessions
              </CardTitle>
              <CardDescription>Your scheduled mentorship & training</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Mentorship Session</h4>
                      <p className="text-sm text-muted-foreground">with Sarah Johnson</p>
                    </div>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">Today</span>
                  </div>
                  <p className="text-sm text-muted-foreground">3:00 PM - 4:00 PM</p>
                </div>
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Workshop: Financial Planning</h4>
                      <p className="text-sm text-muted-foreground">Online Workshop</p>
                    </div>
                    <span className="text-xs bg-secondary/10 text-secondary px-2 py-1 rounded">Tomorrow</span>
                  </div>
                  <p className="text-sm text-muted-foreground">10:00 AM - 12:00 PM</p>
                </div>
                <Button variant="outline" className="w-full">View Calendar</Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default UserDashboard;
