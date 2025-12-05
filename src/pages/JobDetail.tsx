import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { MapPin, Briefcase, Clock, Building, CheckCircle } from "lucide-react";
import { useParams, Link } from "react-router-dom";
import DashboardLayout from "@/components/DashboardLayout";
import { useToast } from "@/hooks/use-toast";

const JobDetail = () => {
  const { jobId } = useParams();
  const { toast } = useToast();

  const handleApply = () => {
    // Save to applied jobs history
    const appliedJobs = JSON.parse(localStorage.getItem("appliedJobs") || "[]");
    const jobData = {
      jobId: job.id,
      title: job.title,
      company: job.company,
      location: job.location,
      type: job.type,
      appliedDate: new Date().toLocaleDateString(),
    };
    
    // Avoid duplicates
    if (!appliedJobs.some((j: any) => j.jobId === job.id)) {
      appliedJobs.push(jobData);
      localStorage.setItem("appliedJobs", JSON.stringify(appliedJobs));
    }
    
    toast({
      title: "Application Submitted!",
      description: "Your application has been sent to the employer.",
    });
  };

  const handleSave = () => {
    // Save to saved jobs history
    const savedJobs = JSON.parse(localStorage.getItem("savedJobs") || "[]");
    const jobData = {
      jobId: job.id,
      title: job.title,
      company: job.company,
      location: job.location,
      type: job.type,
      savedDate: new Date().toLocaleDateString(),
    };
    
    // Avoid duplicates
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

  // Mock job data
  const job = {
    id: jobId,
    title: "Digital Marketing Intern",
    company: "Tech Startup Inc.",
    location: "Remote",
    type: "Internship",
    duration: "3 months",
    salary: "Competitive",
    description: "We are looking for a passionate Digital Marketing Intern to join our growing team. You'll learn cutting-edge marketing strategies and gain hands-on experience with social media management, content creation, and analytics.",
    responsibilities: [
      "Assist in creating and scheduling social media content",
      "Monitor and report on social media metrics",
      "Support email marketing campaigns",
      "Conduct market research and competitor analysis",
      "Help with content creation for blog and website",
    ],
    requirements: [
      "Currently pursuing or recently completed a degree in Marketing, Business, or related field",
      "Strong written and verbal communication skills",
      "Familiarity with social media platforms (Facebook, Instagram, Twitter, LinkedIn)",
      "Basic understanding of digital marketing concepts",
      "Creative thinking and problem-solving abilities",
      "Self-motivated and eager to learn",
    ],
    benefits: [
      "Hands-on learning experience",
      "Mentorship from experienced marketers",
      "Certificate of completion",
      "Potential for full-time employment",
      "Flexible work schedule",
    ],
    posted: "2 days ago",
  };

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div className="flex items-center gap-2">
          <Link to="/dashboard/user/jobs">
            <Button variant="ghost" size="sm">← Back to Jobs</Button>
          </Link>
        </div>

        <Card>
          <CardHeader>
            <div className="flex justify-between items-start">
              <div className="space-y-2">
                <CardTitle className="text-3xl">{job.title}</CardTitle>
                <CardDescription className="flex items-center gap-2 text-base">
                  <Building className="h-5 w-5" />
                  {job.company}
                </CardDescription>
              </div>
              <div className="flex gap-2">
                <Badge variant="secondary" className="text-sm">{job.type}</Badge>
                <Badge variant="outline" className="text-sm">{job.duration}</Badge>
              </div>
            </div>
            <div className="flex items-center gap-4 text-muted-foreground pt-2">
              <span className="flex items-center gap-1">
                <MapPin className="h-4 w-4" />
                {job.location}
              </span>
              <span className="flex items-center gap-1">
                <Briefcase className="h-4 w-4" />
                {job.salary}
              </span>
              <span className="flex items-center gap-1">
                <Clock className="h-4 w-4" />
                Posted {job.posted}
              </span>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex gap-3">
              <Button size="lg" className="flex-1" onClick={handleApply}>Apply Now</Button>
              <Button size="lg" variant="outline" onClick={handleSave}>Save Job</Button>
            </div>

            <Separator />

            <div>
              <h3 className="text-xl font-semibold mb-3">About the Position</h3>
              <p className="text-muted-foreground leading-relaxed">{job.description}</p>
            </div>

            <Separator />

            <div>
              <h3 className="text-xl font-semibold mb-3">Key Responsibilities</h3>
              <ul className="space-y-2">
                {job.responsibilities.map((item, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <CheckCircle className="h-5 w-5 text-primary mt-0.5 flex-shrink-0" />
                    <span className="text-muted-foreground">{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <Separator />

            <div>
              <h3 className="text-xl font-semibold mb-3">Requirements</h3>
              <ul className="space-y-2">
                {job.requirements.map((item, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <CheckCircle className="h-5 w-5 text-primary mt-0.5 flex-shrink-0" />
                    <span className="text-muted-foreground">{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <Separator />

            <div>
              <h3 className="text-xl font-semibold mb-3">What We Offer</h3>
              <ul className="space-y-2">
                {job.benefits.map((item, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <CheckCircle className="h-5 w-5 text-secondary mt-0.5 flex-shrink-0" />
                    <span className="text-muted-foreground">{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            <Separator />

            <div className="bg-muted/50 p-6 rounded-lg">
              <h3 className="text-xl font-semibold mb-3">Ready to Apply?</h3>
              <p className="text-muted-foreground mb-4">
                Take the next step in your career journey. Submit your application today!
              </p>
              <Button size="lg" className="w-full md:w-auto" onClick={handleApply}>
                Submit Application
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default JobDetail;
