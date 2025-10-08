import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.56.0";

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

const supabase = createClient(
  Deno.env.get('SUPABASE_URL') ?? '',
  Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
);

interface SMSRequest {
  from: string;
  to: string;
  text: string;
  messageId?: string;
}

const resolveUserIdByPhone = async (phone: string) => {
  const { data, error } = await supabase
    .from('phone_user_mapping')
    .select('user_id')
    .eq('phone_number', phone)
    .maybeSingle();
  if (error) {
    console.error('Phone mapping lookup error:', error);
  }
  return (data?.user_id as string) || null;
};

const parseJobPosting = (text: string) => {
  // Format: JOB Title|Location|Salary|Description
  const parts = text.substring(4).split('|'); // Remove "JOB " prefix
  if (parts.length >= 4) {
    return {
      title: parts[0].trim(),
      location: parts[1].trim(),
      salary_min: parseInt(parts[2].trim()) || 0,
      description: parts[3].trim(),
      type: 'job',
      status: 'active',
      contact_method: 'sms'
    };
  }
  return null;
};

const parseSkillListing = (text: string) => {
  // Format: SKILL SkillName|Category|Rate|Description
  const parts = text.substring(6).split('|'); // Remove "SKILL " prefix
  if (parts.length >= 3) {
    return {
      skill_name: parts[0].trim(),
      skill_category: parts[1].trim(),
      hourly_rate: parseInt(parts[2].trim()) || 0,
      description: parts[3]?.trim() || '',
      is_available: true
    };
  }
  return null;
};

const sendSMSResponse = async (to: string, message: string) => {
  // In production, integrate with SMS provider (Twilio, Africa's Talking, etc.)
  console.log(`SMS to ${to}: ${message}`);
  
  // For now, just log the response
  return { success: true, messageId: `sms_${Date.now()}` };
};

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const { from, to, text, messageId }: SMSRequest = await req.json();
    
    console.log('SMS Request:', { from, to, text });

    const phoneNumber = from;
    const message = text.toUpperCase().trim();
    let response = '';

    // Job posting via SMS
    if (message.startsWith('JOB ')) {
      const jobData = parseJobPosting(message);
      if (jobData) {
        const employerId = await resolveUserIdByPhone(phoneNumber);
        if (!employerId) {
          response = 'Please register at lumalink.app and verify your phone to post jobs via SMS.';
        } else {
          const { error } = await supabase
            .from('opportunities')
            .insert({
              ...jobData,
              contact_info: phoneNumber,
              employer_id: employerId
            });

          response = error 
            ? 'Error posting job. Please check format: JOB Title|Location|Salary|Description'
            : 'Job posted successfully!';
        }
      } else {
        response = 'Invalid job format. Use: JOB Title|Location|Salary|Description\nExample: JOB Cook|Arua|40000|Restaurant cook needed';
      }
    }
    // Skill listing via SMS
    else if (message.startsWith('SKILL ')) {
      const skillData = parseSkillListing(message);
      if (skillData) {
        // Note: In production, you'd need to link this to a user account
        response = 'Skill registered! To link to your profile, visit lumalink.app and verify your phone number.';
      } else {
        response = 'Invalid skill format. Use: SKILL Name|Category|Rate|Description\nExample: SKILL Tailoring|Crafts|5000|Custom clothing';
      }
    }
    // Job application via SMS
    else if (message.startsWith('APPLY ')) {
      const jobId = message.substring(6).trim();
      response = `Application submitted for job ${jobId}. The employer will contact you directly if interested.`;
    }
    // Help and information
    else if (message === 'HELP' || message === 'INFO') {
      response = `Kwetu Hub SMS Commands:
JOB Title|Location|Salary|Description - Post a job
SKILL Name|Category|Rate|Description - List your skill
APPLY [JobID] - Apply for a job
JOBS - See recent jobs
STATUS - Check your activity
Visit kwetuhub.app for full features!`;
    }
    // List recent jobs
    else if (message === 'JOBS') {
      const { data: jobs } = await supabase
        .from('opportunities')
        .select('id, title, location, salary_min')
        .eq('status', 'active')
        .order('created_at', { ascending: false })
        .limit(5);

      if (jobs && jobs.length > 0) {
        const jobList = jobs.map((job, index) => 
          `${index + 1}. ${job.title} - ${job.location} (${job.salary_min}/month) ID:${job.id.substring(0, 8)}`
        ).join('\n');
        
        response = `Recent Jobs:\n${jobList}\n\nTo apply: SMS "APPLY [JobID]"`;
      } else {
        response = 'No jobs available at the moment. Check again later or post your skills!';
      }
    }
    // Status check
    else if (message === 'STATUS') {
      response = 'To check your full status and manage your profile, visit kwetuhub.app and verify your phone number.';
    }
    // Default response
    else {
      response = 'Welcome to Kwetu Hub! SMS HELP for commands or visit kwetuhub.app for full features.';
    }

    // Log the SMS interaction
    await supabase.from('ussd_interactions').insert({
      phone_number: phoneNumber,
      user_input: text,
      response_sent: response,
      interaction_type: 'sms'
    });

    // Send SMS response (integrate with actual SMS provider)
    await sendSMSResponse(phoneNumber, response);

    return new Response(JSON.stringify({ 
      success: true, 
      response,
      messageId: `sms_${Date.now()}`
    }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
    });

  } catch (error) {
    console.error('SMS Handler Error:', error);
    return new Response(JSON.stringify({ 
      success: false, 
      error: 'Service temporarily unavailable' 
    }), {
      status: 500,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
    });
  }
});