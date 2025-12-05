import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";

const PrivacyPolicy = () => {
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

      {/* Privacy Policy Content */}
      <div className="container mx-auto px-4 py-16 max-w-4xl">
        <h1 className="text-4xl md:text-5xl font-bold text-primary mb-6">Privacy Policy</h1>
        <p className="text-muted-foreground mb-8">Last updated: December 2, 2025</p>

        <div className="prose prose-lg max-w-none space-y-8">
          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">1. Introduction</h2>
            <p className="text-muted-foreground">
              Welcome to Youth Connect, operated by EBP Platform. We are committed to protecting your personal information and your right to privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our platform.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">2. Information We Collect</h2>
            <p className="text-muted-foreground mb-4">
              We collect information that you provide directly to us, including:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Personal identification information (name, email address, phone number)</li>
              <li>Professional information (skills, education, work experience)</li>
              <li>Account credentials and preferences</li>
              <li>Communications with us and other platform users</li>
              <li>Usage data and analytics information</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">3. How We Use Your Information</h2>
            <p className="text-muted-foreground mb-4">
              We use the information we collect to:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Provide, operate, and maintain our platform</li>
              <li>Connect youth with mentors, training programs, and job opportunities</li>
              <li>Improve and personalize your experience</li>
              <li>Communicate with you about updates, opportunities, and support</li>
              <li>Ensure platform security and prevent fraud</li>
              <li>Comply with legal obligations</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">4. Information Sharing</h2>
            <p className="text-muted-foreground mb-4">
              We may share your information with:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Service providers who help us operate the platform</li>
              <li>Mentors, training providers, and employers when you express interest in their opportunities</li>
              <li>Other platform users as part of your public profile</li>
              <li>Law enforcement when required by law</li>
            </ul>
            <p className="text-muted-foreground mt-4">
              We do not sell your personal information to third parties.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">5. Data Security</h2>
            <p className="text-muted-foreground">
              We implement appropriate technical and organizational security measures to protect your personal information. However, no method of transmission over the internet is 100% secure, and we cannot guarantee absolute security.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">6. Your Rights</h2>
            <p className="text-muted-foreground mb-4">
              You have the right to:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Access and receive a copy of your personal data</li>
              <li>Correct inaccurate or incomplete information</li>
              <li>Request deletion of your personal data</li>
              <li>Object to or restrict processing of your data</li>
              <li>Withdraw consent at any time</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">7. Cookies and Tracking</h2>
            <p className="text-muted-foreground">
              We use cookies and similar tracking technologies to track activity on our platform and hold certain information. You can control cookie preferences through your browser settings.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">8. Children's Privacy</h2>
            <p className="text-muted-foreground">
              Our platform is intended for users aged 16 and above. We do not knowingly collect information from children under 16. If you believe we have collected information from a child, please contact us immediately.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">9. Changes to This Policy</h2>
            <p className="text-muted-foreground">
              We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last updated" date.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">10. Contact Us</h2>
            <p className="text-muted-foreground">
              If you have questions about this Privacy Policy, please contact us at:
            </p>
            <p className="text-primary font-semibold mt-2">
              Email: <a href="mailto:privacy@ebpplatform.org" className="hover:underline">privacy@ebpplatform.org</a>
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicy;
