package com.youthconnect.edge_functions.service;

import com.youthconnect.edge_functions.client.OpportunityServiceClient;
import com.youthconnect.edge_functions.client.UserServiceClient;
import com.youthconnect.edge_functions.dto.OpportunityDTO;
import com.youthconnect.edge_functions.dto.UserProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class USSDService {

    private final UserServiceClient userServiceClient;
    private final OpportunityServiceClient opportunityServiceClient;

    public String handleUSSDRequest(String sessionId, String phoneNumber, String text) {
        try {
            // Clean phone number (remove + and any non-digit characters)
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "256" + cleanPhone.substring(1);
            }

            String[] textArray = text.split("\\*");
            int currentLevel = textArray.length;
            String lastInput = textArray.length > 0 ? textArray[textArray.length - 1] : "";

            // Main menu navigation
            if (text.isEmpty()) {
                return generateUSSDMenu("main");
            } else if (text.equals("1")) {
                return generateUSSDMenu("jobs");
            } else if (text.equals("2")) {
                return generateUSSDMenu("postJob");
            } else if (text.equals("3")) {
                return generateUSSDMenu("skills");
            } else if (text.equals("4")) {
                return generateUSSDMenu("mentorship");
            } else if (text.equals("5")) {
                return generateUSSDMenu("status");
            }
            // Job applications
            else if (text.startsWith("1*") && textArray.length == 2) {
                try {
                    int jobIndex = Integer.parseInt(lastInput);
                    if (jobIndex >= 1 && jobIndex <= 4) {
                        return "END Applied for job successfully! We'll contact you via SMS with next steps.";
                    } else if (lastInput.equals("0")) {
                        return generateUSSDMenu("main");
                    }
                } catch (NumberFormatException e) {
                    // Invalid selection
                }
                return "END Invalid selection. Please try again.";
            }
            // Job posting
            else if (text.startsWith("2*")) {
                return processJobPosting(cleanPhone, lastInput);
            }
            // Back to main menu
            else if (lastInput.equals("0")) {
                return generateUSSDMenu("main");
            } else {
                return "END Invalid selection. Dial *XXX# to start again.";
            }

        } catch (Exception e) {
            log.error("USSD processing error: {}", e.getMessage());
            return "END Service temporarily unavailable. Please try again later.";
        }
    }

    private String generateUSSDMenu(String step) {
        Map<String, String> menus = new HashMap<>();
        menus.put("main", "CON Welcome to Kwetu Hub\n1. View Jobs\n2. Post Job (Employers)\n3. My Skills\n4. Ask Question\n5. Check Status");
        menus.put("jobs", "CON Recent Jobs:\n1. Tailor - Arua (50k/month)\n2. Welder - Yumbe (80k/month)\n3. Cook - Nebbi (40k/month)\n4. Farm Work - Adjumani (30k/day)\n0. Back to main menu");
        menus.put("postJob", "CON Post a Job:\nEnter job details in format:\nTitle|Location|Salary|Description\nExample: Cook|Arua|40000|Restaurant cook needed");
        menus.put("skills", "CON Your Skills:\n1. Add New Skill\n2. View My Skills\n3. Update Availability\n0. Back to main menu");
        menus.put("mentorship", "CON Ask a Question:\nType your question about:\n- Business ideas\n- Skills training\n- Job search\n- Farming tips");
        menus.put("status", "CON Your Status:\n- Active Skills: 2\n- Job Applications: 1\n- Questions Asked: 3\n- Last Login: Today\n0. Back to main menu");

        return menus.getOrDefault(step, menus.get("main"));
    }

    private String processJobPosting(String phoneNumber, String jobDetails) {
        try {
            String[] parts = jobDetails.split("\\|");
            if (parts.length >= 4) {
                String title = parts[0].trim();
                String location = parts[1].trim();
                BigDecimal salary = new BigDecimal(parts[2].trim());
                String description = parts[3].trim();

                // Check if user exists
                UserProfileDTO user = userServiceClient.getUserByPhoneNumber(phoneNumber);
                if (user == null) {
                    return "END Please register at kwetuhub.app and verify your phone to post jobs.";
                }

                // Create opportunity
                OpportunityDTO opportunity = new OpportunityDTO();
                opportunity.setTitle(title);
                opportunity.setLocation(location);
                opportunity.setSalaryMin(salary);
                opportunity.setDescription(description);
                opportunity.setType("job");
                opportunity.setStatus("active");
                opportunity.setContactMethod("sms");
                opportunity.setContactInfo(phoneNumber);
                opportunity.setEmployerId(user.getId());

                OpportunityDTO createdOpportunity = opportunityServiceClient.createOpportunity(opportunity);

                if (createdOpportunity != null) {
                    return "END Job posted successfully! Candidates will contact you via SMS.";
                } else {
                    return "END Error posting job. Please try again.";
                }
            } else {
                return "END Invalid format. Use: Title|Location|Salary|Description";
            }
        } catch (Exception e) {
            log.error("Job posting error: {}", e.getMessage());
            return "END Error processing job posting. Please check the format and try again.";
        }
    }

    public String getRecentJobsMenu() {
        try {
            List<OpportunityDTO> opportunities = opportunityServiceClient.getRecentOpportunities();
            if (opportunities.isEmpty()) {
                return "CON No jobs available at the moment.\n0. Back to main menu";
            }

            StringBuilder menu = new StringBuilder("CON Recent Jobs:\n");
            for (int i = 0; i < Math.min(opportunities.size(), 5); i++) {
                OpportunityDTO job = opportunities.get(i);
                menu.append(i + 1)
                        .append(". ")
                        .append(job.getTitle())
                        .append(" - ")
                        .append(job.getLocation())
                        .append(" (")
                        .append(job.getSalaryMin())
                        .append("/month)\n");
            }
            menu.append("0. Back to main menu");

            return menu.toString();
        } catch (Exception e) {
            log.error("Error fetching jobs: {}", e.getMessage());
            return "CON Unable to load jobs. Please try again later.\n0. Back to main menu";
        }
    }
}
