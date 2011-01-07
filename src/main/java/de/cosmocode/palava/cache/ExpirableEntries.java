package de.cosmocode.palava.cache;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * Static utility class for {@link ExpirableEntry}s.
 *
 * @since 3.0
 * @author Willi Schoenborn
 */
final class ExpirableEntries {

    private ExpirableEntries() {
        
    }

    /**
     * Creates an {@link ExpirableEntry} based on the specified.
     *
     * @since 3.0
     * @param expiration the cache expiration
     * @param value the value
     * @return the expirable entry containing value
     * @throws NullPointerException if expiration is null
     */
    static ExpirableEntry create(@Nonnull CacheExpiration expiration, Object value) {
        Preconditions.checkNotNull(expiration, "Expiration");
        if (expiration.isEternal()) {
            return new EternalEntry(value);
        } else if (expiration.getIdleTime() == 0L) {
            return new SimpleExpirableEntry(value, expiration.getLifeTime(), expiration.getLifeTimeUnit());
        } else {
            return new ComplexExpirableEntry(value, expiration);
        }
    }
    
}
