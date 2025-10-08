package com.youthconnect.user_service.entity;

/**
 * Enum to define the different roles a user can have in the system.
 * This enum directly maps to the 'role' ENUM in the 'users' table in the database schema.
 */
public enum Role {
    YOUTH,              // Represents a young individual user seeking opportunities/mentorship
    NGO,                // Represents a Non-Governmental Organization providing opportunities
    FUNDER,             // Represents an entity or individual providing funding
    SERVICE_PROVIDER,   // Represents an entity or individual offering specific services (e.g., legal, marketing)
    MENTOR,             // Represents an experienced individual offering guidance and mentorship
    ADMIN               // Represents an administrator user with elevated system-wide privileges
}