/*
 * REST Web Service for Reuters News Corpus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package qatask;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import net.sf.saxon.xqj.SaxonXQDataSource;

/**
 * REST Web Service.
 *
 * @author Martin Chlumecký martin.chlumecky@gmail.com
 */
@Path("")
public class LewisService {

    /**
     * Resource file of David Lewis' distribution. Advised SGM files in the data
     * set are not valid. It was used the XML file from
     * https://github.com/haseebr/irengine/tree/master/reuters21578-xml with the
     * valid XML format.
     */
    static final String XML_FILE = "reut2-003.xml";

    /**
     * XQueryService for evaluation of HTTP GET messages.
     */
    private final XQueryService xQueryService;

    /**
     * XML result for the GET method.
     */
    private final StringBuilder sbResults;

    /**
     * Creates a new instance of LewisService.
     *
     * @throws javax.xml.xquery.XQException if the connection to XQuery is not
     * successful.
     */
    public LewisService() throws XQException {
        sbResults = new StringBuilder();
        xQueryService = new XQueryService(XML_FILE);
    }

    /**
     * Process HTML GET message with parameters. This method processes a GET
     * message from WebServer.
     *
     * @param id determines the identifier where content is searched.
     * @param content defines a searched text.
     * @return XML which meets criteria defined in method parameters.
     * @throws XQException if XQuery is not executed successfully.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public final String getXml(
            @DefaultValue("") @QueryParam("id") String id,
            @DefaultValue("") @QueryParam("content") String content)
            throws XQException {
        // create XQuery according to GET parameters
        String xquery = xQueryService.getLewisXQuery(id, content);
        // clean sbResults and put the root XML element <LEWIS>
        sbResults.setLength(0);
        sbResults.append("<LEWIS>\n");
        // evaluate XQuere
        XQPreparedExpression exp = xQueryService.getConnection().prepareExpression(xquery);
        XQResultSequence result = exp.executeQuery();
        // convert XQuere results to String and append to the returned String
        while (result.next()) {
            sbResults.append(result.getItemAsString(null));
        }
        // append ending tag of the root XML element <LEWIS>
        sbResults.append("</LEWIS>");
        // return XQuere result as XML
        return sbResults.toString();
    }
}

/**
 * XQuery Service. This class mediates questions between HTTP GET messages and
 * XQuery. The class transforms HTPP parameters into the XQuery question for
 * Lewis' XML files.
 */
class XQueryService {

    /**
     * Data source for XQuery.
     */
    private static XQDataSource xQueryDataSrc;
    /**
     * Connection for XQuery.
     */
    private static XQConnection xQueryConnection;

    /**
     * Path to XML file for XQuery.
     */
    private final String xmlFilePath;

    /**
     * For XQuery statement. It is a selector for the root node <LEWIS> where
     * each record is represented by <REUTERS> tag. The "doc" is a placeholder
     * of a source XML file.
     */
    static final String XQUERY_FOR = "for $x in doc(\'%s\')/LEWIS/REUTERS";
    /**
     * Where XQuery statement. This statement defines searching for a text in
     * all sub-nodes in a <REUTERS> node. The searching text is defined as a
     * placeholder.
     */
    static final String XQUERY_CONTAINS_ALL = "contains(string($x), \'%s\')";
    /**
     * Where XQuery statement. This statement defines searching for a text in a
     * specific sub-node in a <REUTERS> node. The searching text and the
     * specific node are defined as placeholders.
     */
    static final String XQUERY_CONTAINS_NODE = "contains(string($x//%s), \'%s\')";
    /**
     * Where XQuery statement. This statement defines searching for a text in a
     * specific attribute in all node of a <REUTERS> node. The searching text
     * and specific attribute are defined as placeholders.
     */
    static final String XQUERY_CONTAINS_ATTRIBUTE = "contains(string($x//@%s), \'%s\')";

    /**
     * Create XQueryService. Initialization XQuery Service.
     *
     * @param xmlFile is a path to the XML file for XQuery questions.
     * @throws XQException if the XQuery initialization failed.
     */
    XQueryService(final String xmlFile) throws XQException {
        this.xmlFilePath = xmlFile;
        xQueryDataSrc = new SaxonXQDataSource();
        xQueryConnection = xQueryDataSrc.getConnection();
    }

    /**
     * Getter for XQDataSource. This method returns the XQuery Data Source.
     *
     * @return instance of XQuery Data Source
     */
    public XQDataSource getxQueryDataSrc() {
        return xQueryDataSrc;
    }

    /**
     * Getter for XQConnection. This method returns the XQuery Connection.
     *
     * @return instance of XQuery Connection
     */
    public XQConnection getConnection() {
        return xQueryConnection;
    }

    /**
     * Creation of XQuery for David Lewis' distribution. The purpose of this
     * method is assembling XQuery for an XML file defining Reuters News Corpus.
     *
     * If only "content" is defined, XQuery performs full-text searching. If
     * "content" and "identifier" are defined, XQuery searches in all nodes and
     * attributes. If no parameter is defined, the method returns all <REUTERS>
     * of the xmlFile.
     *
     *
     * @param identifier determines the name of a node or attribute which is
     * searched.
     * @param content defines a text which is searched.
     * @return XQuery results in a valid XML form.
     */
    public String getLewisXQuery(final String identifier, final String content) {
        StringBuilder sb = new StringBuilder();
        // append placeholder "For XQuery" statement
        sb.append(String.format(XQUERY_FOR, xmlFilePath));

        // append placeholder "Where XQuery" statement for full-text searching
        if (identifier.length() == 0 && content.length() != 0) {
            sb.append(" where ");
            sb.append(String.format(XQUERY_CONTAINS_ALL, content));
        }
        // append placeholder "Where XQuery" statement for specific node/attribute searching
        if (identifier.length() != 0 && content.length() != 0) {
            sb.append(" where ");
            sb.append(String.format(XQUERY_CONTAINS_NODE, identifier, content));
            sb.append(" or ");
            sb.append(String.format(XQUERY_CONTAINS_ATTRIBUTE, identifier, content));
        }
        // append return statement
        sb.append(" return $x");
        return sb.toString();
    }
}
