package io.hireme.hireme.job;

public enum RecruitmentStatus {
    INCOMING,
    AUTO_REJECTED,      // System (AI) or User said "No"
    NEW_MATCH,    // Freshly landed, waiting for your eyes.

    // HUMAN STATES
    SAVED,              // Shortlisted
    APPLIED,            // CV Sent
    INTERVIEWING,       // In progress
    OFFER,              // Success
    REJECTED_BY_USER,   // You didn't like it
    REJECTED_BY_COMPANY // They said no
}
