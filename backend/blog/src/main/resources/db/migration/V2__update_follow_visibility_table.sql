-- =====================================================
-- Migration Script: Update follow_visibility table
-- Version: 2.0 - Separate visibility settings
-- Date: 2026-01-31
-- =====================================================
-- 
-- This migration updates the follow_visibility table from
-- the old single-visibility structure to the new structure
-- with separate settings for following/followers/friends/stats.
--
-- OPTION 1: Drop and recreate (recommended for development)
-- This will lose existing visibility settings.
-- =====================================================

DROP TABLE IF EXISTS follow_visibility;

-- The table will be automatically recreated by Hibernate on startup.
-- Alternatively, you can run the CREATE statement below:

CREATE TABLE follow_visibility (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Following list visibility settings
    following_is_public TINYINT(1) NOT NULL DEFAULT 1,
    following_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    following_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    following_allowed_nicknames VARCHAR(2000),
    following_blocked_nicknames VARCHAR(2000),
    
    -- Followers list visibility settings
    followers_is_public TINYINT(1) NOT NULL DEFAULT 1,
    followers_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    followers_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    followers_allowed_nicknames VARCHAR(2000),
    followers_blocked_nicknames VARCHAR(2000),
    
    -- Friends list visibility settings
    friends_is_public TINYINT(1) NOT NULL DEFAULT 1,
    friends_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    friends_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    friends_allowed_nicknames VARCHAR(2000),
    friends_blocked_nicknames VARCHAR(2000),
    
    -- Stats visibility settings
    stats_is_public TINYINT(1) NOT NULL DEFAULT 1,
    stats_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    stats_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    stats_allowed_nicknames VARCHAR(2000),
    stats_blocked_nicknames VARCHAR(2000),
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- OPTION 2: Add columns to existing table (preserves data)
-- Use this if you want to keep existing visibility settings.
-- =====================================================

-- If you prefer to add columns instead of dropping the table, run:
/*
ALTER TABLE follow_visibility
    -- Following settings
    ADD COLUMN following_is_public TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN following_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN following_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN following_allowed_nicknames VARCHAR(2000),
    ADD COLUMN following_blocked_nicknames VARCHAR(2000),
    
    -- Followers settings
    ADD COLUMN followers_is_public TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN followers_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN followers_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN followers_allowed_nicknames VARCHAR(2000),
    ADD COLUMN followers_blocked_nicknames VARCHAR(2000),
    
    -- Friends settings
    ADD COLUMN friends_is_public TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN friends_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN friends_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN friends_allowed_nicknames VARCHAR(2000),
    ADD COLUMN friends_blocked_nicknames VARCHAR(2000),
    
    -- Stats settings
    ADD COLUMN stats_is_public TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN stats_visible_to_friends TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN stats_visible_to_following TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN stats_allowed_nicknames VARCHAR(2000),
    ADD COLUMN stats_blocked_nicknames VARCHAR(2000);

-- Then drop old columns if they exist:
ALTER TABLE follow_visibility
    DROP COLUMN IF EXISTS is_public,
    DROP COLUMN IF EXISTS visible_to_friends,
    DROP COLUMN IF EXISTS visible_to_following,
    DROP COLUMN IF EXISTS allowed_nicknames,
    DROP COLUMN IF EXISTS blocked_nicknames;
*/
