package de.cosmocode.palava.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * A constant {@link AgingEntry} which is always expired.
 * 
 * @author Willi Schoenborn
 */
enum AgedEntry implements AgingEntry {

    INSTANCE;
    
    public static final Function<Serializable, AgingEntry> PRODUCER = new Function<Serializable, AgingEntry>() {
        
        @Override
        public AgingEntry apply(Serializable key) {
            return AgedEntry.INSTANCE;
        }
    
    };

    private static final Logger LOG = LoggerFactory.getLogger(AgedEntry.class);
    
    @Override
    public long getTimestamp() {
        return 0;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "Unit");
        return unit.convert(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public boolean isExpired() {
        return true;
    }
    
    @Override
    public <V> V getValue() {
        LOG.trace("No entry found");
        return null;
    }
    
}
