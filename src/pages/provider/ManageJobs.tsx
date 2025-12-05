import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Plus, Edit, Trash2, Eye } from "lucide-react";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface Job {
  id: number;
  title: string;
  type: string;
  location: string;
  applications: number;
  status: string;
}

const ManageJobs = () => {
  const { toast } = useToast();
  const [jobs, setJobs] = useState<Job[]>([
    { id: 1, title: "Digital Marketing Intern", type: "Internship", location: "Remote", applications: 12, status: "Active" },
    { id: 2, title: "Junior Developer", type: "Full-time", location: "Nairobi", applications: 8, status: "Active" },
    { id: 3, title: "Business Analyst", type: "Part-time", location: "Kampala", applications: 5, status: "Draft" },
  ]);

  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    type: "",
    location: "",
    description: "",
    requirements: "",
  });

  const handleSubmit = () => {
    if (formData.title && formData.type && formData.location) {
      const newJob: Job = {
        id: jobs.length + 1,
        title: formData.title,
        type: formData.type,
        location: formData.location,
        applications: 0,
        status: "Active",
      };
      setJobs([...jobs, newJob]);
      setIsDialogOpen(false);
      setFormData({ title: "", type: "", location: "", description: "", requirements: "" });
      toast({
        title: "Job Posted!",
        description: "Your job posting is now live.",
      });
    }
  };

  const handleDelete = (id: number) => {
    setJobs(jobs.filter(job => job.id !== id));
    toast({
      title: "Job Deleted",
      description: "The job posting has been removed.",
    });
  };

  return (
    <DashboardLayout userType="provider">
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Manage Job Postings</h1>
            <p className="text-muted-foreground">Create and manage your job opportunities</p>
          </div>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Post New Job
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Post New Job Opportunity</DialogTitle>
                <DialogDescription>
                  Fill in the details to create a new job posting for youth.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="job-title">Job Title *</Label>
                  <Input
                    id="job-title"
                    placeholder="e.g., Digital Marketing Intern"
                    value={formData.title}
                    onChange={(e) => setFormData({...formData, title: e.target.value})}
                  />
                </div>
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="job-type">Job Type *</Label>
                    <Select value={formData.type} onValueChange={(value) => setFormData({...formData, type: value})}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="Full-time">Full-time</SelectItem>
                        <SelectItem value="Part-time">Part-time</SelectItem>
                        <SelectItem value="Internship">Internship</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label htmlFor="location">Location *</Label>
                    <Input
                      id="location"
                      placeholder="e.g., Remote or Nairobi"
                      value={formData.location}
                      onChange={(e) => setFormData({...formData, location: e.target.value})}
                    />
                  </div>
                </div>
                <div>
                  <Label htmlFor="description">Job Description</Label>
                  <Textarea
                    id="description"
                    placeholder="Describe the role, responsibilities, and what you're looking for..."
                    rows={4}
                    value={formData.description}
                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                  />
                </div>
                <div>
                  <Label htmlFor="requirements">Requirements</Label>
                  <Textarea
                    id="requirements"
                    placeholder="List the skills and qualifications needed..."
                    rows={4}
                    value={formData.requirements}
                    onChange={(e) => setFormData({...formData, requirements: e.target.value})}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsDialogOpen(false)}>Cancel</Button>
                <Button onClick={handleSubmit}>Post Job</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        {/* Job Listings */}
        <div className="grid gap-4">
          {jobs.map((job) => (
            <Card key={job.id}>
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle>{job.title}</CardTitle>
                    <CardDescription className="mt-1">
                      {job.type} • {job.location}
                    </CardDescription>
                  </div>
                  <Badge variant={job.status === "Active" ? "default" : "secondary"}>
                    {job.status}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-sm text-muted-foreground">
                    <span className="font-semibold text-foreground">{job.applications}</span> applications received
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm">
                      <Eye className="h-4 w-4 mr-1" />
                      View
                    </Button>
                    <Button variant="outline" size="sm">
                      <Edit className="h-4 w-4 mr-1" />
                      Edit
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => handleDelete(job.id)}>
                      <Trash2 className="h-4 w-4 mr-1" />
                      Delete
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ManageJobs;
