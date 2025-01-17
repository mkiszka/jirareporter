package name.kiszka.codehelper;

import com.amirov.jirareporter.Reporter;
import com.amirov.jirareporter.RunnerParamsProvider;
import com.amirov.jirareporter.jira.JIRAClient;
import com.amirov.jirareporter.jira.JIRAWorkflow;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import jetbrains.buildServer.agent.BuildProgressLogger;
import name.kiszka.jirareporter.jira.JIRAProcessExceptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/***
 * This class is not a TEST.
 * This is a helper to discover how the Jira API and TeamCity API work (on real data).
 */
public class CodeHelperAsTest {
    @ExtendWith(MockitoExtension.class)
    @org.junit.jupiter.api.Test
    void codeReportHelper(@Mock BuildProgressLogger _logger) throws FileNotFoundException  {

        File jsonTCParams = new File("test/java/name/kiszka/codehelper/params/CodeHelperParams.json");
        if(!jsonTCParams.exists()) {
            assertTrue(true);
        }
        String jsonString = new Scanner(jsonTCParams).useDelimiter("\\Z").next();
        JSONObject providerParameters = new JSONObject(jsonString);


        RunnerParamsProvider prmsProvider = new RunnerParamsProvider(providerParameters, _logger);
        prmsProvider.set("build.type.id", "KomKOD_sandbox"); //system.teamcity.buildType.id
        prmsProvider.set("buildName", "sandbox"); //to musi być prawdziwa nazwa projektu budjącego

        //TeamCityXMLParser parteamCityXMLParserser = new TeamCityXMLParser(prmsProvider);
        TeamCityXMLParser teamCityXMLParser = Mockito.mock(TeamCityXMLParser.class,Mockito.withSettings().useConstructor(prmsProvider));
        Mockito.when(teamCityXMLParser.buildCommentText()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getArtifactHref()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getArtifactName()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getBuildHref()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getBuildId()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getBuildName()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getBuildStatus()).thenReturn("SUCCESS");
        Mockito.when(teamCityXMLParser.getIssueKeys()).thenCallRealMethod();
        Mockito.when(teamCityXMLParser.getBuildName()).thenReturn("Sandbox" );
        Mockito.when(teamCityXMLParser.getBuildNumber()).thenReturn("32" );
        Mockito.when(teamCityXMLParser.getWebUrl()).thenReturn("https://example.domain.com/" );

        //Collection<String> issueIds = parser.getIssueKeys(); //getIssueKeys has to be invoked, there _buildData is set and it's necessary to use TeamCityXMLParser.
        ArrayList<String> issueIds = new ArrayList<String>();
 //       issueIds.add("PP-5");
        issueIds.add("TEST-323");
        //Act
        try {
            Reporter reporter = new Reporter(prmsProvider, new JIRAClient(prmsProvider),new JIRAWorkflow(prmsProvider),teamCityXMLParser,new JIRAProcessExceptions(_logger));
            reporter.report(issueIds);
        } catch (URISyntaxException e) {
            assertTrue(false,"There should be no exception.");
        }
        //Assert
    }
    @ExtendWith(MockitoExtension.class)
    @org.junit.jupiter.api.Test
    void codeHelper(@Mock BuildProgressLogger _logger) throws FileNotFoundException  {

        //Arrange
//
//        //Default runnerParams, Avoid using credentials in the runnerParams.
//        //Store all params in params/CodeHelperParams and ignore this file in git.
//        JSONObject providerParameters = new JSONObject() {{
//            put("agent.ownPort","9090");
//            put("artefacts.paths","");
//            put("enableCommenting","true");
//            put("enableIssueTransitioning","true");
//            put("enableLinkToBuildPage","true");
//            put("enableSSLConnection","true");
//            put("jiraUser","your jira user");
//            put("jiraPassword","your jira password");
//            put("jiraServerUrl","https://jira.domain");
//            put("jiraWorkflow","SUCCESS:In Progress-Done,ToDo-In Progress;FAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;");
//            put("tcUser","your teamcity user");
//            put("tcPassword","your teamcity password");
//            put("tcServerUrl","https://teamcity.domain");
//            put("teamcity.build.checkoutDir","C:\\BuildAgent\\work\\b0f3dc02f4e91bb8");
//            put("teamcity.build.id","13001");
//            put("teamcity.build.workingDir","C:\\BuildAgent\\work\\b0f3dc02f4e91bb8");
//            put("teamcity.fail.exit.code","true");
//            put("teamcity.step.mode","default");
//            put("number","25"); //This's id of teamcity build.
//        }};

        File jsonTCParams = new File("test/java/name/kiszka/codehelper/params/CodeHelperParams.json");
        if(!jsonTCParams.exists()) {
            assertTrue(true);
        }
        String jsonString = new Scanner(jsonTCParams).useDelimiter("\\Z").next();
        JSONObject providerParameters = new JSONObject(jsonString);


        RunnerParamsProvider prmsProvider = new RunnerParamsProvider(providerParameters, _logger);
        prmsProvider.set("build.type.id", "KomKOD_sandbox"); //system.teamcity.buildType.id
        prmsProvider.set("buildName", "sandbox"); //to musi być prawdziwa nazwa projektu budjącego

        TeamCityXMLParser parser = new TeamCityXMLParser(prmsProvider);

        //Collection<String> issueIds = parser.getIssueKeys(); //getIssueKeys has to be invoked, there _buildData is set and it's necessary to use TeamCityXMLParser.
        ArrayList<String> issueIds = new ArrayList<String>();
        issueIds.add("TEST-322");
        //Act
        try {
            Reporter reporter = new Reporter(prmsProvider, new JIRAClient(prmsProvider),new JIRAWorkflow(prmsProvider),parser,new JIRAProcessExceptions(_logger));
            reporter.transitionIssue(issueIds);
        } catch (URISyntaxException e) {
            assertTrue(false,"There should be no exception.");
        }
        //Assert
    }
}
