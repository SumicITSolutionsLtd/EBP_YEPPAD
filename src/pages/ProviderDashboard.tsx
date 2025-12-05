import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Users, BookOpen, Briefcase, PlusCircle, Calendar, MessageSquare } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";

const ProviderDashboard = () => {
  return (
    <DashboardLayout userType="provider">
      <div className="space-y-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Service Provider Dashboard</h1>
            <p className="text-muted-foreground">Manage your mentorship, training, and job opportunities</p>
          </div>
          <Button className="bg-accent">
            <PlusCircle className="h-4 w-4 mr-2" />
            Create New Opportunity
          </Button>
        </div>

        {/* Stats Grid */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Mentees</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Users className="h-5 w-5 text-primary" />
                <span className="text-2xl font-bold">24</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Training Programs</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <BookOpen className="h-5 w-5 text-secondary" />
                <span className="text-2xl font-bold">7</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Job Postings</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <Briefcase className="h-5 w-5 text-accent" />
                <span className="text-2xl font-bold">12</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Applications</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <MessageSquare className="h-5 w-5 text-bold-orange" />
                <span className="text-2xl font-bold">38</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Main Content Grid */}
        <div className="grid md:grid-cols-2 gap-6">
          {/* Recent Applications */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MessageSquare className="h-5 w-5" />
                Recent Applications
              </CardTitle>
              <CardDescription>Youth interested in your opportunities</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border border-border rounded-lg hover:bg-muted/50 transition-colors">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">John Kamau</h4>
                      <p className="text-sm text-muted-foreground">Applied for Marketing Internship</p>
                    </div>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">New</span>
                  </div>
                  <div className="flex gap-2 mt-3">
                    <Button size="sm">Review</Button>
                    <Button size="sm" variant="outline">View Profile</Button>
                  </div>
                </div>
                <div className="p-4 border border-border rounded-lg hover:bg-muted/50 transition-colors">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Mary Wanjiku</h4>
                      <p className="text-sm text-muted-foreground">Applied for Business Training</p>
                    </div>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">New</span>
                  </div>
                  <div className="flex gap-2 mt-3">
                    <Button size="sm">Review</Button>
                    <Button size="sm" variant="outline">View Profile</Button>
                  </div>
                </div>
                <Button variant="outline" className="w-full">View All Applications</Button>
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
              <CardDescription>Your scheduled mentorship & training sessions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Group Mentorship</h4>
                      <p className="text-sm text-muted-foreground">5 mentees</p>
                    </div>
                    <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">Today</span>
                  </div>
                  <p className="text-sm text-muted-foreground">2:00 PM - 3:30 PM</p>
                  <Button size="sm" className="mt-3">Start Session</Button>
                </div>
                <div className="p-4 border border-border rounded-lg">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h4 className="font-semibold">Business Planning Workshop</h4>
                      <p className="text-sm text-muted-foreground">Online • 15 participants</p>
                    </div>
                    <span className="text-xs bg-secondary/10 text-secondary px-2 py-1 rounded">Tomorrow</span>
                  </div>
                  <p className="text-sm text-muted-foreground">9:00 AM - 12:00 PM</p>
                  <Button size="sm" variant="outline" className="mt-3">View Details</Button>
                </div>
                <Button variant="outline" className="w-full">View Full Schedule</Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* My Opportunities */}
        <Card>
          <CardHeader>
            <CardTitle>My Opportunities</CardTitle>
            <CardDescription>Manage your active mentorship, training, and job postings</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-4 border border-border rounded-lg">
                <div className="flex items-center gap-4">
                  <div className="bg-primary/10 p-3 rounded-lg">
                    <Users className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <h4 className="font-semibold">One-on-One Mentorship Program</h4>
                    <p className="text-sm text-muted-foreground">12 active mentees • Mentorship</p>
                  </div>
                </div>
                <Button variant="outline">Manage</Button>
              </div>
              <div className="flex items-center justify-between p-4 border border-border rounded-lg">
                <div className="flex items-center gap-4">
                  <div className="bg-secondary/10 p-3 rounded-lg">
                    <BookOpen className="h-6 w-6 text-secondary" />
                  </div>
                  <div>
                    <h4 className="font-semibold">Digital Marketing Masterclass</h4>
                    <p className="text-sm text-muted-foreground">45 enrolled • Training</p>
                  </div>
                </div>
                <Button variant="outline">Manage</Button>
              </div>
              <div className="flex items-center justify-between p-4 border border-border rounded-lg">
                <div className="flex items-center gap-4">
                  <div className="bg-accent/10 p-3 rounded-lg">
                    <Briefcase className="h-6 w-6 text-accent" />
                  </div>
                  <div>
                    <h4 className="font-semibold">Marketing Assistant Position</h4>
                    <p className="text-sm text-muted-foreground">18 applications • Job Posting</p>
                  </div>
                </div>
                <Button variant="outline">Manage</Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default ProviderDashboard;
