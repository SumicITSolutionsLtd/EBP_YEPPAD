import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "./pages/Index";
import Auth from "./pages/Auth";
import Contact from "./pages/Contact";
import PrivacyPolicy from "./pages/PrivacyPolicy";
import TermsOfService from "./pages/TermsOfService";
import UserDashboard from "./pages/UserDashboard";
import ProviderDashboard from "./pages/ProviderDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import TrainingCatalog from "./pages/TrainingCatalog";
import CourseDetail from "./pages/CourseDetail";
import MyLearning from "./pages/MyLearning";
import JobPortal from "./pages/JobPortal";
import JobDetail from "./pages/JobDetail";
import Mentorship from "./pages/Mentorship";
import MySkills from "./pages/MySkills";
import MyItems from "./pages/MyItems";
import ManageJobs from "./pages/provider/ManageJobs";
import ManageMentors from "./pages/provider/ManageMentors";
import Analytics from "./pages/admin/Analytics";
import ManageContent from "./pages/admin/ManageContent";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Index />} />
          <Route path="/auth" element={<Auth />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/privacy-policy" element={<PrivacyPolicy />} />
          <Route path="/terms-of-service" element={<TermsOfService />} />
          
          {/* User Routes */}
          <Route path="/dashboard/user" element={<UserDashboard />} />
          <Route path="/dashboard/user/skills" element={<MySkills />} />
          <Route path="/dashboard/user/mentorship" element={<Mentorship />} />
          <Route path="/dashboard/user/training" element={<TrainingCatalog />} />
          <Route path="/dashboard/user/training/:courseId" element={<CourseDetail />} />
          <Route path="/dashboard/user/my-learning" element={<MyLearning />} />
          <Route path="/dashboard/user/jobs" element={<JobPortal />} />
          <Route path="/dashboard/user/jobs/:jobId" element={<JobDetail />} />
          <Route path="/dashboard/user/my-items" element={<MyItems />} />
          
          {/* Provider Routes */}
          <Route path="/dashboard/provider" element={<ProviderDashboard />} />
          <Route path="/dashboard/provider/jobs" element={<ManageJobs />} />
          <Route path="/dashboard/provider/mentors" element={<ManageMentors />} />
          
          {/* Admin Routes */}
          <Route path="/dashboard/admin" element={<AdminDashboard />} />
          <Route path="/dashboard/admin/analytics" element={<Analytics />} />
          <Route path="/dashboard/admin/content" element={<ManageContent />} />
          
          {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
