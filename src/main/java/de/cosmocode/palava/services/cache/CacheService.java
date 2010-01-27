/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.cosmocode.palava.services.cache;

import java.io.Serializable;

import de.cosmocode.palava.core.Service;

/**
 * A {@link Service} used to cache objects.
 *
 * @author Willi Schoenborn
 */
public interface CacheService {

    /**
     * Adds an object to the cache. 
     * 
     * @param key the cache key
     * @param value the value being stored
     */
    void store(Serializable key, Object value);
    
    /**
     * Reads an object from the cache.
     * 
     * <p>
     *   <strong>Note:</strong>
     *   This method automatically casts into the needed
     *   type. It's in the callers responsibilities that
     *   no ClassCastException occurs.
     * </p>
     * 
     * @param <T> the generic object type
     * @param key the cache key
     * @return a casted instance of T or null, if there was
     *         no value cached for the given key
     */
    <T> T read(Serializable key);
    
    /**
     * Clears an object from the cache by key.
     * 
     * <p>
     *   <strong>Note:</strong>
     *   This method automatically casts into the needed
     *   type. It's in the callers responsibilities that
     *   no ClassCastException occurs.
     * </p>
     * 
     * @param <T> the generic object type
     * @param key the cache key
     * @return the object that associated with key or null if there was none
     */
    <T> T remove(Serializable key);
    
    /**
     * Clears the cache.
     */
    void clear();
    
}
