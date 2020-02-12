/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.service.internal;

import com.codahale.metrics.Counter;
import org.eclipse.kapua.commons.metric.MetricServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheMetricReader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheMetricReader.class);
    private static final long TIME_TO_SLEEP = 5000;
    protected Counter cacheMiss;
    protected Counter cacheHit;
    protected Counter cacheRemoval;

    CacheMetricReader() {

    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(TIME_TO_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cacheMiss = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_miss_count");
            cacheHit = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_hit_count");
            cacheRemoval = MetricServiceFactory.getInstance().getCounter("cache", "cache", "cache_removal_count");
            LOGGER.info("Cache miss: {}. Cache hit {}. Cache removal {}.", cacheMiss.getCount(), cacheHit.getCount(),
                    cacheRemoval.getCount());
        }
    }

}
