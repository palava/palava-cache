/**
 * palava - a java-php-bridge
 * Copyright (C) 2007  CosmoCode GmbH
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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cosmocode.palava.core.call.Call;
import de.cosmocode.palava.core.command.Response;
import de.cosmocode.palava.core.protocol.content.JsonContent;
import de.cosmocode.palava.core.server.Server;
import de.cosmocode.palava.core.session.HttpSession;
import de.cosmocode.palava.legacy.Job;
import de.cosmocode.palava.services.cache.CacheService;

/**
 * 
 *
 * @author Willi Schoenborn
 */
public final class ClearCache implements Job {

    private static final Logger log = LoggerFactory.getLogger(ClearCache.class);

    
    private CacheService cacheService;
    
    @Override
    public void process(Call request, Response response, HttpSession session, Server server, Map<String, Object> caddy)
        throws Exception {

        log.info("Clearing cache");
        cacheService.clear();
        log.info("Cache cleared");
        
        response.setContent(JsonContent.EMPTY);
        
    }

}
