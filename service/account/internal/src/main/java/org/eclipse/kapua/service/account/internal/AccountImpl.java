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
package org.eclipse.kapua.service.account.internal;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.AbstractKapuaNamedEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.Organization;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Account entity implementation.
 *
 * @since 1.0
 */
@Entity(name = "Account")
@NamedQueries({
        @NamedQuery(name = "Account.findChildAccounts", query = "SELECT a FROM Account a WHERE a.scopeId = :scopeId ORDER BY a.name"),
        @NamedQuery(name = "Account.findChildAccountsRecursive", query = "SELECT a FROM Account a WHERE a.parentAccountPath LIKE :parentAccountPath ORDER BY a.name")
})
@Table(name = "act_account")
public class AccountImpl extends AbstractKapuaNamedEntity implements Account {

    private static final long serialVersionUID = 8530992430658117928L;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "org_name")),
            @AttributeOverride(name = "personName", column = @Column(name = "org_person_name")),
            @AttributeOverride(name = "email", column = @Column(name = "org_email")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "org_phone_number")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "org_address_line_1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "org_address_line_2")),
            @AttributeOverride(name = "addressLine3", column = @Column(name = "org_address_line_3")),
            @AttributeOverride(name = "addressLine3", column = @Column(name = "org_address_line_3")),
            @AttributeOverride(name = "zipPostCode", column = @Column(name = "org_zip_postcode")),
            @AttributeOverride(name = "city", column = @Column(name = "org_city")),
            @AttributeOverride(name = "stateProvinceCounty", column = @Column(name = "org_state_province_county")),
            @AttributeOverride(name = "country", column = @Column(name = "org_country")),
    })
    private OrganizationImpl organization;

    @Basic
    @Column(name = "parent_account_path", nullable = false, updatable = true)
    private String parentAccountPath;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("name ASC")
    private List<AccountImpl> childAccounts;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration_date", nullable = false, updatable = true)
    protected Date expirationDate;

    /**
     * Constructor
     */
    protected AccountImpl() {
        super();
    }

    /**
     * Constructor.
     *
     * @param scopeId
     * @since 1.0.0
     */
    public AccountImpl(KapuaId scopeId) {
        super(scopeId);

        setParentAccountPath("");
    }

    /**
     * Constructor
     *
     * @param scopeId
     * @param name
     * @since 1.0.0
     */
    public AccountImpl(KapuaId scopeId, String name) {
        super(scopeId, name);

        setParentAccountPath("");
    }

    /**
     * Clone constructor.
     *
     * @throws KapuaException
     * @since 1.1.0
     */
    public AccountImpl(Account account) throws KapuaException {
        super(account);

        setOrganization(new OrganizationImpl(account.getOrganization()));
        setParentAccountPath(account.getParentAccountPath());
        setChildAccounts(account.getChildAccounts());
        setExpirationDate(account.getExpirationDate());
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization = (OrganizationImpl) organization;
    }

    @Override
    public String getParentAccountPath() {
        return parentAccountPath;
    }

    @Override
    public void setParentAccountPath(String parentAccountPath) {
        this.parentAccountPath = parentAccountPath;
    }

    @Override
    public List<Account> getChildAccounts() {
        if (childAccounts == null) {
            childAccounts = new ArrayList<>();
        }

        return new ArrayList<>(childAccounts);
    }

    private void setChildAccounts(List<Account> childAccounts) throws KapuaException {
        List<AccountImpl> accounts = new ArrayList<>();

        for (Account a : childAccounts) {
            accounts.add(new AccountImpl(a));
        }

        this.childAccounts = accounts;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Account clone() {
        Account tmp = new AccountImpl();
        tmp.setParentAccountPath(getParentAccountPath());
        tmp.setExpirationDate(getExpirationDate());
        tmp.setOrganization(getOrganization());
        tmp.setDescription(getDescription());
        tmp.setEntityAttributes(getEntityAttributes());
        tmp.setEntityProperties(getEntityProperties());
        tmp.setId(getId());
        tmp.setName(getName());
        tmp.setOptlock(getOptlock());
        tmp.setScopeId(getScopeId());
        return tmp;
    }
}
