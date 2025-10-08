package com.youthconnect.ussd_service.service;

import com.youthconnect.ussd_service.client.GatewayClient;
import com.youthconnect.ussd_service.dto.*;
import com.youthconnect.ussd_service.model.UssdSession;
import com.youthconnect.ussd_service.repository.UssdSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * USSD Menu Service - Production Ready
 *
 * Handles all USSD interactions for YouthConnect Uganda platform
 * Supports:
 * - User registration via USSD
 * - Opportunity browsing
 * - Profile management
 * - Multi-language support (future)
 *
 * Flow: *256# → Registration/Login → Main Menu → Features
 *
 * @author YouthConnect Uganda Team
 * @version 2.0 Production
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UssdMenuService {

    private final GatewayClient gatewayClient;
    private final UssdSessionRepository sessionRepository;

    // ========================================================================
    // CONFIGURATION CONSTANTS
    // ========================================================================

    private static final int SESSION_TIMEOUT_MINUTES = 5;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_OPPORTUNITIES_DISPLAY = 5;

    // Menu State Constants
    private static final String MENU_WELCOME = "welcome";
    private static final String MENU_MAIN = "main";
    private static final String MENU_REGISTER_NAME = "register_name";
    private static final String MENU_REGISTER_GENDER = "register_gender";
    private static final String MENU_REGISTER_AGE = "register_age";
    private static final String MENU_REGISTER_DISTRICT = "register_district";
    private static final String MENU_REGISTER_BUSINESS = "register_business_stage";
    private static final String MENU_OPPORTUNITIES = "opportunities";
    private static final String MENU_PROFILE = "profile";

    // ========================================================================
    // MAIN REQUEST HANDLER
    // ========================================================================

    /**
     * Main USSD request handler with comprehensive error handling
     *
     * @param text User's input (null on first interaction)
     * @param phoneNumber User's phone number (Uganda format)
     * @param sessionId Unique session identifier from telco
     * @return USSD response (CON for continue, END for terminate)
     */
    public String handleUssdRequest(String text, String phoneNumber, String sessionId) {
        log.info("USSD Request - Phone: {}, Session: {}, Input: '{}'",
                maskPhone(phoneNumber), sessionId, text);

        try {
            // Validate inputs
            if (!StringUtils.hasText(phoneNumber) || !StringUtils.hasText(sessionId)) {
                log.error("Invalid parameters: phone={}, session={}", phoneNumber, sessionId);
                return "END Invalid request. Please try again.";
            }

            // Clean and validate phone number
            phoneNumber = cleanPhoneNumber(phoneNumber);
            if (!isValidUgandaPhone(phoneNumber)) {
                log.error("Invalid Uganda phone format: {}", phoneNumber);
                return "END Invalid phone number format.";
            }

            // Get or create session
            UssdSession session = getValidSession(sessionId, phoneNumber);

            // Check user registration status
            boolean isRegistered = checkUserRegistration(phoneNumber);

            // Route to appropriate handler
            String response = (text == null || text.trim().isEmpty())
                    ? handleFirstInteraction(session, isRegistered)
                    : processUserInput(text.trim(), session, isRegistered);

            // Save session state
            session.updateLastUpdated();
            sessionRepository.save(session);

            log.debug("Response generated for session {}", sessionId);
            return response;

        } catch (Exception e) {
            log.error("USSD processing error for {}: {}", phoneNumber, e.getMessage(), e);
            return "END Service unavailable. Please try again later.";
        }
    }

    // ========================================================================
    // REGISTRATION FLOW
    // ========================================================================

    /**
     * Handles step-by-step registration process
     */
    private String handleRegistrationFlow(String input, UssdSession session) {
        String currentMenu = session.getCurrentMenu();
        if (currentMenu == null) {
            session.setCurrentMenu(MENU_WELCOME);
            return showWelcomeUnregistered();
        }

        return switch (currentMenu) {
            case MENU_WELCOME -> handleWelcomeSelection(input, session);
            case MENU_REGISTER_NAME -> handleNameInput(input, session);
            case MENU_REGISTER_GENDER -> handleGenderSelection(input, session);
            case MENU_REGISTER_AGE -> handleAgeSelection(input, session);
            case MENU_REGISTER_DISTRICT -> handleDistrictSelection(input, session);
            case MENU_REGISTER_BUSINESS -> handleBusinessStageSelection(input, session);
            default -> showWelcomeUnregistered();
        };
    }

    /**
     * Welcome screen handler
     */
    private String handleWelcomeSelection(String input, UssdSession session) {
        if ("1".equals(input)) {
            session.setCurrentMenu(MENU_REGISTER_NAME);
            return "CON Enter your full name:\n(First and Last name)";
        } else if ("2".equals(input)) {
            return "END YouthConnect Uganda\n\n" +
                    "Empowering young entrepreneurs\n" +
                    "with opportunities, training,\n" +
                    "and mentorship.\n\n" +
                    "Visit: youthconnect.ug";
        }
        return "END Invalid option. Dial *256# again.";
    }

    /**
     * Name input validation and processing
     */
    private String handleNameInput(String input, UssdSession session) {
        if (!StringUtils.hasText(input)) {
            return "CON Name required.\nEnter your full name:";
        }

        String name = input.trim();

        // Validate name format
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            return "CON Invalid name length.\nEnter your full name (2-50 chars):";
        }

        String[] parts = name.split("\\s+");
        if (parts.length < 2) {
            return "CON Please enter BOTH first\nand last name:\n(e.g., John Doe)";
        }

        if (!name.matches("[a-zA-Z\\s'-]+")) {
            return "CON Invalid characters.\nUse letters only:";
        }

        session.setUserName(name);
        session.setCurrentMenu(MENU_REGISTER_GENDER);
        return "CON Select gender:\n1. Male\n2. Female\n3. Other";
    }

    /**
     * Gender selection
     */
    private String handleGenderSelection(String input, UssdSession session) {
        if (input.matches("[1-3]")) {
            String[] genders = {"Male", "Female", "Other"};
            session.setUserGender(genders[Integer.parseInt(input) - 1]);
            session.setCurrentMenu(MENU_REGISTER_AGE);
            return "CON Select age group:\n1. 18-24 years\n2. 25-30 years\n3. 31+ years";
        }
        return "CON Invalid selection.\n1. Male\n2. Female\n3. Other";
    }

    /**
     * Age group selection
     */
    private String handleAgeSelection(String input, UssdSession session) {
        if (input.matches("[1-3]")) {
            String[] ages = {"18-24", "25-30", "31+"};
            session.setUserAgeGroup(ages[Integer.parseInt(input) - 1]);
            session.setCurrentMenu(MENU_REGISTER_DISTRICT);
            return "CON Select district:\n1. Madi Okollo\n2. Zombo\n3. Nebbi\n4. Other";
        }
        return "CON Invalid selection.\nChoose 1-3:";
    }

    /**
     * District selection (YEPPAD project areas)
     */
    private String handleDistrictSelection(String input, UssdSession session) {
        if (input.matches("[1-4]")) {
            String[] districts = {"Madi Okollo", "Zombo", "Nebbi", "Other"};
            session.setUserDistrict(districts[Integer.parseInt(input) - 1]);
            session.setCurrentMenu(MENU_REGISTER_BUSINESS);
            return "CON Business stage:\n1. Idea Phase\n2. Early Stage\n3. Growth\n4. Established\n5. N/A";
        }
        return "CON Invalid. Choose 1-4:";
    }

    /**
     * Business stage selection and registration completion
     */
    private String handleBusinessStageSelection(String input, UssdSession session) {
        if (input.matches("[1-5]")) {
            String[] stages = {"Idea Phase", "Early Stage", "Growth", "Established", "N/A"};
            session.setUserBusinessStage(stages[Integer.parseInt(input) - 1]);

            // Complete registration
            return completeRegistration(session);
        }
        return "CON Invalid. Choose 1-5:";
    }

    /**
     * Finalizes registration and creates user account
     */
    private String completeRegistration(UssdSession session) {
        try {
            String fullName = session.getUserName();
            String[] names = fullName.split("\\s+", 2);

            UssdRegistrationRequestDTO request = new UssdRegistrationRequestDTO(
                    session.getPhoneNumber(),
                    names[0], // firstName
                    names.length > 1 ? names[1] : "", // lastName
                    session.getUserGender(),
                    session.getUserAgeGroup(),
                    session.getUserDistrict(),
                    session.getUserBusinessStage()
            );

            // Register user via API Gateway
            boolean success = gatewayClient.registerUssdUser(request);

            if (success) {
                session.clearRegistrationData();
                session.setCurrentMenu(MENU_MAIN);

                log.info("Registration successful: {}", session.getPhoneNumber());

                return "END Registration successful!\n\n" +
                        "Welcome to YouthConnect!\n\n" +
                        "Dial *256# again to explore\n" +
                        "opportunities and resources.";
            } else {
                log.error("Registration failed for: {}", session.getPhoneNumber());
                return "END Registration failed.\n\n" +
                        "Please try again later\n" +
                        "or contact support.";
            }

        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return "END Registration error.\n\n" +
                    "Please try again later.";
        }
    }

    // ========================================================================
    // MAIN MENU NAVIGATION (Registered Users)
    // ========================================================================

    /**
     * Handles menu navigation for registered users
     */
    private String handleMenuNavigation(String input, UssdSession session) {
        String currentMenu = session.getCurrentMenu();
        if (currentMenu == null) {
            session.setCurrentMenu(MENU_MAIN);
            return showMainMenu();
        }

        return switch (currentMenu) {
            case MENU_MAIN -> handleMainMenuSelection(input, session);
            case MENU_OPPORTUNITIES -> handleOpportunitiesMenu(input, session);
            case MENU_PROFILE -> handleProfileMenu(input, session);
            default -> showMainMenu();
        };
    }

    /**
     * Main menu selection handler
     */
    private String handleMainMenuSelection(String input, UssdSession session) {
        return switch (input) {
            case "1" -> {
                session.setCurrentMenu(MENU_OPPORTUNITIES);
                yield showOpportunitiesMenu();
            }
            case "2" -> "END Mentorship coming soon!\nStay tuned.";
            case "3" -> "END Learning resources\ncoming soon!";
            case "4" -> {
                session.setCurrentMenu(MENU_PROFILE);
                yield showProfileMenu();
            }
            case "5" -> showHelp();
            default -> "END Invalid option.\nDial *256# again.";
        };
    }

    /**
     * Opportunities menu with real-time fetching
     */
    private String handleOpportunitiesMenu(String input, UssdSession session) {
        if ("4".equals(input)) {
            session.setCurrentMenu(MENU_MAIN);
            return showMainMenu();
        }

        String type = switch (input) {
            case "1" -> "GRANT";
            case "2" -> "TRAINING";
            case "3" -> "JOB";
            default -> null;
        };

        if (type == null) {
            return showOpportunitiesMenu();
        }

        try {
            List<OpportunityDTO> opportunities = gatewayClient.getOpportunitiesByType(type);

            if (opportunities == null || opportunities.isEmpty()) {
                return "END No " + type.toLowerCase() + "\nopportunities available.\n\n" +
                        "Check back soon!";
            }

            StringBuilder response = new StringBuilder("CON Opportunities:\n\n");
            int limit = Math.min(opportunities.size(), MAX_OPPORTUNITIES_DISPLAY);

            for (int i = 0; i < limit; i++) {
                OpportunityDTO opp = opportunities.get(i);
                String title = opp.getTitle();
                if (title.length() > 30) {
                    title = title.substring(0, 27) + "...";
                }
                response.append(i + 1).append(". ").append(title).append("\n");
            }

            if (opportunities.size() > limit) {
                response.append("\n(").append(opportunities.size() - limit)
                        .append(" more available)\n");
            }

            response.append("\n0. Back");
            return response.toString();

        } catch (Exception e) {
            log.error("Error fetching opportunities: {}", e.getMessage());
            return "END Unable to load opportunities.\n\nTry again later.";
        }
    }

    /**
     * Profile menu handler
     */
    private String handleProfileMenu(String input, UssdSession session) {
        return switch (input) {
            case "1" -> "END View full profile at:\nyouthconnect.ug/profile";
            case "2" -> "END Update profile at:\nyouthconnect.ug/profile";
            case "3" -> {
                session.setCurrentMenu(MENU_MAIN);
                yield showMainMenu();
            }
            default -> showProfileMenu();
        };
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Gets valid session or creates new one
     */
    private UssdSession getValidSession(String sessionId, String phoneNumber) {
        Optional<UssdSession> existing = sessionRepository.findById(sessionId);

        if (existing.isPresent() && !existing.get().isExpired(SESSION_TIMEOUT_MINUTES)) {
            return existing.get();
        }

        return new UssdSession(sessionId, phoneNumber);
    }

    /**
     * Checks if user is registered
     */
    private boolean checkUserRegistration(String phoneNumber) {
        try {
            UserProfileDTO profile = gatewayClient.getUserProfile(phoneNumber);
            return profile != null;
        } catch (Exception e) {
            log.warn("Registration check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Handles first interaction
     */
    private String handleFirstInteraction(UssdSession session, boolean isRegistered) {
        if (isRegistered) {
            session.setCurrentMenu(MENU_MAIN);
            return showMainMenu();
        } else {
            session.setCurrentMenu(MENU_WELCOME);
            return showWelcomeUnregistered();
        }
    }

    /**
     * Processes user input
     */
    private String processUserInput(String text, UssdSession session, boolean isRegistered) {
        // Parse USSD input (handle * separators)
        String[] parts = text.split("\\*");
        String input = parts[parts.length - 1].trim();

        return isRegistered
                ? handleMenuNavigation(input, session)
                : handleRegistrationFlow(input, session);
    }

    /**
     * Cleans phone number to Uganda format (256XXXXXXXXX)
     */
    private String cleanPhoneNumber(String phone) {
        if (!StringUtils.hasText(phone)) return phone;

        phone = phone.replaceAll("[^0-9+]", "");

        if (phone.startsWith("+256")) return phone.substring(1);
        if (phone.startsWith("256")) return phone;
        if (phone.startsWith("0")) return "256" + phone.substring(1);
        if (phone.startsWith("7")) return "256" + phone;

        return phone;
    }

    /**
     * Validates Uganda phone number
     */
    private boolean isValidUgandaPhone(String phone) {
        return StringUtils.hasText(phone) &&
                phone.matches("256[7][0-9]{8}");
    }

    /**
     * Masks phone number for logging
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    // ========================================================================
    // MENU DISPLAY METHODS
    // ========================================================================

    private String showWelcomeUnregistered() {
        return "CON Welcome to YouthConnect!\n\n" +
                "Empowering young entrepreneurs\n\n" +
                "1. Register Now\n" +
                "2. Learn More";
    }

    private String showMainMenu() {
        return "CON YouthConnect Main Menu\n\n" +
                "1. Find Opportunities\n" +
                "2. Mentorship\n" +
                "3. Learning Resources\n" +
                "4. My Profile\n" +
                "5. Help";
    }

    private String showOpportunitiesMenu() {
        return "CON Opportunities\n\n" +
                "1. Grants & Funding\n" +
                "2. Training Programs\n" +
                "3. Job Openings\n" +
                "4. Back to Main Menu";
    }

    private String showProfileMenu() {
        return "CON My Profile\n\n" +
                "1. View Profile\n" +
                "2. Update Profile\n" +
                "3. Back to Main Menu";
    }

    private String showHelp() {
        return "END YouthConnect Help\n\n" +
                "For support:\n" +
                "Call: 0800-123-456\n" +
                "Email: support@youthconnect.ug\n" +
                "Web: youthconnect.ug";
    }
}