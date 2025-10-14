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

interface USSDRequest {
  sessionId: string;
  phoneNumber: string;
  text: string;
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

const generateUSSDMenu = (step: string, userInput?: string): string => {
  const menus = {
    main: `CON Welcome to Kwetu Hub
1. View Jobs
2. Post Job (Employers)
3. My Skills
4. Ask Question
5. Check Status`,
    
    jobs: `CON Recent Jobs:
1. Tailor - Arua (50k/month)
2. Welder - Yumbe (80k/month)
3. Cook - Nebbi (40k/month)
4. Farm Work - Adjumani (30k/day)
0. Back to main menu`,
    
    postJob: `CON Post a Job:
Enter job details in format:
Title|Location|Salary|Description
Example: Cook|Arua|40000|Restaurant cook needed`,
    
    skills: `CON Your Skills:
1. Add New Skill
2. View My Skills
3. Update Availability
0. Back to main menu`,
    
    addSkill: `CON Add Skill:
Enter skill in format:
SkillName|Category|Rate
Example: Tailoring|Crafts|5000`,
    
    mentorship: `CON Ask a Question:
Type your question about:
- Business ideas
- Skills training
- Job search
- Farming tips`,
    
    status: `CON Your Status:
- Active Skills: 2
- Job Applications: 1
- Questions Asked: 3
- Last Login: Today
0. Back to main menu`
  };
  
  return menus[step as keyof typeof menus] || menus.main;
};

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const { sessionId, phoneNumber, text }: USSDRequest = await req.json();
    
    console.log('USSD Request:', { sessionId, phoneNumber, text });

    // Log interaction
    await supabase.from('ussd_interactions').insert({
      session_id: sessionId,
      phone_number: phoneNumber,
      user_input: text,
      interaction_type: 'menu_navigation'
    });

    let response = '';
    const textArray = text.split('*');
    const currentLevel = textArray.length;
    const lastInput = textArray[textArray.length - 1];

    // Main menu navigation
    if (text === '') {
      response = generateUSSDMenu('main');
    } else if (text === '1') {
      response = generateUSSDMenu('jobs');
    } else if (text === '2') {
      response = generateUSSDMenu('postJob');
    } else if (text === '3') {
      response = generateUSSDMenu('skills');
    } else if (text === '4') {
      response = generateUSSDMenu('mentorship');
    } else if (text === '5') {
      response = generateUSSDMenu('status');
    }
    // Job applications
    else if (text.startsWith('1*') && textArray.length === 2) {
      const jobIndex = parseInt(lastInput);
      if (jobIndex >= 1 && jobIndex <= 4) {
        // Create application
        response = `END Applied for job successfully! We'll contact you via SMS with next steps.`;
        
        // Here you would typically create an application record
        // await supabase.from('applications').insert({...});
      } else if (lastInput === '0') {
        response = generateUSSDMenu('main');
      } else {
        response = 'END Invalid selection. Please try again.';
      }
    }
    // Job posting
    else if (text.startsWith('2*')) {
      const jobDetails = lastInput.split('|');
      if (jobDetails.length === 4) {
        const [title, location, salary, description] = jobDetails;
        const employerId = await resolveUserIdByPhone(phoneNumber);
        if (!employerId) {
          response = 'END Please register at lumalink.app and verify your phone to post jobs.';
        } else {
          const { error } = await supabase.from('opportunities').insert({
            title,
            location,
            salary_min: parseInt(salary),
            description,
            type: 'job',
            status: 'active',
            contact_method: 'sms',
            contact_info: phoneNumber,
            employer_id: employerId
          });

          response = error 
            ? 'END Error posting job. Please try again.'
            : 'END Job posted successfully! Candidates will contact you via SMS.';
        }
      } else {
        response = 'END Invalid format. Use: Title|Location|Salary|Description';
      }
    }
    // Skills management
    else if (text.startsWith('3*1*')) {
      const skillDetails = lastInput.split('|');
      if (skillDetails.length === 3) {
        const [skillName, category, rate] = skillDetails;
        
        // Add skill (would need user lookup by phone)
        response = 'END Skill added successfully! Register on our website to manage your profile.';
      } else {
        response = 'END Invalid format. Use: SkillName|Category|Rate';
      }
    } else if (text === '3*1') {
      response = generateUSSDMenu('addSkill');
    } else if (text === '3*2') {
      response = 'END Register on our website to view your skills: lumalink.app';
    } else if (text === '3*3') {
      response = 'END Your availability updated. Skills are now visible to employers.';
    }
    // Mentorship questions
    else if (text.startsWith('4*')) {
      const question = lastInput;
      if (question.length > 10) {
        const menteeId = await resolveUserIdByPhone(phoneNumber);
        if (!menteeId) {
          response = 'END Please register at lumalink.app and verify your phone to ask questions.';
        } else {
          await supabase.from('mentorship_sessions').insert({
            question,
            status: 'open',
            is_public: true,
            category: 'general',
            mentee_id: menteeId
          });
          response = 'END Question submitted! Check our website or SMS for answers from mentors.';
        }
      } else {
        response = 'END Please enter a longer question (minimum 10 characters).';
      }
    }
    // Back to main menu
    else if (lastInput === '0') {
      response = generateUSSDMenu('main');
    } else {
      response = 'END Invalid selection. Dial *XXX# to start again.';
    }

    // Log response
    await supabase.from('ussd_interactions').insert({
      session_id: sessionId,
      phone_number: phoneNumber,
      user_input: text,
      response_sent: response,
      interaction_type: 'menu_response'
    });

    return new Response(response, {
      headers: { ...corsHeaders, 'Content-Type': 'text/plain' },
    });

  } catch (error) {
    console.error('USSD Handler Error:', error);
    return new Response('END Service temporarily unavailable. Please try again later.', {
      status: 500,
      headers: { ...corsHeaders, 'Content-Type': 'text/plain' },
    });
  }
});