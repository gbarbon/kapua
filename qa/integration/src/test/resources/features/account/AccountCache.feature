###############################################################################
# Copyright (c) 2020 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
###############################################################################
@account
@accountCache
@integration

Feature: Account cache feature
  Testing cache.

  Scenario: Test cache performances
  Bla
    When I login as user with name "kapua-sys" and password "kapua-password"
    Given I create a generic account with name "test_account"
    And I configure the device registry service
      | type    | name                   | value |
      | boolean | infiniteChildEntities  | true  |
      | integer | maxNumberChildEntities |  1000   |
    And The device "client-id-1"
    Then I am able to use the cache for the account "test_account" and clientId "client-id-1"
    And I logout
