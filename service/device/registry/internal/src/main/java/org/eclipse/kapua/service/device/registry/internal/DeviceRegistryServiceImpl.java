/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.device.registry.internal;

import com.google.common.collect.Lists;
import org.eclipse.kapua.KapuaDuplicateNameException;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.KapuaMaxNumberOfItemsReachedException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.jpa.EntityManagerContainer;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.KapuaUpdatableEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceAttributes;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceDomains;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceListResult;
import org.eclipse.kapua.service.device.registry.DeviceQuery;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.common.DeviceValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.Serializable;

/**
 * {@link DeviceRegistryService} implementation.
 *
 * @since 1.0.0
 */
@KapuaProvider
public class DeviceRegistryServiceImpl extends AbstractKapuaConfigurableResourceLimitedService<Device, DeviceCreator, DeviceRegistryService, DeviceListResult, DeviceQuery, DeviceFactory>
        implements DeviceRegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistryServiceImpl.class);

    private Cache<Serializable, KapuaUpdatableEntity> deviceIdCache =
            serviceCacheManager.getCache(DeviceRegistryCacheFactory.getDeviceIdCacheName());
    private Cache<Serializable, KapuaUpdatableEntity> deviceClientIdCache =
            serviceCacheManager.getCache(DeviceRegistryCacheFactory.getDeviceClientIdCacheName());

    /**
     * Constructor
     *
     * @param deviceEntityManagerFactory
     */
    public DeviceRegistryServiceImpl(DeviceEntityManagerFactory deviceEntityManagerFactory) {
        super(DeviceRegistryService.class.getName(), DeviceDomains.DEVICE_DOMAIN, deviceEntityManagerFactory,
                DeviceRegistryCacheFactory.getInstance(), DeviceRegistryService.class,
                DeviceFactory.class);
    }

    /**
     * Constructor
     */
    public DeviceRegistryServiceImpl() {
        this(DeviceEntityManagerFactory.instance());
    }

    // Operations implementation
    @Override
    public Device create(DeviceCreator deviceCreator) throws KapuaException {
        DeviceValidation.validateCreatePreconditions(deviceCreator);

        //
        // Check limits
        if (allowedChildEntities(deviceCreator.getScopeId()) <= 0) {
            throw new KapuaMaxNumberOfItemsReachedException("Devices");
        }

        //
        // Check duplicate clientId
        DeviceQuery query = new DeviceQueryImpl(deviceCreator.getScopeId());
        query.setPredicate(query.attributePredicate(DeviceAttributes.CLIENT_ID, deviceCreator.getClientId()));

        if (count(query) > 0) {
            throw new KapuaDuplicateNameException(deviceCreator.getClientId());
        }

        return entityManagerSession.onTransactedInsert(EntityManagerContainer.<Device>create().onResultHandler(entityManager -> DeviceDAO.create(entityManager, deviceCreator)));
    }

    @Override
    public Device update(Device device) throws KapuaException {
        DeviceValidation.validateUpdatePreconditions(device);

        return entityManagerSession.onTransactedResult(EntityManagerContainer.<Device>create().onResultHandler(entityManager -> {
            Device currentDevice = DeviceDAO.find(entityManager, device.getScopeId(), device.getId());
            if (currentDevice == null) {
                throw new KapuaEntityNotFoundException(Device.TYPE, device.getId());
            }
            // Update
            return DeviceDAO.update(entityManager, device);
        }).onBeforeVoidHandler(() -> {
            // search also for the name of the cached object, since the name might have been changed
            Device cachedDevice = (Device) deviceIdCache.get(concatenateCacheKey(device.getScopeId(), device.getId()));
            if (cachedDevice != null) {
                deviceClientIdCache.remove(concatenateCacheKey(device.getScopeId(), cachedDevice.getClientId()));
            }
            deviceIdCache.remove(concatenateCacheKey(device.getScopeId(), device.getId()));
            deviceClientIdCache.remove(concatenateCacheKey(device.getScopeId(), device.getClientId()));
        }));
    }

    @Override
    public Device find(KapuaId scopeId, KapuaId entityId) throws KapuaException {
        DeviceValidation.validateFindPreconditions(scopeId, entityId);

        return entityManagerSession.onResult(EntityManagerContainer.<Device>create().onResultHandler(entityManager -> DeviceDAO.find(entityManager, scopeId, entityId))
                .onBeforeResultHandler(() -> {
                            Device device = (Device) deviceIdCache.get(concatenateCacheKey(scopeId, entityId));
                            if (device!=null && !device.getScopeId().equals(scopeId)) {
                                throw new KapuaIllegalArgumentException("scopeId", "scopeId does not correspond to the device one");
                            }
                            return device;
                        })
                .onAfterResultHandler((entity) -> {
                    deviceIdCache.put(concatenateCacheKey(scopeId, entityId), entity);
                    deviceClientIdCache.put(concatenateCacheKey(scopeId, entity.getClientId()), entity);
                }));
    }

    @Override
    public DeviceListResult query(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateQueryPreconditions(query);

        return entityManagerSession.onResult(EntityManagerContainer.<DeviceListResult>create().onResultHandler(entityManager -> DeviceDAO.query(entityManager, query)));
    }

    @Override
    public long count(KapuaQuery<Device> query) throws KapuaException {
        DeviceValidation.validateCountPreconditions(query);

        return entityManagerSession.onResult(EntityManagerContainer.<Long>create().onResultHandler(entityManager -> DeviceDAO.count(entityManager, query)));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId deviceId) throws KapuaException {
        DeviceValidation.validateDeletePreconditions(scopeId, deviceId);

        entityManagerSession.doTransactedAction(EntityManagerContainer.create().onVoidResultHandler(entityManager -> DeviceDAO.delete(entityManager, scopeId, deviceId))
                .onAfterVoidHandler(() -> {
                    Device device = (Device) deviceIdCache.get(concatenateCacheKey(scopeId, deviceId));
                    if (device != null) {
                        deviceClientIdCache.remove(concatenateCacheKey(scopeId, device.getClientId()));
                    }
                    deviceIdCache.remove(concatenateCacheKey(scopeId, deviceId));
                }));
    }

    @Override
    public Device findByClientId(KapuaId scopeId, String clientId) throws KapuaException {

        DeviceValidation.validateFindByClientIdPreconditions(scopeId, clientId);
        Device device = (Device) deviceClientIdCache.get(concatenateCacheKey(scopeId, clientId));

        if (device==null) {
            DeviceQueryImpl query = new DeviceQueryImpl(scopeId);
            query.setPredicate(query.attributePredicate(DeviceAttributes.CLIENT_ID, clientId));
            query.setFetchAttributes(Lists.newArrayList(DeviceAttributes.CONNECTION, DeviceAttributes.LAST_EVENT));

            //
            // Query and parse result
            DeviceListResult result = query(query);
            if (!result.isEmpty()) {
                device = result.getFirstItem();
            }
            if (device!=null) {
                deviceIdCache.put(concatenateCacheKey(scopeId, device.getId()), device);
                deviceClientIdCache.put(concatenateCacheKey(scopeId, clientId), device);
            }
        } else if (!device.getScopeId().equals(scopeId)) {
            throw new KapuaIllegalArgumentException("scopeId", "scopeId does not correspond to the device one");
        }
        return device;
    }

    //@ListenServiceEvent(fromAddress="account")
    //@ListenServiceEvent(fromAddress="authorization")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            //service bus error. Throw some exception?
        }
        LOGGER.info("DeviceRegistryService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        if ("group".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteDeviceByGroupId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        } else if ("account".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteDeviceByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    private void deleteDeviceByGroupId(KapuaId scopeId, KapuaId groupId) throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        DeviceFactory deviceFactory = locator.getFactory(DeviceFactory.class);

        DeviceQuery query = deviceFactory.newQuery(scopeId);
        query.setPredicate(query.attributePredicate(DeviceAttributes.GROUP_ID, groupId));

        DeviceListResult devicesToDelete = query(query);

        for (Device d : devicesToDelete.getItems()) {
            d.setGroupId(null);
            update(d);
        }
    }

    private void deleteDeviceByAccountId(KapuaId scopeId, KapuaId accountId) throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        DeviceFactory deviceFactory = locator.getFactory(DeviceFactory.class);

        DeviceQuery query = deviceFactory.newQuery(accountId);

        DeviceListResult devicesToDelete = query(query);

        for (Device d : devicesToDelete.getItems()) {
            delete(d.getScopeId(), d.getId());
        }
    }

}
