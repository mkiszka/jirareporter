package com.amirov.jirareporter.teamcity;

import com.amirov.jirareporter.RunnerParamsProvider;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;

public class TeamCityXMLParser implements IBuildInfo
{
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    //private static final ObjectMapper mapper = new ObjectMapper();

    private final BuildProgressLogger _logger;
    private final RunnerParamsProvider _prmsProvider;
    private final String _tcBaseUrl;
    private final String _buildTypeId;
    private NamedNodeMap _buildData;

    public TeamCityXMLParser(RunnerParamsProvider prmsProvider)
    {
        _logger = prmsProvider.getLogger();
        _prmsProvider = prmsProvider;
        _tcBaseUrl = prmsProvider.getTCServerUrl();
        _buildTypeId = prmsProvider.getBuildTypeId();

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        Authenticator.setDefault(new Authenticator()
        {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                _logger.message("Feeding username and password for " + getRequestingScheme());
                return (new PasswordAuthentication(_prmsProvider.getTCUser(), _prmsProvider.getTCPassword().toCharArray()));
            }
        });
    }

    public Collection<String> getIssueKeys()
    {
        // running:true,
        NodeList buildNodes = loadXmlNodeList("/app/rest/builds?locator=branch:default:any,running:any,buildType:" + _buildTypeId, "build");
        if (buildNodes.getLength() == 0)
            return null;

        ArrayList<String> issueIds = new ArrayList<>();
        // Collect all builds till last success
        _buildData = buildNodes.item(0).getAttributes();
        for (int i = 0; i < buildNodes.getLength(); i++)
        {
            NamedNodeMap attrs = buildNodes.item(i).getAttributes();
            if (i > 0 && SUCCESS.equalsIgnoreCase(attrs.getNamedItem("status").getNodeValue()))
                break;

            String buildId = attrs.getNamedItem("id").getNodeValue();
            String buildNum = attrs.getNamedItem("number").getNodeValue();

            NodeList issueList = loadXmlNodeList("/app/rest/builds/id:" + buildId + "/relatedIssues", "issue");
            _logger.message("Found " + issueList.getLength() + " related issues for build " + buildNum);
            for(int x = 0; x < issueList.getLength(); x++)
            {
                String issueKey = issueList.item(x).getAttributes().getNamedItem("id").getNodeValue();
                if (!issueIds.contains(issueKey))
                    issueIds.add(issueKey);
            }
        }
        return issueIds;
    }

    public String getBuildId(){ return getBuildAttribute("id"); }

    public String getBuildStatus(){ return getBuildAttribute("status"); }

    public String getBuildNumber () { return getBuildAttribute("number"); }

    public String getBuildName() { return _prmsProvider.getBuildName(); }

    public String getBuildHref(){ return getBuildAttribute("href"); }

    public String getWebUrl(){ return getBuildAttribute("webUrl"); }

    public String getBuildTestsStatus()
    {
        return loadXmlNodeList(getBuildHref(), "statusText").item(0).getTextContent();
    }

    public String getArtifactHref()
    {
        return loadXmlNodeList(getBuildHref(), "artifacts").item(0).getAttributes().getNamedItem("href").getNodeValue();
    }

    public String getArtifactName()
    {
        return loadXmlNodeList(getArtifactHref(), "file").item(0).getAttributes().getNamedItem("name").getNodeValue();
    }

    public String buildCommentText()
    {
        String statusStyle = FAILURE.equalsIgnoreCase(getBuildStatus()) ? "#DE350B" : "#00875A";
        if(_prmsProvider.isCommentTemplateEnabled())
            return _prmsProvider.getTemplateComment()
                    .replace("${build.id}", getBuildId())
                    .replace("${build.type}", _buildTypeId)
                    .replace("${build.name}", _prmsProvider.getBuildName())
                    .replace("${build.number}", getBuildNumber())
                    .replace("${build.status}", getBuildStatus())
                    .replace("${build.status.style}", statusStyle)
                    .replace("${build.weburl}", getWebUrl())
                    .replace("${tests.results}", getBuildTestsStatus());
        else
        {
            return String.format("%s [#%s|%s] Build result: {color:%s}*%s*{color}"
                 , _prmsProvider.getBuildName(), getBuildNumber(), getWebUrl(), statusStyle, getBuildStatus());
        }
    }

    private NodeList loadXmlNodeList(String xmlUrl, String nodeName)
    {
        try
        {
            if (!_prmsProvider.getTCWindowsAuth())
                xmlUrl = "/httpAuth" + xmlUrl;

            URL url = new URL(_tcBaseUrl + xmlUrl);
            URLConnection uc = url.openConnection();
            uc.connect();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(uc.getInputStream()));
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(nodeName);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private String getBuildAttribute(String attribute)
    {
        return _buildData.getNamedItem(attribute).getNodeValue();
    }
}
