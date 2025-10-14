import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ArrowLeft, Building, Users, BarChart3, HandHeart } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

const PartnerPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    organizationName: "",
    contactPerson: "",
    email: "",
    phone: "",
    organizationType: "",
    partnershipType: "",
    description: "",
    expectedOutcome: "",
    timeline: ""
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    // Simulate form submission
    try {
      await new Promise(resolve => setTimeout(resolve, 2000));
      toast.success("Partnership request submitted! We'll be in touch within 48 hours.");
      setFormData({
        organizationName: "",
        contactPerson: "",
        email: "",
        phone: "",
        organizationType: "",
        partnershipType: "",
        description: "",
        expectedOutcome: "",
        timeline: ""
      });
    } catch (error) {
      toast.error("Failed to submit request. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="min-h-screen bg-background">
      <div className="bg-gradient-to-br from-primary via-primary-glow to-accent py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Button
            variant="ghost"
            onClick={() => navigate('/')}
            className="mb-6 text-white hover:bg-white/10"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Home
          </Button>
          
          <div className="text-center text-white">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">Partner With Us</h1>
            <p className="text-xl md:text-2xl max-w-3xl mx-auto opacity-90">
              Join our mission to empower rural youth across Northern Uganda. 
              Together, we can create lasting change in communities that need it most.
            </p>
          </div>
        </div>
      </div>

      <section className="py-16 bg-muted/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-foreground mb-4">Partnership Opportunities</h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              We offer various ways to collaborate and make a meaningful impact
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-16">
            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-primary to-accent rounded-lg flex items-center justify-center mb-4">
                  <Building className="h-6 w-6 text-white" />
                </div>
                <CardTitle>NGO Partnership</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Collaborate on mentorship programs, training initiatives, 
                  and community outreach projects
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-accent to-accent-light rounded-lg flex items-center justify-center mb-4">
                  <BarChart3 className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Government Collaboration</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Work with us on policy development, program oversight, 
                  and impact measurement initiatives
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-trust to-blue-500 rounded-lg flex items-center justify-center mb-4">
                  <Users className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Corporate Sponsorship</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Support youth development through funding, 
                  internships, and technology partnerships
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-green-500 to-emerald-500 rounded-lg flex items-center justify-center mb-4">
                  <HandHeart className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Community Impact</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Join grassroots initiatives and local 
                  community development programs
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      <section className="py-16 bg-background">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <Card className="bg-card shadow-xl">
            <CardHeader className="text-center">
              <CardTitle className="text-3xl font-bold">Partnership Application</CardTitle>
              <CardDescription className="text-lg">
                Tell us about your organization and how you'd like to collaborate with Kwetu Hub
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="organizationName">Organization Name *</Label>
                    <Input
                      id="organizationName"
                      value={formData.organizationName}
                      onChange={(e) => handleInputChange('organizationName', e.target.value)}
                      placeholder="Enter your organization name"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="contactPerson">Contact Person *</Label>
                    <Input
                      id="contactPerson"
                      value={formData.contactPerson}
                      onChange={(e) => handleInputChange('contactPerson', e.target.value)}
                      placeholder="Primary contact name"
                      required
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="email">Email Address *</Label>
                    <Input
                      id="email"
                      type="email"
                      value={formData.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                      placeholder="contact@organization.com"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input
                      id="phone"
                      value={formData.phone}
                      onChange={(e) => handleInputChange('phone', e.target.value)}
                      placeholder="+256 XXX XXX XXX"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="organizationType">Organization Type *</Label>
                    <Select onValueChange={(value) => handleInputChange('organizationType', value)} required>
                      <SelectTrigger>
                        <SelectValue placeholder="Select organization type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ngo">Non-Governmental Organization</SelectItem>
                        <SelectItem value="government">Government Agency</SelectItem>
                        <SelectItem value="corporate">Corporate/Private Sector</SelectItem>
                        <SelectItem value="international">International Organization</SelectItem>
                        <SelectItem value="academic">Academic Institution</SelectItem>
                        <SelectItem value="community">Community Organization</SelectItem>
                        <SelectItem value="other">Other</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="partnershipType">Partnership Type *</Label>
                    <Select onValueChange={(value) => handleInputChange('partnershipType', value)} required>
                      <SelectTrigger>
                        <SelectValue placeholder="Select partnership type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="mentorship">Mentorship Programs</SelectItem>
                        <SelectItem value="funding">Funding & Grants</SelectItem>
                        <SelectItem value="training">Training & Capacity Building</SelectItem>
                        <SelectItem value="technology">Technology Support</SelectItem>
                        <SelectItem value="policy">Policy & Advocacy</SelectItem>
                        <SelectItem value="research">Research & Development</SelectItem>
                        <SelectItem value="other">Other Collaboration</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Partnership Description *</Label>
                  <Textarea
                    id="description"
                    value={formData.description}
                    onChange={(e) => handleInputChange('description', e.target.value)}
                    placeholder="Describe how you'd like to partner with Kwetu Hub and what resources/expertise you can contribute..."
                    rows={4}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expectedOutcome">Expected Outcomes *</Label>
                  <Textarea
                    id="expectedOutcome"
                    value={formData.expectedOutcome}
                    onChange={(e) => handleInputChange('expectedOutcome', e.target.value)}
                    placeholder="What impact do you hope to achieve through this partnership? What are your goals and success metrics?"
                    rows={3}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="timeline">Project Timeline</Label>
                  <Select onValueChange={(value) => handleInputChange('timeline', value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Expected partnership duration" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="immediate">Immediate (1-3 months)</SelectItem>
                      <SelectItem value="short">Short-term (3-6 months)</SelectItem>
                      <SelectItem value="medium">Medium-term (6-12 months)</SelectItem>
                      <SelectItem value="long">Long-term (1+ years)</SelectItem>
                      <SelectItem value="ongoing">Ongoing collaboration</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <Button 
                  type="submit" 
                  className="w-full" 
                  disabled={loading}
                  variant="hero"
                  size="lg"
                >
                  {loading ? "Submitting..." : "Submit Partnership Request"}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
};

export default PartnerPage;