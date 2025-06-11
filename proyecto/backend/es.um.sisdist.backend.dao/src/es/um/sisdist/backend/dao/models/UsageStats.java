package es.um.sisdist.backend.dao.models;

public class UsageStats {
    private int totalPrompts;
    private int totalConversations;
    private long lastAccessTimestamp; // Epoch millis (puedes usar LocalDateTime si prefieres)

    public UsageStats() {
    }

    public UsageStats(int totalPrompts, int totalConversations, long lastAccessTimestamp) {
        this.totalPrompts = totalPrompts;
        this.totalConversations = totalConversations;
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    public int getTotalPrompts() {
        return totalPrompts;
    }

    public void setTotalPrompts(int totalPrompts) {
        this.totalPrompts = totalPrompts;
    }

    public int getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(int totalConversations) {
        this.totalConversations = totalConversations;
    }

    public long getLastAccessTimestamp() {
        return lastAccessTimestamp;
    }

    public void setLastAccessTimestamp(long lastAccessTimestamp) {
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    @Override
    public String toString() {
        return "UsageStats{" +
                "totalPrompts=" + totalPrompts +
                ", totalConversations=" + totalConversations +
                ", lastAccessTimestamp=" + lastAccessTimestamp +
                '}';
    }
}
