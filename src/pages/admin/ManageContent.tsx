import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Plus, Edit, Trash2, Eye, CheckCircle, XCircle } from "lucide-react";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface ContentItem {
  id: number;
  title: string;
  type: string;
  status: string;
  author: string;
  date: string;
}

const ManageContent = () => {
  const { toast } = useToast();
  const [jobs, setJobs] = useState<ContentItem[]>([
    { id: 1, title: "Marketing Intern", type: "Job", status: "Pending", author: "Tech Startup Inc.", date: "2024-01-15" },
    { id: 2, title: "Web Developer", type: "Job", status: "Approved", author: "Creative Agency", date: "2024-01-14" },
  ]);

  const [courses, setCourses] = useState<ContentItem[]>([
    { id: 1, title: "Digital Marketing 101", type: "Course", status: "Approved", author: "Growth Academy", date: "2024-01-10" },
    { id: 2, title: "Business Fundamentals", type: "Course", status: "Pending", author: "Skills Institute", date: "2024-01-16" },
  ]);

  const [mentors, setMentors] = useState<ContentItem[]>([
    { id: 1, title: "Sarah Johnson - Marketing", type: "Mentor", status: "Approved", author: "Tech Solutions", date: "2024-01-12" },
    { id: 2, title: "David Kimani - Tech", type: "Mentor", status: "Pending", author: "Innovation Labs", date: "2024-01-17" },
  ]);

  const handleApprove = (type: string, id: number) => {
    if (type === "Job") {
      setJobs(jobs.map(job => job.id === id ? { ...job, status: "Approved" } : job));
    } else if (type === "Course") {
      setCourses(courses.map(course => course.id === id ? { ...course, status: "Approved" } : course));
    } else if (type === "Mentor") {
      setMentors(mentors.map(mentor => mentor.id === id ? { ...mentor, status: "Approved" } : mentor));
    }
    toast({
      title: "Content Approved",
      description: "The content has been approved and is now visible to users.",
    });
  };

  const handleReject = (type: string, id: number) => {
    if (type === "Job") {
      setJobs(jobs.map(job => job.id === id ? { ...job, status: "Rejected" } : job));
    } else if (type === "Course") {
      setCourses(courses.map(course => course.id === id ? { ...course, status: "Rejected" } : course));
    } else if (type === "Mentor") {
      setMentors(mentors.map(mentor => mentor.id === id ? { ...mentor, status: "Rejected" } : mentor));
    }
    toast({
      title: "Content Rejected",
      description: "The content has been rejected.",
      variant: "destructive",
    });
  };

  const handleDelete = (type: string, id: number) => {
    if (type === "Job") {
      setJobs(jobs.filter(job => job.id !== id));
    } else if (type === "Course") {
      setCourses(courses.filter(course => course.id !== id));
    } else if (type === "Mentor") {
      setMentors(mentors.filter(mentor => mentor.id !== id));
    }
    toast({
      title: "Content Deleted",
      description: "The content has been permanently removed.",
    });
  };

  const ContentTable = ({ items, type }: { items: ContentItem[], type: string }) => (
    <div className="space-y-4">
      {items.map((item) => (
        <Card key={item.id}>
          <CardHeader>
            <div className="flex justify-between items-start">
              <div>
                <CardTitle className="text-lg">{item.title}</CardTitle>
                <CardDescription className="mt-1">
                  By {item.author} • {item.date}
                </CardDescription>
              </div>
              <Badge variant={
                item.status === "Approved" ? "default" : 
                item.status === "Pending" ? "secondary" : 
                "destructive"
              }>
                {item.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className="flex gap-2">
              <Button variant="outline" size="sm">
                <Eye className="h-4 w-4 mr-1" />
                View
              </Button>
              {item.status === "Pending" && (
                <>
                  <Button variant="outline" size="sm" onClick={() => handleApprove(type, item.id)}>
                    <CheckCircle className="h-4 w-4 mr-1 text-green-500" />
                    Approve
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => handleReject(type, item.id)}>
                    <XCircle className="h-4 w-4 mr-1 text-red-500" />
                    Reject
                  </Button>
                </>
              )}
              <Button variant="outline" size="sm">
                <Edit className="h-4 w-4 mr-1" />
                Edit
              </Button>
              <Button variant="outline" size="sm" onClick={() => handleDelete(type, item.id)}>
                <Trash2 className="h-4 w-4 mr-1" />
                Delete
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );

  return (
    <DashboardLayout userType="admin">
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Content Management</h1>
            <p className="text-muted-foreground">Review and manage platform content</p>
          </div>
          <Button>
            <Plus className="h-4 w-4 mr-2" />
            Add Content
          </Button>
        </div>

        {/* Summary Stats */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Pending Reviews</CardTitle>
            </CardHeader>
            <CardContent>
              <span className="text-2xl font-bold">
                {jobs.filter(j => j.status === "Pending").length + 
                 courses.filter(c => c.status === "Pending").length + 
                 mentors.filter(m => m.status === "Pending").length}
              </span>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Jobs</CardTitle>
            </CardHeader>
            <CardContent>
              <span className="text-2xl font-bold">{jobs.filter(j => j.status === "Approved").length}</span>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Courses</CardTitle>
            </CardHeader>
            <CardContent>
              <span className="text-2xl font-bold">{courses.filter(c => c.status === "Approved").length}</span>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Mentors</CardTitle>
            </CardHeader>
            <CardContent>
              <span className="text-2xl font-bold">{mentors.filter(m => m.status === "Approved").length}</span>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="jobs" className="space-y-4">
          <TabsList>
            <TabsTrigger value="jobs">Job Postings</TabsTrigger>
            <TabsTrigger value="courses">Training Courses</TabsTrigger>
            <TabsTrigger value="mentors">Mentors</TabsTrigger>
          </TabsList>

          <TabsContent value="jobs">
            <ContentTable items={jobs} type="Job" />
          </TabsContent>

          <TabsContent value="courses">
            <ContentTable items={courses} type="Course" />
          </TabsContent>

          <TabsContent value="mentors">
            <ContentTable items={mentors} type="Mentor" />
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default ManageContent;
