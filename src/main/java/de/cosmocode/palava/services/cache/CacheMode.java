package de.cosmocode.palava.services.cache;

/**
 * Used to configure different ways to handle cache
 * updates/refreshes.
 *
 * @author Willi Schoenborn
 */
public enum CacheMode {

    /**
     * Last recently used.
     */
    LRU,
    
    /**
     * Last frequently used.
     */
    LFU,
    
    /**
     * First in, first out.
     */
    FIFO,
    
    /**
     * No limit.
     */
    UNLIMITED,
    
    /**
     * Soft references.
     */
    AUTOMATIC,
    
    /**
     * Discard old values.
     */
    TIMEOUT,
    
    /**
     * {@link CacheMode#LRU} and {@link CacheMode#AUTOMATIC}.
     */
    HYBRID;
    
}
