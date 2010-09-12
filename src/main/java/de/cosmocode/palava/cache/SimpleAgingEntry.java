package de.cosmocode.palava.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A map value which holds it's age.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
final class SimpleAgingEntry implements AgingEntry {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleAgingEntry.class);
    
    private final long timestamp = System.currentTimeMillis();
    private final Object value;
    private final long maxAge;
    private final TimeUnit maxAgeUnit;
    
    public SimpleAgingEntry(@Nullable Object value, long maxAge, TimeUnit maxAgeUnit) {
        this.value = value;
        this.maxAge = maxAge;
        this.maxAgeUnit = Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean isExpired() {
        return getAge(maxAgeUnit) > maxAge;
    }
    
    @Override
    public <V> V getValue() {
        if (isExpired()) {
            LOG.trace("Entry found but was too old");
            return null;
        } else {
            @SuppressWarnings("unchecked")
            final V typed = (V) value;
            return typed;
        }
    }
    
}
