package io.syndesis.qe;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features", tags = {"not @manual", "not @wip", "not @ignore"},
    plugin = {
            "pretty",
            "html:target/cucumber/cucumber-html",
            "junit:target/cucumber/cucumber-junit.xml",
            "json:target/cucumber/cucumber-report.json",
            "io.syndesis.qe.cucumber.MailFormatter:target/cucumber/cucumber-mail/"
    })
public class CucumberTestsRunner extends TestSuiteParent {

    //we could have some setup here

}
