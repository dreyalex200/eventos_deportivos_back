-- ============================================================
-- Sports Events Management Platform
-- MySQL 8.4 LTS+
-- Initial relational schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS sports_events
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE sports_events;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. SECURITY / USERS
-- ============================================================

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- ============================================================
-- 2. GENERIC / GEOGRAPHIC CATALOGS
-- ============================================================

DROP TABLE IF EXISTS emergency_contacts;
DROP TABLE IF EXISTS person_documents;
DROP TABLE IF EXISTS person_addresses;
DROP TABLE IF EXISTS persons;
DROP TABLE IF EXISTS document_types;
DROP TABLE IF EXISTS genders;
DROP TABLE IF EXISTS cities;

-- ============================================================
-- 3. SPORTS CATALOGS
-- ============================================================

DROP TABLE IF EXISTS event_categories;
DROP TABLE IF EXISTS age_categories;
DROP TABLE IF EXISTS sport_modalities;
DROP TABLE IF EXISTS sports;
DROP TABLE IF EXISTS competition_types;

-- ============================================================
-- 4. ORGANIZATIONS
-- ============================================================

DROP TABLE IF EXISTS sponsors;
DROP TABLE IF EXISTS organizations;
DROP TABLE IF EXISTS organization_types;

-- ============================================================
-- 5. VENUES
-- ============================================================

DROP TABLE IF EXISTS venue_availability;
DROP TABLE IF EXISTS venue_facilities;
DROP TABLE IF EXISTS sports_venues;

-- ============================================================
-- 6. EVENTS / RULES / PHASES
-- ============================================================

DROP TABLE IF EXISTS event_rule_values;
DROP TABLE IF EXISTS event_rules;
DROP TABLE IF EXISTS rules;
DROP TABLE IF EXISTS event_phases;
DROP TABLE IF EXISTS events;

-- ============================================================
-- 7. TEAMS / REGISTRATIONS / PARTICIPANTS
-- ============================================================

DROP TABLE IF EXISTS registration_documents;
DROP TABLE IF EXISTS registration_members;
DROP TABLE IF EXISTS team_staff;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS teams;

-- ============================================================
-- 8. SCHEDULING / MATCHES
-- ============================================================

DROP TABLE IF EXISTS match_officials;
DROP TABLE IF EXISTS match_events;
DROP TABLE IF EXISTS match_results;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS event_schedules;

-- ============================================================
-- SECURITY
-- ============================================================

CREATE TABLE roles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code),
    KEY idx_roles_status (status)
) ENGINE=InnoDB;

CREATE TABLE permissions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_code (code),
    KEY idx_permissions_status (status)
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
    role_id BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_status (status)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- GEOGRAPHY / PERSONS
-- ============================================================

CREATE TABLE cities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    state_province VARCHAR(150) NULL,
    country VARCHAR(150) NOT NULL DEFAULT 'Colombia',
    country_code CHAR(2) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_cities_name (name),
    KEY idx_cities_status (status)
) ENGINE=InnoDB;

CREATE TABLE genders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_genders_code (code)
) ENGINE=InnoDB;

CREATE TABLE document_types (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_document_types_code (code)
) ENGINE=InnoDB;

