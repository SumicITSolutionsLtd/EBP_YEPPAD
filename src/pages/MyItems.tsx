import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { MapPin, Briefcase, Clock, Building } from "lucide-react";
import { Link } from "react-router-dom";
import DashboardLayout from "@/components/DashboardLayout";

interface JobHistory {
  jobId: string;
  title: string;
  company: string;
  location: string;
  type: string;
  appliedDate?: string;
  savedDate?: string;
}

const MyItems = () => {
  const [appliedJobs, setAppliedJobs] = useState<JobHistory[]>([]);
  const [savedJobs, setSavedJobs] = useState<JobHistory[]>([]);

  useEffect(() => {
    // Load job history from localStorage
    const applied = localStorage.getItem("appliedJobs");
    const saved = localStorage.getItem("savedJobs");
    
    if (applied) setAppliedJobs(JSON.parse(applied));
    if (saved) setSavedJobs(JSON.parse(saved));
  }, []);

  return (
    <DashboardLayout userType="youth">
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">My Items</h1>
          <p className="text-muted-foreground">Track your saved and applied jobs</p>
        </div>

        <Tabs defaultValue="applied" className="w-full">
          <TabsList className="grid w-full md:w-[400px] grid-cols-2">
            <TabsTrigger value="applied">Applied Jobs ({appliedJobs.length})</TabsTrigger>
            <TabsTrigger value="saved">Saved Jobs ({savedJobs.length})</TabsTrigger>
          </TabsList>

          <TabsContent value="applied" className="space-y-4 mt-6">
            {appliedJobs.length === 0 ? (
              <Card>
                <CardContent className="pt-6 text-center py-12">
                  <p className="text-muted-foreground">You haven't applied to any jobs yet.</p>
                  <Link to="/dashboard/user/jobs" className="text-primary hover:underline">
                    Browse available jobs
                  </Link>
                </CardContent>
              </Card>
            ) : (
              appliedJobs.map((job, idx) => (
                <Card key={idx} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div className="space-y-1">
                        <CardTitle className="text-xl">{job.title}</CardTitle>
                        <CardDescription className="flex items-center gap-2">
                          <Building className="h-4 w-4" />
                          {job.company}
                        </CardDescription>
                      </div>
                      <Badge variant="secondary">{job.type}</Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                      <span className="flex items-center gap-1">
                        <MapPin className="h-4 w-4" />
                        {job.location}
                      </span>
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        Applied {job.appliedDate}
                      </span>
                    </div>
                    <Link 
                      to={`/dashboard/user/jobs/${job.jobId}`}
                      className="text-primary hover:underline text-sm"
                    >
                      View Job Details →
                    </Link>
                  </CardContent>
                </Card>
              ))
            )}
          </TabsContent>

          <TabsContent value="saved" className="space-y-4 mt-6">
            {savedJobs.length === 0 ? (
              <Card>
                <CardContent className="pt-6 text-center py-12">
                  <p className="text-muted-foreground">You haven't saved any jobs yet.</p>
                  <Link to="/dashboard/user/jobs" className="text-primary hover:underline">
                    Browse available jobs
                  </Link>
                </CardContent>
              </Card>
            ) : (
              savedJobs.map((job, idx) => (
                <Card key={idx} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div className="space-y-1">
                        <CardTitle className="text-xl">{job.title}</CardTitle>
                        <CardDescription className="flex items-center gap-2">
                          <Building className="h-4 w-4" />
                          {job.company}
                        </CardDescription>
                      </div>
                      <Badge variant="outline">{job.type}</Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground mb-4">
                      <span className="flex items-center gap-1">
                        <MapPin className="h-4 w-4" />
                        {job.location}
                      </span>
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        Saved {job.savedDate}
                      </span>
                    </div>
                    <Link 
                      to={`/dashboard/user/jobs/${job.jobId}`}
                      className="text-primary hover:underline text-sm"
                    >
                      View Job Details →
                    </Link>
                  </CardContent>
                </Card>
              ))
            )}
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default MyItems;
