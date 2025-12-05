import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { Mail, Phone, MapPin, ArrowLeft } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";

const Contact = () => {
  return (
    <div className="min-h-screen bg-background">
      {/* Navigation */}
      <nav className="border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3">
            <img src={ebpLogo} alt="EBP Logo" className="h-12 w-auto" />
            <span className="text-xl font-bold text-primary">Youth Connect</span>
          </Link>
          <Link to="/">
            <Button variant="ghost">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Home
            </Button>
          </Link>
        </div>
      </nav>

      {/* Contact Content */}
      <div className="container mx-auto px-4 py-16 max-w-4xl">
        <div className="text-center mb-12">
          <h1 className="text-4xl md:text-5xl font-bold text-primary mb-4">Contact Us</h1>
          <p className="text-xl text-muted-foreground">
            Get in touch with our team. We're here to help you succeed.
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 mb-12">
          <div className="bg-card p-8 rounded-lg shadow-lg border border-border">
            <div className="bg-primary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
              <Mail className="h-8 w-8 text-primary" />
            </div>
            <h3 className="text-2xl font-bold mb-4 text-card-foreground">Email Us</h3>
            <p className="text-muted-foreground mb-4">
              Send us an email and we'll get back to you within 24 hours.
            </p>
            <a href="mailto:info@ebpplatform.org" className="text-primary hover:underline font-semibold">
              info@ebpplatform.org
            </a>
          </div>

          <div className="bg-card p-8 rounded-lg shadow-lg border border-border">
            <div className="bg-secondary/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
              <Phone className="h-8 w-8 text-secondary" />
            </div>
            <h3 className="text-2xl font-bold mb-4 text-card-foreground">Call Us</h3>
            <p className="text-muted-foreground mb-4">
              Available Monday to Friday, 9am to 5pm EAT.
            </p>
            <a href="tel:+254700000000" className="text-primary hover:underline font-semibold">
              +254 700 000 000
            </a>
          </div>

          <div className="bg-card p-8 rounded-lg shadow-lg border border-border md:col-span-2">
            <div className="bg-accent/10 w-16 h-16 rounded-full flex items-center justify-center mb-6">
              <MapPin className="h-8 w-8 text-accent" />
            </div>
            <h3 className="text-2xl font-bold mb-4 text-card-foreground">Visit Us</h3>
            <p className="text-muted-foreground mb-4">
              Come visit our office during business hours.
            </p>
            <p className="text-card-foreground font-semibold">
              EBP Platform Headquarters<br />
              Nairobi, Kenya
            </p>
          </div>
        </div>

        <div className="bg-gradient-to-br from-primary/10 to-secondary/10 p-8 rounded-lg text-center">
          <h3 className="text-2xl font-bold mb-4 text-card-foreground">Need Quick Help?</h3>
          <p className="text-muted-foreground mb-6">
            Check out our FAQ section or reach out to us directly.
          </p>
          <Link to="/auth">
            <Button size="lg" className="bg-primary hover:bg-primary/90">
              Get Started
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Contact;
