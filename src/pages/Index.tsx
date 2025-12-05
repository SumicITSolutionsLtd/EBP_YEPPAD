import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { ArrowRight, Users, BookOpen, Briefcase, Target, ChevronLeft, ChevronRight } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";
import useEmblaCarousel from "embla-carousel-react";
import Autoplay from "embla-carousel-autoplay";
import { useCallback, useEffect, useState } from "react";

const heroSlides = [
  {
    title: "Empowering Youth Through",
    highlight: "Opportunity",
    description: "Connect with mentors, access training programs, and discover job opportunities tailored for young entrepreneurs and students.",
    bgGradient: "from-primary via-secondary to-bright-blue",
  },
  {
    title: "Build Your Skills With",
    highlight: "Expert Training",
    description: "Access world-class courses and workshops designed to accelerate your entrepreneurial journey and career growth.",
    bgGradient: "from-secondary via-bright-blue to-primary",
  },
  {
    title: "Connect With Industry",
    highlight: "Mentors",
    description: "Get guidance from experienced professionals who are passionate about helping young people succeed.",
    bgGradient: "from-bright-blue via-primary to-secondary",
  },
];

const Index = () => {
  const [emblaRef, emblaApi] = useEmblaCarousel({ loop: true }, [
    Autoplay({ delay: 5000, stopOnInteraction: false }),
  ]);
  const [selectedIndex, setSelectedIndex] = useState(0);

  const scrollPrev = useCallback(() => emblaApi?.scrollPrev(), [emblaApi]);
  const scrollNext = useCallback(() => emblaApi?.scrollNext(), [emblaApi]);
  const scrollTo = useCallback((index: number) => emblaApi?.scrollTo(index), [emblaApi]);

  useEffect(() => {
    if (!emblaApi) return;
    const onSelect = () => setSelectedIndex(emblaApi.selectedScrollSnap());
    emblaApi.on("select", onSelect);
    return () => { emblaApi.off("select", onSelect); };
  }, [emblaApi]);

  return (
    <div className="min-h-screen bg-background">
      {/* Navigation */}
      <nav className="border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <img src={ebpLogo} alt="EBP Logo" className="h-12 w-auto" />
            <span className="text-xl font-bold text-primary">Youth Connect</span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/auth">
              <Button variant="ghost">Sign In</Button>
            </Link>
            <Link to="/auth">
              <Button className="bg-bold-orange hover:bg-bold-orange/90">Get Started</Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Carousel */}
      <section className="relative overflow-hidden">
        <div ref={emblaRef} className="overflow-hidden">
          <div className="flex">
            {heroSlides.map((slide, index) => (
              <div key={index} className={`flex-[0_0_100%] min-w-0 relative bg-gradient-to-br ${slide.bgGradient} py-24 md:py-32`}>
                <div className="container mx-auto px-4">
                  <div className="grid md:grid-cols-2 gap-12 items-center">
                    <div className="text-white space-y-6">
                      <h1 className="text-4xl md:text-6xl font-bold leading-tight animate-fade-in">
                        {slide.title}{" "}
                        <span className="text-bold-orange">{slide.highlight}</span>
                      </h1>
                      <p className="text-xl text-white/90">
                        {slide.description}
                      </p>
                      <div className="flex gap-4">
                        <Link to="/auth">
                          <Button size="lg" className="bg-bold-orange hover:bg-bold-orange/80 text-white">
                            Join Now <ArrowRight className="ml-2 h-5 w-5" />
                          </Button>
                        </Link>
                        <Link to="#about">
                          <Button size="lg" variant="outline" className="border-white/80 text-white bg-white/10 hover:bg-white/20 hover:border-white">
                            Learn More
                          </Button>
                        </Link>
                      </div>
                    </div>
                    <div className="hidden md:block relative">
                      <div className="absolute inset-0 bg-gradient-to-br from-white/30 via-bold-orange/20 to-white/30 rounded-full blur-3xl scale-110"></div>
                      <img src={ebpLogo} alt="EBP" className="relative w-full max-w-md mx-auto drop-shadow-2xl filter brightness-110" />
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
        
        {/* Navigation Arrows */}
        <button onClick={scrollPrev} className="absolute left-4 top-1/2 -translate-y-1/2 bg-white/20 hover:bg-white/30 text-white p-3 rounded-full backdrop-blur transition-all">
          <ChevronLeft className="h-6 w-6" />
        </button>
        <button onClick={scrollNext} className="absolute right-4 top-1/2 -translate-y-1/2 bg-white/20 hover:bg-white/30 text-white p-3 rounded-full backdrop-blur transition-all">
          <ChevronRight className="h-6 w-6" />
        </button>
        
        {/* Dots */}
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2">
          {heroSlides.map((_, index) => (
            <button
              key={index}
              onClick={() => scrollTo(index)}
              className={`w-3 h-3 rounded-full transition-all ${selectedIndex === index ? "bg-white w-8" : "bg-white/50 hover:bg-white/70"}`}
            />
          ))}
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-muted/30">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-primary mb-4">What We Offer</h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Comprehensive support for your entrepreneurial journey
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-card p-8 rounded-lg shadow-lg border border-border hover:shadow-xl transition-shadow">
              <div className="bg-primary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
                <Users className="h-8 w-8 text-primary" />
              </div>
              <h3 className="text-2xl font-bold mb-4 text-card-foreground">Mentorship</h3>
              <p className="text-muted-foreground">
                Connect with experienced entrepreneurs and industry professionals who can guide your journey.
              </p>
            </div>
            <div className="bg-card p-8 rounded-lg shadow-lg border border-border hover:shadow-xl transition-shadow">
              <div className="bg-secondary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
                <BookOpen className="h-8 w-8 text-secondary" />
              </div>
              <h3 className="text-2xl font-bold mb-4 text-card-foreground">Training Programs</h3>
              <p className="text-muted-foreground">
                Access courses, workshops, and resources to build your skills and knowledge.
              </p>
            </div>
            <div className="bg-card p-8 rounded-lg shadow-lg border border-border hover:shadow-xl transition-shadow">
              <div className="bg-accent/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
                <Briefcase className="h-8 w-8 text-accent" />
              </div>
              <h3 className="text-2xl font-bold mb-4 text-card-foreground">Job Opportunities</h3>
              <p className="text-muted-foreground">
                Discover internships, part-time roles, and career opportunities from our partners.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 bg-background">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl font-bold text-primary mb-6">About Youth Connect</h2>
              <div className="space-y-4 text-muted-foreground">
                <p className="text-lg">
                  Youth Connect is a comprehensive platform designed to bridge the gap between young aspiring entrepreneurs and the opportunities they need to succeed.
                </p>
                <p className="text-lg">
                  Whether you're a campus student or someone without formal education, we believe everyone deserves access to quality mentorship, training, and career opportunities.
                </p>
                <p className="text-lg">
                  Our platform connects three key stakeholders:
                </p>
                <ul className="space-y-3 ml-6">
                  <li className="flex items-start gap-2">
                    <Target className="h-6 w-6 text-primary mt-1 flex-shrink-0" />
                    <span><strong>Youth Entrepreneurs:</strong> Access opportunities and grow your skills</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <Target className="h-6 w-6 text-secondary mt-1 flex-shrink-0" />
                    <span><strong>Service Providers:</strong> Offer mentorship, training, and job opportunities</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <Target className="h-6 w-6 text-accent mt-1 flex-shrink-0" />
                    <span><strong>NGO Administrators:</strong> Monitor and facilitate meaningful connections</span>
                  </li>
                </ul>
              </div>
            </div>
            <div className="bg-gradient-to-br from-primary/20 to-secondary/20 p-12 rounded-2xl">
              <div className="space-y-6">
                <div className="bg-card p-6 rounded-lg shadow-md">
                  <h4 className="text-xl font-bold text-primary mb-2">Our Mission</h4>
                  <p className="text-muted-foreground">
                    To empower youth with the tools, connections, and opportunities needed to build successful entrepreneurial careers.
                  </p>
                </div>
                <div className="bg-card p-6 rounded-lg shadow-md">
                  <h4 className="text-xl font-bold text-secondary mb-2">Our Vision</h4>
                  <p className="text-muted-foreground">
                    A world where every young person has equal access to quality mentorship and opportunities.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Dashboard Access Section */}
      <section className="py-20 bg-muted/50">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold text-primary mb-4">Explore the Dashboards</h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Choose your role to see what Youth Connect can do for you
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <Link to="/dashboard/user">
              <div className="bg-card p-8 rounded-lg shadow-lg border-2 border-primary hover:shadow-xl transition-all hover:scale-105 cursor-pointer">
                <div className="bg-primary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6 mx-auto">
                  <Users className="h-8 w-8 text-primary" />
                </div>
                <h3 className="text-2xl font-bold mb-4 text-center text-card-foreground">Youth Dashboard</h3>
                <p className="text-muted-foreground text-center mb-6">
                  Access training, find mentors, browse jobs, and track your skills
                </p>
                <Button className="w-full bg-primary hover:bg-primary/90">
                  View Youth Dashboard
                </Button>
              </div>
            </Link>
            
            <Link to="/dashboard/provider">
              <div className="bg-card p-8 rounded-lg shadow-lg border-2 border-secondary hover:shadow-xl transition-all hover:scale-105 cursor-pointer">
                <div className="bg-secondary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6 mx-auto">
                  <Briefcase className="h-8 w-8 text-secondary" />
                </div>
                <h3 className="text-2xl font-bold mb-4 text-center text-card-foreground">Provider Dashboard</h3>
                <p className="text-muted-foreground text-center mb-6">
                  Post jobs, manage mentors, and connect with youth entrepreneurs
                </p>
                <Button className="w-full bg-secondary hover:bg-secondary/90">
                  View Provider Dashboard
                </Button>
              </div>
            </Link>
            
            <Link to="/dashboard/admin">
              <div className="bg-card p-8 rounded-lg shadow-lg border-2 border-accent hover:shadow-xl transition-all hover:scale-105 cursor-pointer">
                <div className="bg-accent/10 w-16 h-16 rounded-full flex items-center justify-center mb-6 mx-auto">
                  <Target className="h-8 w-8 text-accent" />
                </div>
                <h3 className="text-2xl font-bold mb-4 text-center text-card-foreground">Admin Dashboard</h3>
                <p className="text-muted-foreground text-center mb-6">
                  Manage content, view analytics, and oversee platform operations
                </p>
                <Button className="w-full bg-accent hover:bg-accent/90">
                  View Admin Dashboard
                </Button>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-primary to-secondary">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-4xl font-bold text-white mb-6">Ready to Start Your Journey?</h2>
          <p className="text-xl text-white/90 mb-8 max-w-2xl mx-auto">
            Join thousands of young entrepreneurs who are already building their future.
          </p>
          <Link to="/auth">
            <Button size="lg" className="bg-accent hover:bg-accent/90 text-white">
              Get Started Today <ArrowRight className="ml-2 h-5 w-5" />
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-dark-navy text-white py-12">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center gap-2 mb-4">
                <img src={ebpLogo} alt="EBP Logo" className="h-10 w-auto" />
                <span className="font-bold">Youth Connect</span>
              </div>
              <p className="text-white/70">
                Empowering youth through opportunity and connection.
              </p>
            </div>
            <div>
              <h4 className="font-bold mb-4">Platform</h4>
              <ul className="space-y-2 text-white/70">
                <li><Link to="/dashboard/user/mentorship" className="hover:text-white">Mentorship</Link></li>
                <li><Link to="/dashboard/user/training" className="hover:text-white">Training</Link></li>
                <li><Link to="/dashboard/user/jobs" className="hover:text-white">Jobs</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Company</h4>
              <ul className="space-y-2 text-white/70">
                <li><Link to="#about" className="hover:text-white">About</Link></li>
                <li><Link to="/contact" className="hover:text-white">Contact</Link></li>
                <li><Link to="/dashboard/provider" className="hover:text-white">Partners</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Legal</h4>
              <ul className="space-y-2 text-white/70">
                <li><Link to="/privacy-policy" className="hover:text-white">Privacy Policy</Link></li>
                <li><Link to="/terms-of-service" className="hover:text-white">Terms of Service</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-white/20 mt-8 pt-8 text-center text-white/70">
            <p>&copy; 2025 EBP PLATFORM. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Index;
