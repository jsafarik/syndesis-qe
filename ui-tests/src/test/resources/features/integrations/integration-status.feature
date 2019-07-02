# @sustainer: mastepan@redhat.com
@stage-smoke
@ui
@database
@integrations-check-starting-status
Feature: Integration - DB to DB

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis

  @integration-check-starting-status-on-detail-page
  Scenario: Check starting integration status on detail page
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    When click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    And click on the "Next" button

    And publish integration

    And fill Name Integration form
      |  name | ASD |

    And publish integration

    Then check starting integration status on Integration Detail page

  @integration-check-starting-status-on-integration-list
  Scenario: Check starting integration status on integration list
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    When click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    When click on the "Next" button

    And publish integration

    And fill Name Integration form
      | name | ASD |

    And publish integration

    When navigate to the "Integrations" page

    Then check starting integration ASD status on Integrations page

  @integration-check-starting-status-on-home-page
  Scenario: Check starting integration status on home page
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    And click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in periodic query input with "SELECT * FROM todo" value
    And click on the "Next" button

    And click on the "Publish" button

    And fill Name Integration form
      | Name | ASD |

    And publish integration

    When navigate to the "Home" page

    Then check starting integration ASD status on Home page
