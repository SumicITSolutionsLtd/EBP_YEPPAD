import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search, MapPin, Briefcase, Clock, Building } from "lucide-react";
import { Link } from "react-router-dom";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const jobListings = [
  {
    id: 1,
    title: "Digital Marketing Intern",
    company: "Tech Startup Inc.",
    location: "Remote",
    type: "Internship",
    duration: "3 months",
    description: "Learn digital marketing strategies and social media management.",
    requirements: ["Basic marketing knowledge", "Social media savvy", "Creative thinking"],
    posted: "2 days ago",
  },
  {
    id: 2,
    title: "Junior Web Developer",
    company: "Creative Agency",
    location: "Nairobi, Kenya",
    type: "Full-time",
    duration: "Permanent",
    description: "Build and maintain websites for various clients.",
    requirements: ["HTML, CSS, JavaScript", "React experience", "Problem solving"],
    posted: "1 week ago",
  },
  {
    id: 3,
    title: "Business Development Associate",
    company: "Growth Solutions Ltd",
    location: "Kampala, Uganda",
    type: "Part-time",
    duration: "6 months",
    description: "Support business growth initiatives and client relations.",
    requirements: ["Communication skills", "Sales interest", "Team player"],
    posted: "3 days ago",
  },
  {
    id: 4,
    title: "Graphic Design Intern",
    company: "Media House",
    location: "Remote",
    type: "Internship",
    duration: "4 months",
    description: "Create visual content for digital and print media.",
    requirements: ["Adobe Creative Suite", "Portfolio", "Creativity"],
    posted: "5 days ago",
  },
];

const JobPortal = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [jobType, setJobType] = useState("all");
  const [location, setLocation] = useState("all");
  const { toast } = useToast();

  const handleSaveJob = (job: typeof jobListings[0]) => {
    const savedJobs = JSON.parse(localStorage.getItem("savedJobs") || "[]");
    const jobData = {
      jobId: job.id,
      title: job.title,
      company: job.company,
      location: job.location,
      type: job.type,
      savedDate: new Date().toLocaleDateString(),
    };
    
    if (!savedJobs.some((j: any) => j.jobId === job.id)) {
      savedJobs.push(jobData);
      localStorage.setItem("savedJobs", JSON.stringify(savedJobs));
      toast({
        title: "Job Saved!",
        description: "You can view this job in My Items.",
      });
    } else {
      toast({
        title: "Already Saved",
        description: "This job is already in your saved list.",
      });
    }
  };

  const filteredJobs = jobListings.filter((job) => {
    const matchesSearch = job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         job.company.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = jobType === "all" || job.type === jobType;
    const matchesLocation = location === "all" || job.location.toLowerCase().includes(location.toLowerCase());
    return matchesSearch && matchesType && matchesLocation;
  });

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Job Portal</h1>
          <p className="text-muted-foreground">Discover opportunities that match your skills</p>
        </div>

        {/* Search and Filters */}
        <Card>
          <CardContent className="pt-6">
            <div className="grid md:grid-cols-4 gap-4">
              <div className="md:col-span-2 relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search jobs or companies..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Select value={jobType} onValueChange={setJobType}>
                <SelectTrigger>
                  <SelectValue placeholder="Job Type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value="Full-time">Full-time</SelectItem>
                  <SelectItem value="Part-time">Part-time</SelectItem>
                  <SelectItem value="Internship">Internship</SelectItem>
                </SelectContent>
              </Select>
              <Select value={location} onValueChange={setLocation}>
                <SelectTrigger>
                  <SelectValue placeholder="Location" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Locations</SelectItem>
                  <SelectItem value="remote">Remote</SelectItem>
                  <SelectItem value="nairobi">Nairobi</SelectItem>
                  <SelectItem value="kampala">Kampala</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        {/* Job Listings */}
        <div className="grid gap-4">
          {filteredJobs.map((job) => (
            <Card key={job.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div className="space-y-1">
                    <CardTitle className="text-xl">{job.title}</CardTitle>
                    <CardDescription className="flex items-center gap-2">
                      <Building className="h-4 w-4" />
                      {job.company}
                    </CardDescription>
                  </div>
                  <div className="flex gap-2">
                    <Badge variant="secondary">{job.type}</Badge>
                    <Badge variant="outline">{job.duration}</Badge>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground mb-4">{job.description}</p>
                <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                  <span className="flex items-center gap-1">
                    <MapPin className="h-4 w-4" />
                    {job.location}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    {job.posted}
                  </span>
                </div>
                <div className="flex flex-wrap gap-2 mb-4">
                  {job.requirements.map((req, idx) => (
                    <Badge key={idx} variant="outline" className="bg-muted/50">
                      {req}
                    </Badge>
                  ))}
                </div>
                <div className="flex gap-2">
                  <Link to={`/dashboard/user/jobs/${job.id}`} className="flex-1">
                    <Button variant="default" className="w-full">View Details</Button>
                  </Link>
                  <Button variant="outline" onClick={() => handleSaveJob(job)}>Save</Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default JobPortal;