CREATE TABLE persons (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    document_type_id BIGINT UNSIGNED NULL,
    document_number VARCHAR(50) NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100) NULL,
    last_name VARCHAR(100) NOT NULL,
    second_last_name VARCHAR(100) NULL,
    birth_date DATE NULL,
    gender_id BIGINT UNSIGNED NULL,
    nationality VARCHAR(100) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    mobile_phone VARCHAR(50) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_person_document (document_type_id, document_number),
    KEY idx_persons_name (last_name, first_name),
    KEY idx_persons_birth_date (birth_date),
    KEY idx_persons_gender (gender_id),
    KEY idx_persons_status (status),
    CONSTRAINT fk_persons_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_types(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_persons_gender
        FOREIGN KEY (gender_id) REFERENCES genders(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE person_addresses (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    person_id BIGINT UNSIGNED NOT NULL,
    city_id BIGINT UNSIGNED NULL,
    address VARCHAR(500) NOT NULL,
    neighborhood VARCHAR(150) NULL,
    postal_code VARCHAR(20) NULL,
    address_type VARCHAR(30) NOT NULL DEFAULT 'RESIDENTIAL',
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_person_addresses_person (person_id),
    KEY idx_person_addresses_city (city_id),
    CONSTRAINT fk_person_addresses_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_person_addresses_city
        FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE person_documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    person_id BIGINT UNSIGNED NOT NULL,
    document_type_id BIGINT UNSIGNED NOT NULL,
    document_number VARCHAR(100) NULL,
    file_reference VARCHAR(1000) NULL,
    issue_date DATE NULL,
    expiration_date DATE NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_person_documents_person (person_id),
    KEY idx_person_documents_type (document_type_id),
    CONSTRAINT fk_person_documents_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_person_documents_type
        FOREIGN KEY (document_type_id) REFERENCES document_types(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE emergency_contacts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    person_id BIGINT UNSIGNED NOT NULL,
    contact_person_id BIGINT UNSIGNED NULL,
    relationship_type VARCHAR(100) NOT NULL,
    contact_name VARCHAR(200) NULL,
    phone VARCHAR(50) NOT NULL,
    mobile_phone VARCHAR(50) NULL,
    email VARCHAR(255) NULL,
    priority SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_emergency_contacts_person (person_id),
    KEY idx_emergency_contacts_contact_person (contact_person_id),
    CONSTRAINT fk_emergency_contacts_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_emergency_contacts_contact_person
        FOREIGN KEY (contact_person_id) REFERENCES persons(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SPORTS / COMPETITION CATALOGS
-- ============================================================

CREATE TABLE sports (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sports_code (code),
    KEY idx_sports_status (status)
) ENGINE=InnoDB;

CREATE TABLE sport_modalities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sport_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    players_min SMALLINT UNSIGNED NULL,
    players_max SMALLINT UNSIGNED NULL,
    substitutes_min SMALLINT UNSIGNED NULL,
    substitutes_max SMALLINT UNSIGNED NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sport_modalities_code (sport_id, code),
    KEY idx_sport_modalities_sport (sport_id),
    CONSTRAINT fk_sport_modalities_sport
        FOREIGN KEY (sport_id) REFERENCES sports(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE competition_types (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_competition_types_code (code)
) ENGINE=InnoDB;

CREATE TABLE age_categories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    min_age SMALLINT UNSIGNED NULL,
    max_age SMALLINT UNSIGNED NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_age_categories_code (code),
    KEY idx_age_categories_range (min_age, max_age)
) ENGINE=InnoDB;

-- ============================================================
-- ORGANIZATIONS / SPONSORS
-- ============================================================

CREATE TABLE organization_types (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_organization_types_code (code)
) ENGINE=InnoDB;

CREATE TABLE organizations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    organization_type_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(255) NULL,
    identification_number VARCHAR(100) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    address VARCHAR(500) NULL,
    city_id BIGINT UNSIGNED NULL,
    contact_person_id BIGINT UNSIGNED NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_organizations_identification (identification_number),
    KEY idx_organizations_type (organization_type_id),
    KEY idx_organizations_city (city_id),
    KEY idx_organizations_name (name),
    CONSTRAINT fk_organizations_type
        FOREIGN KEY (organization_type_id) REFERENCES organization_types(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_organizations_city
        FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_organizations_contact_person
        FOREIGN KEY (contact_person_id) REFERENCES persons(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE sponsors (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    organization_id BIGINT UNSIGNED NULL,
    name VARCHAR(200) NOT NULL,
    identification_number VARCHAR(100) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_sponsors_organization (organization_id),
    KEY idx_sponsors_name (name),
    CONSTRAINT fk_sponsors_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- EVENTS
-- ============================================================

CREATE TABLE events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sport_id BIGINT UNSIGNED NOT NULL,
    modality_id BIGINT UNSIGNED NULL,
    competition_type_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NULL,
    description TEXT NULL,
    city_id BIGINT UNSIGNED NULL,
    organizer_organization_id BIGINT UNSIGNED NULL,
    sponsor_id BIGINT UNSIGNED NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    registration_start DATETIME NULL,
    registration_end DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    configuration JSON NULL,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_events_code (code),
    KEY idx_events_sport (sport_id),
    KEY idx_events_modality (modality_id),
    KEY idx_events_competition_type (competition_type_id),
    KEY idx_events_city (city_id),
    KEY idx_events_status (status),
    KEY idx_events_dates (start_date, end_date),
    CONSTRAINT fk_events_sport
        FOREIGN KEY (sport_id) REFERENCES sports(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_events_modality
        FOREIGN KEY (modality_id) REFERENCES sport_modalities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_events_competition_type
        FOREIGN KEY (competition_type_id) REFERENCES competition_types(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_events_city
        FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_events_organizer
        FOREIGN KEY (organizer_organization_id) REFERENCES organizations(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_events_sponsor
        FOREIGN KEY (sponsor_id) REFERENCES sponsors(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_events_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE event_categories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    age_category_id BIGINT UNSIGNED NULL,
    gender_id BIGINT UNSIGNED NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NULL,
    min_players SMALLINT UNSIGNED NULL,
    max_players SMALLINT UNSIGNED NULL,
    min_substitutes SMALLINT UNSIGNED NULL,
    max_substitutes SMALLINT UNSIGNED NULL,
    description VARCHAR(500) NULL,
    configuration JSON NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_event_categories_code (event_id, code),
    KEY idx_event_categories_event (event_id),
    KEY idx_event_categories_age (age_category_id),
    KEY idx_event_categories_gender (gender_id),
    CONSTRAINT fk_event_categories_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_categories_age
        FOREIGN KEY (age_category_id) REFERENCES age_categories(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_event_categories_gender
        FOREIGN KEY (gender_id) REFERENCES genders(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE event_phases (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NULL,
    phase_type VARCHAR(50) NOT NULL,
    sequence SMALLINT UNSIGNED NOT NULL,
    start_date DATETIME NULL,
    end_date DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    configuration JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_event_phases_code (event_id, code),
    UNIQUE KEY uq_event_phases_sequence (event_id, sequence),
    KEY idx_event_phases_event (event_id),
    KEY idx_event_phases_dates (start_date, end_date),
    CONSTRAINT fk_event_phases_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- RULE ENGINE
-- ============================================================

CREATE TABLE rules (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NULL,
    rule_type VARCHAR(50) NOT NULL,
    value_type VARCHAR(30) NOT NULL DEFAULT 'JSON',
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_rules_code (code),
    KEY idx_rules_type (rule_type),
    KEY idx_rules_status (status)
) ENGINE=InnoDB;

CREATE TABLE event_rules (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    rule_id BIGINT UNSIGNED NOT NULL,
    applies_to_phase_id BIGINT UNSIGNED NULL,
    applies_to_category_id BIGINT UNSIGNED NULL,
    is_required TINYINT(1) NOT NULL DEFAULT 1,
    priority SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_event_rules_scope (
        event_id, rule_id, applies_to_phase_id, applies_to_category_id
    ),
    KEY idx_event_rules_event (event_id),
    KEY idx_event_rules_rule (rule_id),
    KEY idx_event_rules_phase (applies_to_phase_id),
    KEY idx_event_rules_category (applies_to_category_id),
    CONSTRAINT fk_event_rules_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_rules_rule
        FOREIGN KEY (rule_id) REFERENCES rules(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_event_rules_phase
        FOREIGN KEY (applies_to_phase_id) REFERENCES event_phases(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_rules_category
        FOREIGN KEY (applies_to_category_id) REFERENCES event_categories(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE event_rule_values (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_rule_id BIGINT UNSIGNED NOT NULL,
    value_string VARCHAR(1000) NULL,
    value_number DECIMAL(18,4) NULL,
    value_boolean TINYINT(1) NULL,
    value_date DATE NULL,
    value_datetime DATETIME NULL,
    value_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_event_rule_values_rule (event_rule_id),
    CONSTRAINT fk_event_rule_values_rule
        FOREIGN KEY (event_rule_id) REFERENCES event_rules(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SPORTS VENUES
-- ============================================================

CREATE TABLE sports_venues (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    city_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NULL,
    address VARCHAR(500) NULL,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    capacity INT UNSIGNED NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_sports_venues_city (city_id),
    KEY idx_sports_venues_name (name),
    KEY idx_sports_venues_status (status),
    CONSTRAINT fk_sports_venues_city
        FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE venue_facilities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    venue_id BIGINT UNSIGNED NOT NULL,
    sport_id BIGINT UNSIGNED NULL,
    modality_id BIGINT UNSIGNED NULL,
    name VARCHAR(200) NOT NULL,
    facility_type VARCHAR(100) NULL,
    capacity INT UNSIGNED NULL,
    description VARCHAR(500) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_venue_facilities_venue (venue_id),
    KEY idx_venue_facilities_sport (sport_id),
    KEY idx_venue_facilities_modality (modality_id),
    CONSTRAINT fk_venue_facilities_venue
        FOREIGN KEY (venue_id) REFERENCES sports_venues(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_venue_facilities_sport
        FOREIGN KEY (sport_id) REFERENCES sports(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_venue_facilities_modality
        FOREIGN KEY (modality_id) REFERENCES sport_modalities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE venue_availability (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    venue_facility_id BIGINT UNSIGNED NOT NULL,
    day_of_week TINYINT UNSIGNED NULL,
    available_start TIME NULL,
    available_end TIME NULL,
    valid_from DATE NULL,
    valid_until DATE NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_venue_availability_facility (venue_facility_id),
    KEY idx_venue_availability_dates (valid_from, valid_until),
    CONSTRAINT fk_venue_availability_facility
        FOREIGN KEY (venue_facility_id) REFERENCES venue_facilities(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- TEAMS
-- ============================================================

CREATE TABLE teams (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    organization_id BIGINT UNSIGNED NULL,
    sponsor_id BIGINT UNSIGNED NULL,
    city_id BIGINT UNSIGNED NULL,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50) NULL,
    description VARCHAR(500) NULL,
    logo_file_reference VARCHAR(1000) NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_teams_organization (organization_id),
    KEY idx_teams_sponsor (sponsor_id),
    KEY idx_teams_city (city_id),
    KEY idx_teams_name (name),
    CONSTRAINT fk_teams_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_teams_sponsor
        FOREIGN KEY (sponsor_id) REFERENCES sponsors(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_teams_city
        FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE team_members (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    team_id BIGINT UNSIGNED NOT NULL,
    person_id BIGINT UNSIGNED NOT NULL,
    member_type VARCHAR(50) NOT NULL DEFAULT 'PLAYER',
    jersey_number SMALLINT UNSIGNED NULL,
    position VARCHAR(100) NULL,
    is_captain TINYINT(1) NOT NULL DEFAULT 0,
    start_date DATE NULL,
    end_date DATE NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_team_members_active_relation (team_id, person_id, member_type),
    KEY idx_team_members_team (team_id),
    KEY idx_team_members_person (person_id),
    CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id) REFERENCES teams(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_team_members_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE team_staff (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    team_id BIGINT UNSIGNED NOT NULL,
    person_id BIGINT UNSIGNED NOT NULL,
    role VARCHAR(50) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_team_staff_relation (team_id, person_id, role),
    KEY idx_team_staff_team (team_id),
    KEY idx_team_staff_person (person_id),
    CONSTRAINT fk_team_staff_team
        FOREIGN KEY (team_id) REFERENCES teams(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_team_staff_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- REGISTRATIONS
-- ============================================================

CREATE TABLE registrations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    event_category_id BIGINT UNSIGNED NOT NULL,
    team_id BIGINT UNSIGNED NOT NULL,
    organization_id BIGINT UNSIGNED NULL,
    submitted_by_user_id BIGINT UNSIGNED NULL,
    submitted_at DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by_user_id BIGINT UNSIGNED NULL,
    approved_at DATETIME NULL,
    rejection_reason VARCHAR(1000) NULL,
    registration_data JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_registration_event_category_team (
        event_id, event_category_id, team_id
    ),
    KEY idx_registrations_event (event_id),
    KEY idx_registrations_category (event_category_id),
    KEY idx_registrations_team (team_id),
    KEY idx_registrations_status (status),
    CONSTRAINT fk_registrations_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_registrations_category
        FOREIGN KEY (event_category_id) REFERENCES event_categories(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_registrations_team
        FOREIGN KEY (team_id) REFERENCES teams(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_registrations_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_registrations_submitted_by
        FOREIGN KEY (submitted_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_registrations_approved_by
        FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE registration_members (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    registration_id BIGINT UNSIGNED NOT NULL,
    person_id BIGINT UNSIGNED NOT NULL,
    participant_type VARCHAR(50) NOT NULL DEFAULT 'PLAYER',
    jersey_number SMALLINT UNSIGNED NULL,
    position VARCHAR(100) NULL,
    is_captain TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    validation_data JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_registration_member (registration_id, person_id, participant_type),
    KEY idx_registration_members_registration (registration_id),
    KEY idx_registration_members_person (person_id),
    CONSTRAINT fk_registration_members_registration
        FOREIGN KEY (registration_id) REFERENCES registrations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_registration_members_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE registration_documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    registration_id BIGINT UNSIGNED NOT NULL,
    person_id BIGINT UNSIGNED NULL,
    document_type_id BIGINT UNSIGNED NOT NULL,
    file_reference VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    validation_notes VARCHAR(1000) NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_at DATETIME NULL,
    validated_by_user_id BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_registration_documents_registration (registration_id),
    KEY idx_registration_documents_person (person_id),
    KEY idx_registration_documents_type (document_type_id),
    CONSTRAINT fk_registration_documents_registration
        FOREIGN KEY (registration_id) REFERENCES registrations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_registration_documents_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_registration_documents_type
        FOREIGN KEY (document_type_id) REFERENCES document_types(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_registration_documents_validator
        FOREIGN KEY (validated_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SCHEDULING
-- ============================================================

CREATE TABLE event_schedules (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    event_phase_id BIGINT UNSIGNED NULL,
    venue_facility_id BIGINT UNSIGNED NOT NULL,
    scheduled_start DATETIME NOT NULL,
    scheduled_end DATETIME NOT NULL,
    schedule_type VARCHAR(50) NOT NULL DEFAULT 'MATCH',
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000) NULL,
    created_by BIGINT UNSIGNED NULL,
    updated_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_event_schedules_event (event_id),
    KEY idx_event_schedules_phase (event_phase_id),
    KEY idx_event_schedules_facility (venue_facility_id),
    KEY idx_event_schedules_datetime (scheduled_start, scheduled_end),
    CONSTRAINT fk_event_schedules_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_schedules_phase
        FOREIGN KEY (event_phase_id) REFERENCES event_phases(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_event_schedules_facility
        FOREIGN KEY (venue_facility_id) REFERENCES venue_facilities(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_event_schedules_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_event_schedules_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- MATCHES / RESULTS
-- ============================================================

CREATE TABLE matches (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    event_id BIGINT UNSIGNED NOT NULL,
    event_category_id BIGINT UNSIGNED NOT NULL,
    event_phase_id BIGINT UNSIGNED NULL,
    schedule_id BIGINT UNSIGNED NULL,
    home_team_id BIGINT UNSIGNED NOT NULL,
    away_team_id BIGINT UNSIGNED NOT NULL,
    match_number INT UNSIGNED NULL,
    round_number SMALLINT UNSIGNED NULL,
    scheduled_start DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    PRIMARY KEY (id),

    KEY idx_matches_event (event_id),
    KEY idx_matches_category (event_category_id),
    KEY idx_matches_phase (event_phase_id),
    KEY idx_matches_schedule (schedule_id),
    KEY idx_matches_home_team (home_team_id),
    KEY idx_matches_away_team (away_team_id),
    KEY idx_matches_start (scheduled_start),

    CONSTRAINT fk_matches_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_matches_category
        FOREIGN KEY (event_category_id) REFERENCES event_categories(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_matches_phase
        FOREIGN KEY (event_phase_id) REFERENCES event_phases(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_matches_schedule
        FOREIGN KEY (schedule_id) REFERENCES event_schedules(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_matches_home_team
        FOREIGN KEY (home_team_id) REFERENCES teams(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_matches_away_team
        FOREIGN KEY (away_team_id) REFERENCES teams(id)
        ON DELETE RESTRICT ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE match_results (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    match_id BIGINT UNSIGNED NOT NULL,
    home_score DECIMAL(8,2) NOT NULL DEFAULT 0,
    away_score DECIMAL(8,2) NOT NULL DEFAULT 0,
    winner_team_id BIGINT UNSIGNED NULL,
    result_type VARCHAR(50) NULL,
    is_final TINYINT(1) NOT NULL DEFAULT 0,
    recorded_by_user_id BIGINT UNSIGNED NULL,
    recorded_at DATETIME NULL,
    notes VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_match_results_match (match_id),
    KEY idx_match_results_winner (winner_team_id),
    CONSTRAINT fk_match_results_match
        FOREIGN KEY (match_id) REFERENCES matches(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_match_results_winner
        FOREIGN KEY (winner_team_id) REFERENCES teams(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_match_results_recorded_by
        FOREIGN KEY (recorded_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_match_results_scores
        CHECK (home_score >= 0 AND away_score >= 0)
) ENGINE=InnoDB;

CREATE TABLE match_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    match_id BIGINT UNSIGNED NOT NULL,
    team_id BIGINT UNSIGNED NULL,
    person_id BIGINT UNSIGNED NULL,
    event_type VARCHAR(50) NOT NULL,
    event_minute SMALLINT UNSIGNED NULL,
    period VARCHAR(30) NULL,
    quantity DECIMAL(8,2) NULL,
    metadata JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_match_events_match (match_id),
    KEY idx_match_events_team (team_id),
    KEY idx_match_events_person (person_id),
    KEY idx_match_events_type (event_type),
    CONSTRAINT fk_match_events_match
        FOREIGN KEY (match_id) REFERENCES matches(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_match_events_team
        FOREIGN KEY (team_id) REFERENCES teams(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_match_events_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE match_officials (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    match_id BIGINT UNSIGNED NOT NULL,
    person_id BIGINT UNSIGNED NOT NULL,
    role VARCHAR(50) NOT NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_match_official (match_id, person_id, role),
    KEY idx_match_officials_person (person_id),
    CONSTRAINT fk_match_officials_match
        FOREIGN KEY (match_id) REFERENCES matches(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_match_officials_person
        FOREIGN KEY (person_id) REFERENCES persons(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- AUDIT
-- ============================================================

CREATE TABLE audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NULL,
    action VARCHAR(50) NOT NULL,
    entity VARCHAR(150) NOT NULL,
    entity_id BIGINT UNSIGNED NULL,
    old_data JSON NULL,
    new_data JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_logs_user (user_id),
    KEY idx_audit_logs_entity (entity, entity_id),
    KEY idx_audit_logs_action (action),
    KEY idx_audit_logs_created_at (created_at),
    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- INITIAL CATALOG DATA
-- ============================================================

INSERT INTO roles (code, name, description)
VALUES
    ('ADMIN', 'Administrator', 'Full platform administration'),
    ('OPERATOR', 'Operator', 'Operational management of sports events'),
    ('AUDITOR', 'Auditor', 'Read-only auditing and traceability access');

INSERT INTO genders (code, name, description)
VALUES
    ('MALE', 'Male', 'Male competition category'),
    ('FEMALE', 'Female', 'Female competition category'),
    ('MIXED', 'Mixed', 'Mixed competition category');

INSERT INTO document_types (code, name, description)
VALUES
    ('CC', 'Cédula de Ciudadanía', 'Colombian citizenship ID'),
    ('TI', 'Tarjeta de Identidad', 'Colombian identity card'),
    ('CE', 'Cédula de Extranjería', 'Foreign resident ID'),
    ('PASSPORT', 'Passport', 'Passport document');

INSERT INTO organization_types (code, name, description)
VALUES
    ('EDUCATIONAL_INSTITUTION', 'Educational Institution', 'School, college or educational institution'),
    ('SPORTS_CLUB', 'Sports Club', 'Sports club or team organization'),
    ('COMPANY', 'Company', 'Private company or corporate organization'),
    ('GOVERNMENT_ENTITY', 'Government Entity', 'Government organization'),
    ('SPORTS_FEDERATION', 'Sports Federation', 'Sports federation or association'),
    ('OTHER', 'Other', 'Other organization type');

INSERT INTO sports (code, name, description)
VALUES
    ('FOOTBALL', 'Football', 'Association football'),
    ('BASKETBALL', 'Basketball', 'Basketball'),
    ('VOLLEYBALL', 'Volleyball', 'Volleyball'),
    ('BASEBALL', 'Baseball', 'Baseball'),
    ('FUTSAL', 'Futsal', 'Indoor football'),
    ('OTHER', 'Other', 'Other configurable sports');

INSERT INTO competition_types (code, name, description)
VALUES
    ('FREE_TOURNAMENT', 'Free Tournament', 'Open or freely organized tournament'),
    ('INTERSCHOOLS', 'Inter-School Competition', 'Competition involving educational institutions'),
    ('CORPORATE_GAMES', 'Corporate Games', 'Competition between companies or organizations'),
    ('INSTITUTIONAL', 'Institutional Competition', 'Competition organized by an institution'),
    ('MUNICIPAL', 'Municipal Competition', 'Municipal-level competition'),
    ('REGIONAL', 'Regional Competition', 'Regional competition'),
    ('FEDERATED', 'Federated Competition', 'Competition governed by a federation'),
    ('CUSTOM', 'Custom Competition', 'Custom competition type');

INSERT INTO rules (code, name, description, rule_type, value_type)
VALUES
    ('REGISTRATION_START_DATE', 'Registration Start Date',
     'Defines when team or participant registration becomes available', 'REGISTRATION', 'DATETIME'),
    ('REGISTRATION_END_DATE', 'Registration End Date',
     'Defines when registration closes', 'REGISTRATION', 'DATETIME'),
    ('REQUIRE_EDUCATIONAL_INSTITUTION', 'Educational Institution Required',
     'Requires an educational institution for registration', 'REGISTRATION', 'BOOLEAN'),
    ('REQUIRE_COACH', 'Coach Required',
     'Requires a coach to be registered', 'STAFF', 'BOOLEAN'),
    ('REQUIRE_ASSISTANT_COACH', 'Assistant Coach Required',
     'Requires an assistant coach to be registered', 'STAFF', 'BOOLEAN'),
    ('REQUIRE_SPONSOR', 'Sponsor Required',
     'Requires a sponsor for registration', 'REGISTRATION', 'BOOLEAN'),
    ('AGE_RESTRICTION', 'Age Restriction',
     'Restricts participants according to age', 'ELIGIBILITY', 'JSON'),
    ('GENDER_RESTRICTION', 'Gender Restriction',
     'Restricts participants according to gender category', 'ELIGIBILITY', 'JSON'),
    ('MIN_TEAM_MEMBERS', 'Minimum Team Members',
     'Minimum number of team members required', 'TEAM', 'NUMBER'),
    ('MAX_TEAM_MEMBERS', 'Maximum Team Members',
     'Maximum number of team members allowed', 'TEAM', 'NUMBER'),
    ('MAX_SUBSTITUTES', 'Maximum Substitutes',
     'Maximum number of substitutes allowed', 'TEAM', 'NUMBER'),
    ('DOCUMENT_REQUIRED', 'Required Document',
     'Defines a document required during registration', 'DOCUMENT', 'JSON');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- End of initial schema
-- ============================================================
