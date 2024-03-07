ALTER TABLE municipality
    ADD COLUMN include_school_ad_users BOOLEAN DEFAULT FALSE,
    ADD COLUMN school_domain VARCHAR(50);