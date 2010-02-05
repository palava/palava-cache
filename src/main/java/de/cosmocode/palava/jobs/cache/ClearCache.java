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

package de.cosmocode.palava.jobs.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.cosmocode.palava.bridge.Content;
import de.cosmocode.palava.bridge.call.Call;
import de.cosmocode.palava.bridge.command.Command;
import de.cosmocode.palava.bridge.command.CommandException;
import de.cosmocode.palava.bridge.content.JsonContent;
import de.cosmocode.palava.services.cache.CacheService;

/**
 * Clears the cache which is provided by {@link CacheService}.
 *
 * @author Willi Schoenborn
 */
public final class ClearCache implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(ClearCache.class);

    @Inject
    private CacheService cacheService;

    @Override
    public Content execute(Call call) throws CommandException {

        LOG.info("Clearing cache");
        cacheService.clear();
        LOG.info("Cache cleared");
        
        return JsonContent.EMPTY;
    }

}
