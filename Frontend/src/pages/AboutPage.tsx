import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowLeft, Users, Target, Globe, Heart } from "lucide-react";
import { useNavigate } from "react-router-dom";

const AboutPage = () => {
  const navigate = useNavigate();

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
            <h1 className="text-4xl md:text-6xl font-bold mb-6">About Kwetu Hub</h1>
            <p className="text-xl md:text-2xl max-w-3xl mx-auto opacity-90">
              Empowering rural youth across Northern Uganda through technology, 
              mentorship, and entrepreneurship opportunities
            </p>
          </div>
        </div>
      </div>

      <section className="py-16 bg-muted/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-foreground mb-4">Our Mission</h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Breaking down barriers to entrepreneurship by connecting rural youth with opportunities, 
              mentorship, and skills development through innovative offline-first technology
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-primary to-accent rounded-lg flex items-center justify-center mb-4">
                  <Users className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Youth Empowerment</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Supporting young entrepreneurs with access to opportunities, 
                  funding, and skills development programs
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-accent to-accent-light rounded-lg flex items-center justify-center mb-4">
                  <Target className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Inclusive Growth</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Ensuring equal participation of women and persons with disabilities 
                  in entrepreneurship programs
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-trust to-blue-500 rounded-lg flex items-center justify-center mb-4">
                  <Globe className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Technology Access</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Bridging the digital divide with offline-first solutions 
                  and multi-language support
                </p>
              </CardContent>
            </Card>

            <Card className="text-center bg-card hover:shadow-medium transition-all duration-300">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-gradient-to-r from-green-500 to-emerald-500 rounded-lg flex items-center justify-center mb-4">
                  <Heart className="h-6 w-6 text-white" />
                </div>
                <CardTitle>Community Impact</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  Creating sustainable change through partnerships with NGOs 
                  and government institutions
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      <section className="py-16 bg-background">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-3xl font-bold text-foreground mb-6">Our Story</h2>
              <div className="space-y-4 text-muted-foreground">
                <p>
                  Kwetu Hub was born from the recognition that rural youth in Northern Uganda 
                  face significant barriers in accessing entrepreneurship opportunities. Despite 
                  their potential and drive, connectivity issues, language barriers, and lack 
                  of access to information have kept many from realizing their dreams.
                </p>
                <p>
                  Our platform leverages innovative offline-first technology to ensure that 
                  opportunities reach every corner of Northern Uganda. From USSD and SMS 
                  integration for basic phones to voice-enabled learning in local languages, 
                  we're breaking down every barrier we can identify.
                </p>
                <p>
                  Through partnerships with NGOs and government institutions, we're not just 
                  connecting youth to opportunities – we're building a comprehensive ecosystem 
                  that supports their entire entrepreneurial journey.
                </p>
              </div>
            </div>
            
            <div className="bg-muted/50 p-8 rounded-lg">
              <h3 className="text-2xl font-bold text-foreground mb-6">Impact Numbers</h3>
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-primary mb-2">3,400+</div>
                  <div className="text-sm text-muted-foreground">Youth Reached</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-accent mb-2">68%</div>
                  <div className="text-sm text-muted-foreground">Women Participation</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-trust mb-2">87%</div>
                  <div className="text-sm text-muted-foreground">Success Rate</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-green-600 mb-2">12%</div>
                  <div className="text-sm text-muted-foreground">PWD Inclusion</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="py-16 bg-gradient-to-r from-primary to-accent">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-white mb-4">Join Our Mission</h2>
          <p className="text-xl text-white/90 mb-8">
            Ready to be part of the change? Whether you're a youth looking for opportunities 
            or an organization wanting to partner with us, we're here to help.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" variant="secondary" className="text-lg px-8 py-4" onClick={() => navigate('/auth')}>
              Get Started Today
            </Button>
            <Button size="lg" variant="outline" className="text-lg px-8 py-4 border-white text-white hover:bg-white hover:text-primary" onClick={() => navigate('/partner')}>
              Partner With Us
            </Button>
          </div>
        </div>
      </section>
    </div>
  );
};

export default AboutPage;