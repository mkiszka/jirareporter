package name.kiszka.jirareporter.jira;

import com.amirov.jirareporter.jira.exceptions.JIRAProcessWorkflowException;
import com.amirov.jirareporter.jira.exceptions.JIRATransitionParamsSetException;
import com.atlassian.jira.rest.client.RestClientException;
import com.sun.jersey.api.client.UniformInterfaceException;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;

public class JIRAProcessExceptions {

    private BuildProgressLogger _logger;

    public JIRAProcessExceptions(BuildProgressLogger logger) {
        _logger = logger;
    }

    public void process(RestClientException rse, String issueId) {
        Throwable t = rse.getCause();
        if( t instanceof UniformInterfaceException) {
            UniformInterfaceException uie = (UniformInterfaceException)t;
            _logger.message(uie.getMessage());
            if(uie.getMessage().contains("404")) {
                //Issue not found
                _logger.message(issueId + " not found.");
                //TODO BuildProblemData
                //BuildProblemData a = new BuildProblemData();
            }
            if(uie.getMessage().contains("401")) {
                //Authorization failed
                throw rse;
            }
        }
        _logger.message(rse.getMessage());
    }
    public void process(JIRATransitionParamsSetException e) {
        e.printStackTrace(); //TODO
        _logger.message("Wrong transition parameters.");
        _logger.message("Transitions failed!");
    }

    public void process(JIRAProcessWorkflowException e) {
        e.printStackTrace(); //TODO
        _logger.message("The transition definition is wrong.");
        _logger.message("Transitions failed!");
    }
}
