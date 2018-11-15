ALTER TABLE artifacts_versions ADD COLUMN version_code BIGINT NOT NULL;
ALTER TABLE artifacts_versions ALTER COLUMN filename varchar NOT NULL