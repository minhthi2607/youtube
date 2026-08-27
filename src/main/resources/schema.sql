-- YouTube clone schema (MySQL)
-- This file is the single source of truth for the database schema.
-- spring.jpa.hibernate.ddl-auto is set to "validate": Hibernate never mutates
-- the schema, this script does. Keep entities and this file in sync.

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS videos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    file_path       VARCHAR(500) NOT NULL,
    content_type    VARCHAR(50)  NOT NULL,
    file_size_bytes BIGINT       NOT NULL,
    views           BIGINT       NOT NULL DEFAULT 0,
    uploader_id     BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_videos_uploader FOREIGN KEY (uploader_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_videos_title (title),
    INDEX idx_videos_uploader (uploader_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    VARCHAR(1000) NOT NULL,
    video_id   BIGINT        NOT NULL,
    user_id    BIGINT        NOT NULL,
    created_at DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_comments_video FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_comments_video_created (video_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id   BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    type       VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_likes_video FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_likes_user_video UNIQUE (user_id, video_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS subscriptions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id BIGINT      NOT NULL,
    channel_id    BIGINT      NOT NULL,
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_subscriptions_subscriber FOREIGN KEY (subscriber_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_channel FOREIGN KEY (channel_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_subscriptions_subscriber_channel UNIQUE (subscriber_id, channel_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
