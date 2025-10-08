-- Create user roles enum
CREATE TYPE public.app_role AS ENUM ('youth', 'employer', 'ngo', 'government', 'admin');

-- Create user profiles table
CREATE TABLE public.profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL UNIQUE,
  email TEXT,
  full_name TEXT NOT NULL,
  phone TEXT,
  location TEXT,
  age INTEGER,
  gender TEXT,
  is_pwd BOOLEAN DEFAULT FALSE,
  is_rural BOOLEAN DEFAULT FALSE,
  preferred_language TEXT DEFAULT 'English',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create user roles table
CREATE TABLE public.user_roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  role app_role NOT NULL,
  UNIQUE(user_id, role)
);

-- Create skills/talents table
CREATE TABLE public.skills (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  skill_name TEXT NOT NULL,
  skill_category TEXT NOT NULL,
  description TEXT,
  experience_years INTEGER DEFAULT 0,
  portfolio_images TEXT[], -- Array of image URLs
  hourly_rate DECIMAL(10,2),
  is_available BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create jobs/opportunities table
CREATE TABLE public.opportunities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employer_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('job', 'contract', 'training', 'loan', 'grant')),
  category TEXT,
  location TEXT,
  salary_min DECIMAL(10,2),
  salary_max DECIMAL(10,2),
  requirements TEXT[],
  skills_needed TEXT[],
  deadline TIMESTAMP WITH TIME ZONE,
  is_remote BOOLEAN DEFAULT FALSE,
  contact_method TEXT DEFAULT 'platform', -- 'platform', 'sms', 'ussd'
  contact_info TEXT,
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'closed', 'draft')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create applications table
CREATE TABLE public.applications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  opportunity_id UUID REFERENCES public.opportunities(id) ON DELETE CASCADE NOT NULL,
  applicant_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  message TEXT,
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected')),
  applied_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(opportunity_id, applicant_id)
);

-- Create mentorship sessions table
CREATE TABLE public.mentorship_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  mentee_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  mentor_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  question TEXT NOT NULL,
  question_audio_url TEXT,
  answer TEXT,
  answer_audio_url TEXT,
  category TEXT,
  is_public BOOLEAN DEFAULT TRUE,
  status TEXT DEFAULT 'open' CHECK (status IN ('open', 'answered', 'closed')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  answered_at TIMESTAMP WITH TIME ZONE
);

-- Create business tools progress table
CREATE TABLE public.business_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  tool_type TEXT NOT NULL CHECK (tool_type IN ('idea_validation', 'business_plan', 'savings_guide', 'market_research')),
  progress_data JSONB DEFAULT '{}',
  completion_percentage INTEGER DEFAULT 0,
  completed_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create USSD interactions table
CREATE TABLE public.ussd_interactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone_number TEXT NOT NULL,
  session_id TEXT,
  user_input TEXT,
  response_sent TEXT,
  interaction_type TEXT CHECK (interaction_type IN ('job_post', 'job_search', 'skill_register', 'mentorship')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create impact metrics table
CREATE TABLE public.impact_metrics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  metric_type TEXT NOT NULL,
  metric_value INTEGER NOT NULL,
  filters JSONB DEFAULT '{}', -- Store demographic filters
  region TEXT,
  recorded_date DATE DEFAULT CURRENT_DATE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.skills ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.opportunities ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mentorship_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.business_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ussd_interactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.impact_metrics ENABLE ROW LEVEL SECURITY;

-- Create security definer function for role checking
CREATE OR REPLACE FUNCTION public.has_role(_user_id UUID, _role app_role)
RETURNS BOOLEAN
LANGUAGE SQL
STABLE
SECURITY DEFINER
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.user_roles
    WHERE user_id = _user_id
      AND role = _role
  )
$$;

-- RLS Policies for profiles
CREATE POLICY "Users can view all profiles" ON public.profiles FOR SELECT USING (TRUE);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own profile" ON public.profiles FOR INSERT WITH CHECK (auth.uid() = user_id);

