import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { LucideIcon } from "lucide-react";

interface UserTypeCardProps {
  title: string;
  description: string;
  icon: LucideIcon;
  features: string[];
  buttonText: string;
  onSelect: () => void;
  variant?: "youth" | "ngo" | "government";
}

const UserTypeCard = ({ 
  title, 
  description, 
  icon: Icon, 
  features, 
  buttonText, 
  onSelect,
  variant = "youth"
}: UserTypeCardProps) => {
  const getButtonVariant = () => {
    switch (variant) {
      case "youth": return "hero";
      case "ngo": return "accent";
      case "government": return "trust";
      default: return "default";
    }
  };

  return (
    <Card className="h-full bg-card hover:shadow-medium transition-all duration-300 border-border">
      <CardHeader className="text-center pb-4">
        <div className="mx-auto w-16 h-16 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center mb-4">
          <Icon className="h-8 w-8 text-white" />
        </div>
        <CardTitle className="text-xl font-bold text-foreground">{title}</CardTitle>
        <CardDescription className="text-muted-foreground">{description}</CardDescription>
      </CardHeader>
      <CardContent className="pt-0">
        <ul className="space-y-2 mb-6">
          {features.map((feature, index) => (
            <li key={index} className="flex items-center text-sm text-muted-foreground">
              <div className="w-1.5 h-1.5 bg-primary rounded-full mr-3"></div>
              {feature}
            </li>
          ))}
        </ul>
        <Button 
          className="w-full" 
          variant={getButtonVariant()}
          onClick={onSelect}
        >
          {buttonText}
        </Button>
      </CardContent>
    </Card>
  );
};

export default UserTypeCard;