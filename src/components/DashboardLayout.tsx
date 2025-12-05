import { ReactNode, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Home, Users, BookOpen, Briefcase, Settings, LogOut, BarChart3, FileText, Award, FolderHeart } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";
import { useAuth } from "@/hooks/useAuth";

interface DashboardLayoutProps {
  children: ReactNode;
  userType: "youth" | "provider" | "admin";
}

const DashboardLayout = ({ children, userType }: DashboardLayoutProps) => {
  const { logout, requireAuth, isLoading, isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      requireAuth();
    }
  }, [isLoading, isAuthenticated]);

  const handleLogout = () => {
    logout();
  };

  const menuItems = {
    youth: [
      { icon: Home, label: "Dashboard", path: "/dashboard/user" },
      { icon: Award, label: "My Skills", path: "/dashboard/user/skills" },
      { icon: Users, label: "Mentorship", path: "/dashboard/user/mentorship" },
      { icon: BookOpen, label: "Training Catalog", path: "/dashboard/user/training" },
      { icon: FileText, label: "My Learning", path: "/dashboard/user/my-learning" },
      { icon: Briefcase, label: "Job Portal", path: "/dashboard/user/jobs" },
      { icon: FolderHeart, label: "My Items", path: "/dashboard/user/my-items" },
    ],
    provider: [
      { icon: Home, label: "Dashboard", path: "/dashboard/provider" },
      { icon: Briefcase, label: "Manage Jobs", path: "/dashboard/provider/jobs" },
      { icon: Users, label: "Manage Mentors", path: "/dashboard/provider/mentors" },
      { icon: BookOpen, label: "Programs", path: "/dashboard/provider/programs" },
      { icon: BarChart3, label: "Analytics", path: "/dashboard/provider/analytics" },
      { icon: Settings, label: "Settings", path: "/dashboard/provider/settings" },
    ],
    admin: [
      { icon: Home, label: "Dashboard", path: "/dashboard/admin" },
      { icon: Users, label: "Users", path: "/dashboard/admin/users" },
      { icon: Briefcase, label: "Providers", path: "/dashboard/admin/providers" },
      { icon: FileText, label: "Content", path: "/dashboard/admin/content" },
      { icon: BarChart3, label: "Analytics", path: "/dashboard/admin/analytics" },
      { icon: Settings, label: "Settings", path: "/dashboard/admin/settings" },
    ],
  };

  const currentMenu = menuItems[userType];

  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar */}
      <aside className="w-64 bg-sidebar border-r border-sidebar-border flex flex-col">
        <div className="p-6 border-b border-sidebar-border">
          <Link to="/" className="flex items-center gap-3">
            <img src={ebpLogo} alt="EBP Logo" className="h-10 w-auto" />
            <span className="text-lg font-bold text-sidebar-foreground">Youth Connect</span>
          </Link>
        </div>
        
        <nav className="flex-1 p-4 space-y-2">
          {currentMenu.map((item) => (
            <Link key={item.path} to={item.path}>
              <Button
                variant="ghost"
                className="w-full justify-start text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
              >
                <item.icon className="h-5 w-5 mr-3" />
                {item.label}
              </Button>
            </Link>
          ))}
        </nav>

        <div className="p-4 border-t border-sidebar-border">
          <Button
            variant="ghost"
            className="w-full justify-start text-sidebar-foreground hover:bg-sidebar-accent"
            onClick={handleLogout}
          >
            <LogOut className="h-5 w-5 mr-3" />
            Logout
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <div className="container mx-auto p-8">
          {children}
        </div>
      </main>
    </div>
  );
};

export default DashboardLayout;
