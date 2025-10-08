-- Tighten RLS for profiles: restrict SELECT to owner and privileged roles

-- Ensure RLS is enabled (no-op if already enabled)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Drop overly permissive policy
DROP POLICY IF EXISTS "Users can view all profiles" ON public.profiles;

-- Policy: Users can view their own profile
CREATE POLICY "Users can view own profile"
ON public.profiles
FOR SELECT
USING (auth.uid() = user_id);

-- Policy: NGO/Government/Admin can view all profiles
CREATE POLICY "Privileged roles can view all profiles"
ON public.profiles
FOR SELECT
USING (
  has_role(auth.uid(), 'ngo'::app_role)
  OR has_role(auth.uid(), 'government'::app_role)
  OR has_role(auth.uid(), 'admin'::app_role)
);
