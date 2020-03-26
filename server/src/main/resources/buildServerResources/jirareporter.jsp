<%@ include file="/include.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%--<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>--%>
<%--<jsp:useBean id="xsollaProp" type="xsollacustomrunner.server.XsollaBean"/>--%>

<c:set var="title" value="JIRA Reporter" scope="request"/>


<l:settingsGroup title="JIRA configuration">
    <tr>
      <th><label for="jiraServerUrl">Server URL:</label></th>
      <td><props:textProperty name="jiraServerUrl" className="longField"/></td>
    </tr>
    <tr>
      <th><label for="jiraUser">User:</label></th>
      <td><props:textProperty name="jiraUser" /></td>
    </tr>
    <tr>
      <th><label for="jiraPassword">Password:</label></th>
      <td><props:passwordProperty name="jiraPassword"/></td>
    </tr>
    <tr>
      <th>Security:</th>
      <td>
        <props:checkboxProperty name="jiraWindowsAuth"/>
        <label for="jiraWindowsAuth">NTLM authentication</label>
        <br>
        <props:checkboxProperty name="enableSSLConnection"/>
        <label for="enableSSLConnection">SSL connection</label>
      </td>
    </tr>
    <!--<tr>
        <span>Enable issue progressing:</span>
        <props:checkboxProperty name="enableIssueProgressing"/>
        <br>
        <div id="jira.workflow">
            <props:multilineProperty name="jiraWorkflow" rows="5" cols="58" linkTitle="Enter your JIRA workflow for issue progressing"/>
        </div>
    </tr>-->
    <tr>
      <th>Reporting settings:</th>
      <td>
        <props:checkboxProperty name="enableLinkToBuildPage"/>
        <label for="enableLinkToBuildPage">Make link to TeamCity build page on success</label>
        <br>
        <props:checkboxProperty name="enableCommenting"/>
        <label for="enableCommenting">Post comment to Jira issue</label>
        <br>
        <props:checkboxProperty name="enableTemplateComment"/>
        <label for="enableTemplateComment">Enable comment template for Jira</label>
        <div id="templateJiraComment">
            <props:multilineProperty name="templateComment" rows="5" cols="58" linkTitle="Comment template:"/>
        </div>
      </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="TeamCity Configuration">
    <tr>
      <th><label for="tcServerUrl">Server URL:</label></th>
      <td><props:textProperty name="tcServerUrl" className="longField"/></td>
    </tr>
    <tr>
      <th><label for="tcUser">User:</label></th>
      <td><props:textProperty name="tcUser" /></td>
    </tr>
    <tr>
      <th><label for="tcPassword">Password:</label></th>
      <td><props:passwordProperty name="tcPassword"/></td>
    </tr>
    <tr>
      <th>Security:</th>
      <td>
        <props:checkboxProperty name="tcWindowsAuth"/>
        <label for="tcWindowsAuth">NTLM authentication</label>
      </td>
    </tr>
</l:settingsGroup>

<script type="text/javascript">
    /*var checkBox = jQuery('#enableIssueProgressing');
    checkBox.change(function(){
        if(jQuery(this).prop("checked")){
            BS.Util.show('jira.workflow');
        }
        else{
            BS.Util.hide('jira.workflow');
        }
        BS.VisibilityHandlers.updateVisibility('mainContent');
    });
    if(checkBox.prop("checked")){
        BS.Util.show('jira.workflow');
    }
    else{
        BS.Util.hide('jira.workflow');
    }*/
    var tplComment = jQuery('#enableTemplateComment');
    tplComment.change(function(){
        if(jQuery(this).prop("checked")){
            BS.Util.show('templateJiraComment');
        }
        else{
            BS.Util.hide('templateJiraComment');
        }
        BS.VisibilityHandlers.updateVisibility('mainContent');
    });
    if(tplComment.prop("checked")){
        BS.Util.show('templateJiraComment');
    }
    else{
        BS.Util.hide('templateJiraComment');
    }
    BS.VisibilityHandlers.updateVisibility('mainContent');
</script>