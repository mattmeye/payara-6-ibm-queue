-- PostgreSQL initialization script for MQ message storage
-- This script creates the necessary tables and indexes for the MQ integration

-- Create the main messages table
CREATE TABLE IF NOT EXISTS mq_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) UNIQUE NOT NULL,
    correlation_id VARCHAR(255),
    queue_name VARCHAR(255) NOT NULL,
    message_content TEXT,
    message_type VARCHAR(50),
    priority INTEGER,
    expiry BIGINT,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_mq_messages_message_id ON mq_messages(message_id);
CREATE INDEX IF NOT EXISTS idx_mq_messages_correlation_id ON mq_messages(correlation_id);
CREATE INDEX IF NOT EXISTS idx_mq_messages_queue_name ON mq_messages(queue_name);
CREATE INDEX IF NOT EXISTS idx_mq_messages_status ON mq_messages(status);
CREATE INDEX IF NOT EXISTS idx_mq_messages_received_at ON mq_messages(received_at);
CREATE INDEX IF NOT EXISTS idx_mq_messages_processed_at ON mq_messages(processed_at);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_mq_messages_status_received ON mq_messages(status, received_at);
CREATE INDEX IF NOT EXISTS idx_mq_messages_queue_status ON mq_messages(queue_name, status);

-- Create a view for active messages (not processed yet)
CREATE OR REPLACE VIEW active_mq_messages AS
SELECT
    id,
    message_id,
    correlation_id,
    queue_name,
    message_content,
    message_type,
    priority,
    received_at,
    status,
    retry_count
FROM mq_messages
WHERE status IN ('RECEIVED', 'PROCESSING', 'RETRY')
ORDER BY received_at ASC;

-- Create a view for message statistics
CREATE OR REPLACE VIEW mq_message_stats AS
SELECT
    queue_name,
    status,
    COUNT(*) as message_count,
    MIN(received_at) as oldest_message,
    MAX(received_at) as newest_message,
    AVG(EXTRACT(EPOCH FROM (processed_at - received_at))) as avg_processing_time_seconds
FROM mq_messages
GROUP BY queue_name, status;

-- Create a function to clean up old processed messages
CREATE OR REPLACE FUNCTION cleanup_old_messages(days_old INTEGER DEFAULT 30)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM mq_messages
    WHERE status = 'PROCESSED'
    AND processed_at < CURRENT_TIMESTAMP - INTERVAL '1 day' * days_old;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create a function to retry failed messages
CREATE OR REPLACE FUNCTION retry_failed_messages(max_retry_count INTEGER DEFAULT 3)
RETURNS INTEGER AS $$
DECLARE
    updated_count INTEGER;
BEGIN
    UPDATE mq_messages
    SET status = 'RETRY',
        retry_count = retry_count + 1,
        error_message = NULL
    WHERE status = 'FAILED'
    AND retry_count < max_retry_count;

    GET DIAGNOSTICS updated_count = ROW_COUNT;

    RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

-- Insert some sample data for testing (optional)
INSERT INTO mq_messages (message_id, queue_name, message_content, message_type, status)
VALUES
    ('SAMPLE001', 'DEV.QUEUE.1', 'Sample test message 1', 'TEXT', 'PROCESSED'),
    ('SAMPLE002', 'DEV.QUEUE.1', 'Sample test message 2', 'TEXT', 'RECEIVED'),
    ('SAMPLE003', 'DEV.QUEUE.2', 'Sample test message 3', 'TEXT', 'PROCESSING')
ON CONFLICT (message_id) DO NOTHING;

-- Grant permissions to the application user
GRANT SELECT, INSERT, UPDATE, DELETE ON mq_messages TO mquser;
GRANT USAGE, SELECT ON SEQUENCE mq_messages_id_seq TO mquser;
GRANT SELECT ON active_mq_messages TO mquser;
GRANT SELECT ON mq_message_stats TO mquser;
GRANT EXECUTE ON FUNCTION cleanup_old_messages(INTEGER) TO mquser;
GRANT EXECUTE ON FUNCTION retry_failed_messages(INTEGER) TO mquser;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'MQ Database initialization completed successfully';
    RAISE NOTICE 'Tables created: mq_messages';
    RAISE NOTICE 'Views created: active_mq_messages, mq_message_stats';
    RAISE NOTICE 'Functions created: cleanup_old_messages, retry_failed_messages';
END $$;