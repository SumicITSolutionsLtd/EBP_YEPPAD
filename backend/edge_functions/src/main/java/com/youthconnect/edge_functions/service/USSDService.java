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
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "256" + cleanPhone.substring(1);
            }

            String[] textArray = text.split("\\*");
            String lastInput = textArray.length > 0 ? textArray[textArray.length - 1] : "";

            // Main menu
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
                        return "END Applied successfully! We'll contact you via SMS.";
                    } else if (lastInput.equals("0")) {
                        return generateUSSDMenu("main");
                    }
                } catch (NumberFormatException e) {
                    // Invalid
                }
                return "END Invalid selection. Please try again.";
            }
            // Job posting
            else if (text.startsWith("2*")) {
                return processJobPosting(cleanPhone, lastInput);
            }
            // Back to main
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
        menus.put("main", "CON Welcome to Kwetu Hub\n1. View Jobs\n2. Post Job\n3. My Skills\n4. Ask Question\n5. Check Status");
        menus.put("jobs", "CON Recent Jobs:\n1. Tailor - Arua (50k/month)\n2. Welder - Yumbe (80k/month)\n3. Cook - Nebbi (40k/month)\n4. Farm Work - Adjumani (30k/day)\n0. Back");
        menus.put("postJob", "CON Post a Job:\nEnter: Title|Location|Salary|Description\nExample: Cook|Arua|40000|Restaurant cook needed");
        menus.put("skills", "CON Your Skills:\n1. Add New Skill\n2. View My Skills\n3. Update Availability\n0. Back");
        menus.put("mentorship", "CON Ask a Question:\nType your question about:\n- Business ideas\n- Skills training\n- Job search\n- Farming tips");
        menus.put("status", "CON Your Status:\n- Active Skills: 2\n- Job Applications: 1\n- Questions Asked: 3\n- Last Login: Today\n0. Back");

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

                UserProfileDTO user = userServiceClient.getUserByPhoneNumber(phoneNumber);
                if (user == null) {
                    return "END Please register at kwetuhub.app first.";
                }

                OpportunityDTO opportunity = new OpportunityDTO();
                opportunity.setTitle(title);
                opportunity.setLocation(location);
                opportunity.setSalaryMin(salary);
                opportunity.setDescription(description);
                opportunity.setOpportunityType("JOB");  // ✅ FIXED
                opportunity.setStatus("active");
                opportunity.setContactMethod("sms");
                opportunity.setContactInfo(phoneNumber);
                opportunity.setEmployerId(user.getUserId());  // ✅ FIXED

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
            return "END Error processing job. Please check format and try again.";
        }
    }
}