DROP DATABASE IF EXISTS newsfeed_db;
CREATE DATABASE newsfeed_db;

\c newsfeed_db;

CREATE TABLE users (
       id BIGSERIAL PRIMARY KEY,
       username VARCHAR(50) NOT NULL UNIQUE,
       email VARCHAR(100) NOT NULL UNIQUE,
       password_hash VARCHAR(255) NOT NULL, -- encoded password
       bio TEXT,
       profile_pic_url TEXT,
       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
       isValid BOOLEAN default true
);

-- POSTS TABLE (depends on users)
CREATE TABLE posts (
       id BIGSERIAL PRIMARY KEY,
       user_id BIGINT NOT NULL,
       content TEXT,

        -- App will send  unique ID (UUID). if ID is the same, then we will ignore the request.
       request_id VARCHAR(64) UNIQUE,

       like_count INT DEFAULT 0,
       attachment_count INT DEFAULT 0,
       share_count INT DEFAULT 0,

       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
       isValid BOOLEAN default true,

       CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_posts_request_id ON posts(request_id);

-- FOLLOWS TABLE (Refresher - Important for "Followed some people" logic)
CREATE TABLE follows (
     follower_id BIGINT NOT NULL,
     followee_id BIGINT NOT NULL,

    -- THE IDEMPOTENCY KEY
     request_id VARCHAR(64) UNIQUE NOT NULL,

     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     isValid BOOLEAN default true,

     PRIMARY KEY (follower_id, followee_id),
     CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
     CONSTRAINT fk_followee FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_follows_request_id ON follows(request_id);

-- INTERACTIONS (depends on users and posts)
DROP TABLE IF EXISTS interactions;

CREATE TABLE interactions (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL,
      post_id BIGINT NOT NULL,
      action_type VARCHAR(20) NOT NULL, -- 'LIKED', 'SHARED', 'BOOKMARKED'

    -- THE IDEMPOTENCY KEY (Must be NOT NULL)
      request_id VARCHAR(64) UNIQUE NOT NULL,

      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      isValid BOOLEAN default true,

      CONSTRAINT fk_interact_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      CONSTRAINT fk_interact_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- 1. Idempotency fast lookup
CREATE INDEX idx_interaction_request_id ON interactions(request_id);

-- 2. The Soft-Delete Fix (Only enforce unique if isValid is true)
CREATE UNIQUE INDEX idx_unique_active_interaction
    ON interactions(user_id, post_id, action_type)
    WHERE isValid = true;

-- POST ATTACHMENTS (depends on posts)
CREATE TABLE post_attachments (
      id BIGSERIAL PRIMARY KEY,
      post_id BIGINT NOT NULL,

-- type, contentUrl, previewImageUrl
      media_type VARCHAR(20) NOT NULL, -- 'IMAGE' or 'VIDEO'
      content_url TEXT NOT NULL,       -- Original High-Res URL
      thumbnail_url TEXT,              -- Small size for Feed (Optimization)
      caption TEXT,

      display_order INT DEFAULT 0,     -- there are multiple attachments then what is sequence
      isValid BOOLEAN default true,
      CONSTRAINT fk_attachment_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- -- FEED ITEMS (depends on users and posts)
-- CREATE TABLE feed_items (
--     id BIGSERIAL PRIMARY KEY,
--     user_id BIGINT NOT NULL, -- who's post
--     post_id BIGINT NOT NULL, -- what client will see
--     created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Sorting
--     isValid BOOLEAN default true,
--
--     CONSTRAINT fk_feed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
--     CONSTRAINT fk_feed_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
-- );

-- CREATE INDEX IF NOT EXISTS idx_feed_user_created ON feed_items(user_id, created_at DESC) WHERE isValid = true;
