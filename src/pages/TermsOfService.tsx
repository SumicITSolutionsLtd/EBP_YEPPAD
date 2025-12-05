import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import ebpLogo from "@/assets/ebp-logo.png";

const TermsOfService = () => {
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

      {/* Terms of Service Content */}
      <div className="container mx-auto px-4 py-16 max-w-4xl">
        <h1 className="text-4xl md:text-5xl font-bold text-primary mb-6">Terms of Service</h1>
        <p className="text-muted-foreground mb-8">Last updated: December 2, 2025</p>

        <div className="prose prose-lg max-w-none space-y-8">
          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">1. Agreement to Terms</h2>
            <p className="text-muted-foreground">
              By accessing or using Youth Connect, operated by EBP Platform, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use our platform.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">2. Eligibility</h2>
            <p className="text-muted-foreground">
              You must be at least 16 years old to use this platform. By using Youth Connect, you represent and warrant that you meet this age requirement and have the legal capacity to enter into these terms.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">3. User Accounts</h2>
            <p className="text-muted-foreground mb-4">
              When you create an account, you agree to:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Provide accurate, current, and complete information</li>
              <li>Maintain the security of your account credentials</li>
              <li>Notify us immediately of any unauthorized access</li>
              <li>Be responsible for all activities under your account</li>
              <li>Not create multiple accounts or share your account with others</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">4. Platform Usage</h2>
            <p className="text-muted-foreground mb-4">
              You agree to use Youth Connect only for lawful purposes. You may not:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Violate any applicable laws or regulations</li>
              <li>Infringe on the rights of others</li>
              <li>Post false, misleading, or fraudulent content</li>
              <li>Harass, abuse, or harm other users</li>
              <li>Attempt to gain unauthorized access to our systems</li>
              <li>Use automated systems to access the platform without permission</li>
              <li>Transmit viruses, malware, or other harmful code</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">5. Content and Intellectual Property</h2>
            <p className="text-muted-foreground mb-4">
              The platform and its content are owned by EBP Platform and protected by intellectual property laws. You are granted a limited license to access and use the platform for its intended purpose.
            </p>
            <p className="text-muted-foreground">
              Content you submit to the platform remains your property, but you grant us a license to use, display, and share it as necessary to provide our services.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">6. User Roles and Responsibilities</h2>
            <div className="space-y-4">
              <div>
                <h3 className="text-xl font-semibold text-card-foreground mb-2">Youth Users</h3>
                <p className="text-muted-foreground">
                  Must accurately represent their skills, experience, and educational background when applying for opportunities.
                </p>
              </div>
              <div>
                <h3 className="text-xl font-semibold text-card-foreground mb-2">Service Providers</h3>
                <p className="text-muted-foreground">
                  Must provide accurate information about opportunities, comply with employment laws, and treat all users fairly.
                </p>
              </div>
              <div>
                <h3 className="text-xl font-semibold text-card-foreground mb-2">Administrators</h3>
                <p className="text-muted-foreground">
                  Must use administrative privileges responsibly and protect user privacy and data.
                </p>
              </div>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">7. Third-Party Services</h2>
            <p className="text-muted-foreground">
              Youth Connect may contain links to third-party websites or integrate with third-party services. We are not responsible for the content, privacy practices, or terms of these third parties.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">8. Disclaimers</h2>
            <p className="text-muted-foreground mb-4">
              Youth Connect is provided "as is" without warranties of any kind. We do not guarantee:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>The accuracy or completeness of content on the platform</li>
              <li>That the platform will be available at all times</li>
              <li>That opportunities posted will result in employment or training</li>
              <li>The quality or suitability of mentors, training programs, or jobs</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">9. Limitation of Liability</h2>
            <p className="text-muted-foreground">
              To the maximum extent permitted by law, EBP Platform shall not be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your use of or inability to use the platform.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">10. Termination</h2>
            <p className="text-muted-foreground">
              We reserve the right to suspend or terminate your account at any time for violations of these terms or for any other reason. You may also terminate your account at any time by contacting us.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">11. Changes to Terms</h2>
            <p className="text-muted-foreground">
              We may modify these Terms of Service at any time. We will notify users of significant changes. Your continued use of the platform after changes constitutes acceptance of the new terms.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">12. Governing Law</h2>
            <p className="text-muted-foreground">
              These terms are governed by the laws of Kenya. Any disputes shall be resolved in the courts of Kenya.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-card-foreground mb-4">13. Contact Information</h2>
            <p className="text-muted-foreground">
              For questions about these Terms of Service, contact us at:
            </p>
            <p className="text-primary font-semibold mt-2">
              Email: <a href="mailto:legal@ebpplatform.org" className="hover:underline">legal@ebpplatform.org</a>
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default TermsOfService;