-- RLS Policies for user_roles
CREATE POLICY "Users can view all roles" ON public.user_roles FOR SELECT USING (TRUE);
CREATE POLICY "Admins can manage all roles" ON public.user_roles FOR ALL USING (public.has_role(auth.uid(), 'admin'));

-- RLS Policies for skills
CREATE POLICY "Anyone can view skills" ON public.skills FOR SELECT USING (TRUE);
CREATE POLICY "Users can manage own skills" ON public.skills FOR ALL USING (auth.uid() = user_id);

-- RLS Policies for opportunities
CREATE POLICY "Anyone can view active opportunities" ON public.opportunities FOR SELECT USING (status = 'active');
CREATE POLICY "Employers can manage own opportunities" ON public.opportunities FOR ALL USING (auth.uid() = employer_id);
CREATE POLICY "NGOs and government can view all" ON public.opportunities FOR SELECT USING (
  public.has_role(auth.uid(), 'ngo') OR public.has_role(auth.uid(), 'government') OR public.has_role(auth.uid(), 'admin')
);

-- RLS Policies for applications
CREATE POLICY "Users can view own applications" ON public.applications FOR SELECT USING (
  auth.uid() = applicant_id OR 
  auth.uid() IN (SELECT employer_id FROM public.opportunities WHERE id = opportunity_id)
);
CREATE POLICY "Users can create applications" ON public.applications FOR INSERT WITH CHECK (auth.uid() = applicant_id);
CREATE POLICY "Employers can update application status" ON public.applications FOR UPDATE USING (
  auth.uid() IN (SELECT employer_id FROM public.opportunities WHERE id = opportunity_id)
);

-- RLS Policies for mentorship
CREATE POLICY "Anyone can view public mentorship" ON public.mentorship_sessions FOR SELECT USING (is_public = TRUE);
CREATE POLICY "Users can view own mentorship" ON public.mentorship_sessions FOR SELECT USING (
  auth.uid() = mentee_id OR auth.uid() = mentor_id
);
CREATE POLICY "Users can create mentorship questions" ON public.mentorship_sessions FOR INSERT WITH CHECK (auth.uid() = mentee_id);
CREATE POLICY "Users can answer mentorship questions" ON public.mentorship_sessions FOR UPDATE USING (
  auth.uid() = mentor_id OR auth.uid() = mentee_id
);

-- RLS Policies for business progress
CREATE POLICY "Users can manage own business progress" ON public.business_progress FOR ALL USING (auth.uid() = user_id);

-- RLS Policies for USSD interactions
CREATE POLICY "Admins can view USSD interactions" ON public.ussd_interactions FOR SELECT USING (
  public.has_role(auth.uid(), 'admin') OR public.has_role(auth.uid(), 'government')
);

-- RLS Policies for impact metrics
CREATE POLICY "NGOs and government can view metrics" ON public.impact_metrics FOR SELECT USING (
  public.has_role(auth.uid(), 'ngo') OR public.has_role(auth.uid(), 'government') OR public.has_role(auth.uid(), 'admin')
);
CREATE POLICY "NGOs and government can insert metrics" ON public.impact_metrics FOR INSERT WITH CHECK (
  public.has_role(auth.uid(), 'ngo') OR public.has_role(auth.uid(), 'government') OR public.has_role(auth.uid(), 'admin')
);

-- Create function to update timestamps
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for timestamp updates
CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();
CREATE TRIGGER update_opportunities_updated_at BEFORE UPDATE ON public.opportunities FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();
CREATE TRIGGER update_business_progress_updated_at BEFORE UPDATE ON public.business_progress FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- Create function to handle new user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
BEGIN
  INSERT INTO public.profiles (user_id, email, full_name)
  VALUES (NEW.id, NEW.email, COALESCE(NEW.raw_user_meta_data->>'full_name', 'User'));
  
  -- Default role is youth
  INSERT INTO public.user_roles (user_id, role)
  VALUES (NEW.id, 'youth');
  
  RETURN NEW;
END;
$$;

-- Create trigger for new user signup
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();