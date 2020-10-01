/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.credential.mfa.shiro;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.AbstractKapuaUpdatableEntity;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authentication.credential.mfa.MfaOption;
import org.eclipse.kapua.service.authentication.credential.mfa.ScratchCode;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@Entity(name = "MfaOption")
@Table(name = "atht_mfa_option")
/**
 * {@link MfaOption} implementation.
 */
public class MfaOptionImpl extends AbstractKapuaUpdatableEntity implements MfaOption {

    private static final long serialVersionUID = -1872939877726584407L;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "eid", column = @Column(name = "user_id", updatable = false, nullable = false))
    })
    private KapuaEid userId;

    @Basic
    @Column(name = "mfa_secret_key", nullable = false)
    private String mfaSecretKey;

    @Basic
    @Column(name = "trust_key")
    private String trustKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "trust_expiration_date")
    protected Date trustExpirationDate;

    @Transient
    private String qrCodeImage;

    @Transient
    private List<ScratchCode> scratchCodes;

    @Basic
    @Column(name = "enforced")
    private Boolean enforced;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "activated_on_date")
    protected Date activatedOnDate;

    /**
     * Constructor.
     */
    public MfaOptionImpl() {
        super();
    }

    /**
     * Constructor.
     *
     * @param scopeId The scope {@link KapuaId} to set into the {@link MfaOption}.
     */
    public MfaOptionImpl(KapuaId scopeId) {
        super(scopeId);
    }

    /**
     * Constructor.
     *
     * @param scopeId      The scope {@link KapuaId} to set into the {@link MfaOption}.
     * @param userId       user identifier
     * @param mfaSecretKey The secret key to set into the {@link MfaOption}.
     */
    public MfaOptionImpl(KapuaId scopeId, KapuaId userId, String mfaSecretKey) {
        super(scopeId);
        this.userId = KapuaEid.parseKapuaId(userId);
        this.mfaSecretKey = mfaSecretKey;
    }

    /**
     * Clone constructor.
     *
     * @param mfaOption
     * @throws KapuaException
     */
    public MfaOptionImpl(MfaOption mfaOption) throws KapuaException {
        super(mfaOption);
        setUserId(mfaOption.getUserId());
        setMfaSecretKey(mfaOption.getMfaSecretKey());
        setTrustKey(mfaOption.getTrustKey());
        setTrustExpirationDate(mfaOption.getTrustExpirationDate());
        setEnforced(mfaOption.isEnforced());
        setActivatedOnDate(mfaOption.getActivatedOnDate());
    }

    @Override
    public KapuaId getUserId() {
        return userId;
    }

    @Override
    public void setUserId(KapuaId userId) {
        this.userId = KapuaEid.parseKapuaId(userId);
    }

    @Override
    public String getMfaSecretKey() {
        return mfaSecretKey;
    }

    @Override
    public void setMfaSecretKey(String mfaSecretKey) {
        this.mfaSecretKey = mfaSecretKey;
    }

    @Override
    public String getTrustKey() {
        return this.trustKey;
    }

    @Override
    public void setTrustKey(String trustKey) {
        this.trustKey = trustKey;
    }

    @Override
    public Date getTrustExpirationDate() {
        return trustExpirationDate;
    }

    @Override
    public void setTrustExpirationDate(Date trustExpirationDate) {
        this.trustExpirationDate = trustExpirationDate;
    }

    @Override
    public String getQRCodeImage() {
        return qrCodeImage;
    }

    @Override
    public void setQRCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    @Override
    public List<ScratchCode> getScratchCodes() {
        return scratchCodes;
    }

    @Override
    public void setScratchCodes(List<ScratchCode> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }

    @Override
    public Boolean isEnforced() {
        return enforced;
    }

    @Override
    public void setEnforced(Boolean enforced) {
        this.enforced = enforced;
    }

    @Override
    public Date getActivatedOnDate() {
        return activatedOnDate;
    }

    @Override
    public void setActivatedOnDate(Date activatedOnDate) {
        this.activatedOnDate = activatedOnDate;
    }

}
