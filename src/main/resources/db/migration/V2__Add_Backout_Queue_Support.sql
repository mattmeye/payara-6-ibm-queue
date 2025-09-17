-- Add backout queue support columns to mq_messages table
ALTER TABLE mq_messages
ADD COLUMN backout_count INTEGER DEFAULT 0,
ADD COLUMN backout_at TIMESTAMP,
ADD COLUMN redelivery_scheduled_at TIMESTAMP,
ADD COLUMN escalated BOOLEAN DEFAULT FALSE,
ADD COLUMN escalated_at TIMESTAMP;

-- Create indexes for performance
CREATE INDEX idx_mq_messages_backout_status ON mq_messages(status) WHERE status = 'BACKOUT';
CREATE INDEX idx_mq_messages_escalated ON mq_messages(escalated) WHERE escalated = TRUE;
CREATE INDEX idx_mq_messages_redelivery_scheduled ON mq_messages(redelivery_scheduled_at) WHERE redelivery_scheduled_at IS NOT NULL;
CREATE INDEX idx_mq_messages_backout_count ON mq_messages(backout_count) WHERE backout_count > 0;

-- Add check constraints
ALTER TABLE mq_messages
ADD CONSTRAINT chk_backout_count_positive CHECK (backout_count >= 0),
ADD CONSTRAINT chk_escalated_boolean CHECK (escalated IN (TRUE, FALSE));

-- Update any existing failed messages to have proper backout count
UPDATE mq_messages
SET backout_count = CASE
    WHEN retry_count > 3 THEN 1
    ELSE 0
END
WHERE status = 'FAILED' AND backout_count IS NULL;

-- Comment on new columns
COMMENT ON COLUMN mq_messages.backout_count IS 'Number of times message has been sent to backout queue';
COMMENT ON COLUMN mq_messages.backout_at IS 'Timestamp when message was last sent to backout queue';
COMMENT ON COLUMN mq_messages.redelivery_scheduled_at IS 'Scheduled time for redelivery from backout queue';
COMMENT ON COLUMN mq_messages.escalated IS 'Whether message has been escalated to escalation queue';
COMMENT ON COLUMN mq_messages.escalated_at IS 'Timestamp when message was escalated';