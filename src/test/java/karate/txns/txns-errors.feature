Feature: test error handlig in txns

Background:
* def common = callonce read('common-once-txns.feature')
* url common.baseUrl
* def txn = call read('common-start-txn.feature') { baseUrl: '#(common.baseUrl)' }

# test requests on txn that are invalid due to current txn status

@requires-mongodb-4 @requires-replica-set
Scenario: start txn when already IN
    Given path '/_sessions/' + txn.sid + '/_txns'
    And request {}
    When method POST
    Then status 304

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }

@requires-mongodb-4 @requires-replica-set
Scenario: commit txn when txn status=NONE
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }

    Given path '/_sessions'
    And request {}
    When method POST
    Then status 201
    And match header Location contains '/_sessions/'
    * def sid = common.sid(responseHeaders['Location'][0])

    Given path '/_sessions/' + sid + '/_txns/' + 1
    And request {}
    When method PATCH
    Then status 406
    * print "**** TODO this 406 must be handled to return a better error status code"

@requires-mongodb-4 @requires-replica-set
Scenario: abort txn when txn status=NONE
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }

    Given path '/_sessions'
    And request {}
    When method POST
    Then status 201
    And match header Location contains '/_sessions/'
    * def sid = common.sid(responseHeaders['Location'][0])

    Given path '/_sessions/' + sid + '/_txns/' + 1
    When method DELETE
    Then status 204
    * print "**** TODO this 204 must be handled to return a better error status code"

@requires-mongodb-4 @requires-replica-set
Scenario: abort txn when txn status=ABORTED
    * def sid = txn.sid
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(sid)', txn: 1 }

    Given path '/_sessions/' + sid + '/_txns/' + 1
    And request {}
    When method DELETE
    Then status 204

Scenario: commit txn when txn status=ABORTED
    * def sid = txn.sid
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(sid)', txn: 1 }

    Given path '/_sessions/' + sid + '/_txns/' + 1
    And request {}
    When method PATCH
    Then status 406
    * print "**** TODO this 406 must be handled to return a better error status code"

# test requests on txn that are invalid due to wrong txn id

@requires-mongodb-4 @requires-replica-set
Scenario: create a document using a wrong txnId
    * def sid = txn.sid
    
    Given path common.db + common.coll
    And request { a: 101 }
    And param sid = sid
    And param txn = 2
    When method POST
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(sid)', txn: 1 }

Scenario: GET collection using a wrong txnId
    * def sid = txn.sid
    
    Given path common.db + common.coll
    And param sid = sid
    And param txn = 2
    When method GET
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(sid)', txn: 1 }

# test requests on documents that are invalid due to wrong txn status

@requires-mongodb-4 @requires-replica-set
Scenario: create a document in committed txn
    * def sid = txn.sid
    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }
    
    Given path common.db + common.coll
    And request { a: 101 }
    And param sid = sid
    And param txn = 1
    When method POST
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

@requires-mongodb-4 @requires-replica-set
Scenario: GET collection in committed txn
    * def sid = txn.sid
    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }
    
    Given path common.db + common.coll
    And param sid = sid
    And param txn = 1
    When method GET
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

@requires-mongodb-4 @requires-replica-set
Scenario: create a document in aborted txn
    * def sid = txn.sid
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }
    
    Given path common.db + common.coll
    And request { a: 101 }
    And param sid = sid
    And param txn = 1
    When method POST
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

@requires-mongodb-4 @requires-replica-set
Scenario: GET collection in aborted txn
    * def sid = txn.sid
    * call read('common-abort-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }
    
    Given path common.db + common.coll
    And param sid = sid
    And param txn = 1
    When method GET
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

# test requests on documents that are invalid due to wrong txn id

@requires-mongodb-4 @requires-replica-set
Scenario: create a document with wrong txn id
    * def sid = txn.sid
    
    Given path common.db + common.coll
    And request { a: 101 }
    And param sid = sid
    And param txn = 2
    When method POST
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }

@requires-mongodb-4 @requires-replica-set
Scenario: GET collection with wrong txn id
    * def sid = txn.sid
    
    Given path common.db + common.coll
    And param sid = sid
    And param txn = 2
    When method GET
    Then status 500
    * print "**** TODO this 500 must be handled to return a better status code"

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }

# test conflicting txns

@requires-mongodb-4 @requires-replica-set
Scenario: create a document in txn T1, create conflicting document in T2. T2 aborts, T1 can commit
    * def txn2 = call read('common-start-txn.feature') { baseUrl: '#(common.baseUrl)' }
    
    Given path common.db + common.coll
    And request { _id: 1, a: 101 }
    And param sid = txn.sid
    And param txn = 1
    When method POST
    Then status 201

    Given path common.db + common.coll
    And request { _id: 1, a: 101 }
    And param sid = txn2.sid
    And param txn = 1
    When method POST
    Then status 500
    And print "**** TODO this 500 must be handled to return a better status code"

    Given path '/_sessions/' + txn.sid + '/_txns'
    When method GET
    Then status 200
    And match response.currentTxn.status == 'IN'
    And match response.currentTxn.id == 1

    Given path '/_sessions/' + txn2.sid + '/_txns'
    When method GET
    Then status 200
    And match response.currentTxn.status == 'ABORTED'
    And match response.currentTxn.id == 1

    * call read('common-commit-txn.feature') { baseUrl: '#(common.baseUrl)', sid: '#(txn.sid)', txn: 1 }