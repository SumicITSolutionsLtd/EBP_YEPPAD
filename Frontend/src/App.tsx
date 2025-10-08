import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "@/hooks/useAuth";
import { LanguageProvider } from "@/contexts/LanguageContext";
import Index from "./pages/Index";
import MainApp from "./pages/MainApp";
import AuthPage from "./pages/AuthPage";
import AboutPage from "./pages/AboutPage";
import PartnerPage from "./pages/PartnerPage";
import YouthDashboard from "./pages/YouthDashboard";
import NGODashboard from "./pages/NGODashboard";
import GovernmentDashboard from "./pages/GovernmentDashboard";
import ActivitiesPage from "./pages/ActivitiesPage";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <LanguageProvider>
      <AuthProvider>
        <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Index />} />
            <Route path="/app" element={<MainApp />} />
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/partner" element={<PartnerPage />} />
            <Route path="/youth" element={<YouthDashboard onBack={() => window.location.href = '/'} />} />
            <Route path="/activities" element={<ActivitiesPage onBack={() => window.history.back()} />} />
            <Route path="/ngo" element={<NGODashboard />} />
            <Route path="/government" element={<GovernmentDashboard />} />
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
        </TooltipProvider>
      </AuthProvider>
    </LanguageProvider>
  </QueryClientProvider>
);

export default App;
