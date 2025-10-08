import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { MapPin, Calendar, DollarSign, Users, Bookmark } from "lucide-react";

interface OpportunityCardProps {
  title: string;
  description: string;
  type: "loan" | "grant" | "training" | "market";
  location: string;
  deadline: string;
  amount?: string;
  participants?: number;
  isBookmarked?: boolean;
  onBookmark?: () => void;
  onApply?: () => void;
}

const OpportunityCard = ({
  title,
  description,
  type,
  location,
  deadline,
  amount,
  participants,
  isBookmarked = false,
  onBookmark,
  onApply
}: OpportunityCardProps) => {
  const getTypeColor = () => {
    switch (type) {
      case "loan": return "bg-trust text-trust-foreground";
      case "grant": return "bg-primary text-primary-foreground";
      case "training": return "bg-accent text-accent-foreground";
      case "market": return "bg-secondary text-secondary-foreground";
      default: return "bg-muted text-muted-foreground";
    }
  };

  const getTypeLabel = () => {
    return type.charAt(0).toUpperCase() + type.slice(1);
  };

  return (
    <Card className="bg-card border-border hover:shadow-medium transition-all duration-300">
      <CardHeader className="pb-3">
        <div className="flex justify-between items-start">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <Badge className={getTypeColor()}>
                {getTypeLabel()}
              </Badge>
            </div>
            <CardTitle className="text-lg font-semibold text-foreground mb-1">
              {title}
            </CardTitle>
            <CardDescription className="text-muted-foreground">
              {description}
            </CardDescription>
          </div>
          <Button
            variant="ghost"
            size="icon"
            className="text-muted-foreground hover:text-accent"
            onClick={onBookmark}
          >
            <Bookmark 
              className={`h-4 w-4 ${isBookmarked ? 'fill-current text-accent' : ''}`} 
            />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="pt-0">
        <div className="space-y-2 mb-4">
          <div className="flex items-center text-sm text-muted-foreground">
            <MapPin className="h-4 w-4 mr-2 text-primary" />
            {location}
          </div>
          <div className="flex items-center text-sm text-muted-foreground">
            <Calendar className="h-4 w-4 mr-2 text-primary" />
            Deadline: {deadline}
          </div>
          {amount && (
            <div className="flex items-center text-sm text-muted-foreground">
              <DollarSign className="h-4 w-4 mr-2 text-primary" />
              {amount}
            </div>
          )}
          {participants && (
            <div className="flex items-center text-sm text-muted-foreground">
              <Users className="h-4 w-4 mr-2 text-primary" />
              {participants} participants
            </div>
          )}
        </div>
        <Button 
          className="w-full" 
          variant="default"
          onClick={onApply}
        >
          Apply Now
        </Button>
      </CardContent>
    </Card>
  );
};

export default OpportunityCard;