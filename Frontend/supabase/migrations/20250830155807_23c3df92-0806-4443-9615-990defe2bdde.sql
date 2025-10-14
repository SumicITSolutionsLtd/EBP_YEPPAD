-- Fix security issues identified in the review

-- 1. Make opportunities.employer_id NOT NULL to ensure proper ownership
ALTER TABLE public.opportunities 
ALTER COLUMN employer_id SET NOT NULL;

-- 2. Add constraints to prevent orphaned mentorship sessions
ALTER TABLE public.mentorship_sessions 
ADD CONSTRAINT mentorship_sessions_mentee_id_not_null 
CHECK (mentee_id IS NOT NULL);

-- 3. Add phone number to user mapping table for USSD/SMS integration
CREATE TABLE public.phone_user_mapping (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  phone_number TEXT NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Enable RLS on phone mapping table
ALTER TABLE public.phone_user_mapping ENABLE ROW LEVEL SECURITY;

-- Create policies for phone mapping
CREATE POLICY "Users can view own phone mapping" 
ON public.phone_user_mapping 
FOR SELECT 
USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own phone mapping" 
ON public.phone_user_mapping 
FOR INSERT 
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own phone mapping" 
ON public.phone_user_mapping 
FOR UPDATE 
USING (auth.uid() = user_id);

-- Admins can manage all phone mappings
CREATE POLICY "Admins can manage phone mappings" 
ON public.phone_user_mapping 
FOR ALL 
USING (has_role(auth.uid(), 'admin'::app_role));

-- Create trigger for automatic timestamp updates
CREATE TRIGGER update_phone_user_mapping_updated_at
BEFORE UPDATE ON public.phone_user_mapping
FOR EACH ROW
EXECUTE FUNCTION public.update_updated_at_column();

-- Add index for performance
CREATE INDEX idx_phone_user_mapping_phone ON public.phone_user_mapping(phone_number);
CREATE INDEX idx_phone_user_mapping_user_id ON public.phone_user_mapping(user_id);