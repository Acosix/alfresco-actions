/*
 * Copyright 2019 Acosix GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.acosix.alfresco.actions.repo.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.PropertyCheck;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.ISO8601DateFormatMethod;
import org.springframework.extensions.webscripts.ScriptableUtils;
import org.springframework.extensions.webscripts.UrlEncodeMethod;
import org.springframework.extensions.webscripts.json.JSONUtils;

/**
 * @author Axel Faust
 */
public class WebhookCallActionExecuter extends ActionExecuterAbstractBase implements InitializingBean
{

    public static final String NAME = "acosix-actions.webhookCall";

    public static final String PARAM_URL_TEMPLATE = "urlTemplate";

    public static final String PARAM_URL_TEMPLATE_ARGUMENTS = "urlTemplateArguments";

    public static final String PARAM_PAYLOAD_TEMPLATE = "payloadTemplate";

    public static final String PARAM_PAYLOAD_TEMPLATE_ARGUMENTS = "payloadTemplateArguments";

    public static final String PARAM_PAYLOAD_MIMETYPE = "payloadMimetype";

    public static final String PARAM_HEADERS = "headers";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookCallActionExecuter.class);

    private static final String TEMPLATE_TYPE_FREEMARKER = "freemarker";

    private static final Map<String, ContentType> DEFAULT_CONTENT_TYPES;

    static
    {
        final Map<String, ContentType> defaultContentTypes = new HashMap<>();
        defaultContentTypes.put(MimetypeMap.MIMETYPE_JSON, ContentType.APPLICATION_JSON);
        defaultContentTypes.put(MimetypeMap.MIMETYPE_XML, ContentType.APPLICATION_XML);
        defaultContentTypes.put(MimetypeMap.MIMETYPE_ATOM, ContentType.APPLICATION_ATOM_XML);
        defaultContentTypes.put(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), ContentType.APPLICATION_FORM_URLENCODED);
        DEFAULT_CONTENT_TYPES = Collections.unmodifiableMap(defaultContentTypes);
    }

    protected NodeService nodeService;

    protected TemplateService templateService;

    protected MimetypeService mimetypeService;

    protected SysAdminParams sysAdminParams;

    protected Repository repository;

    protected String userAgent;

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "nodeService", this.nodeService);
        PropertyCheck.mandatory(this, "templateService", this.templateService);
        PropertyCheck.mandatory(this, "mimetypeService", this.mimetypeService);
        PropertyCheck.mandatory(this, "repository", this.repository);
        PropertyCheck.mandatory(this, "sysAdminParams", this.sysAdminParams);
        PropertyCheck.mandatory(this, "userAgent", this.userAgent);
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(final NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param templateService
     *            the templateService to set
     */
    public void setTemplateService(final TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * @param mimetypeService
     *            the mimetypeService to set
     */
    public void setMimetypeService(final MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @param sysAdminParams
     *            the sysAdminParams to set
     */
    public void setSysAdminParams(final SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * @param repository
     *            the repository to set
     */
    public void setRepository(final Repository repository)
    {
        this.repository = repository;
    }

    /**
     * @param userAgent
     *            the userAgent to set
     */
    public void setUserAgent(final String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        final Map<String, Serializable> parameterValues = action.getParameterValues();

        final String urlTemplate = DefaultTypeConverter.INSTANCE.convert(String.class, parameterValues.get(PARAM_URL_TEMPLATE));
        final String urlTemplateArguments = DefaultTypeConverter.INSTANCE.convert(String.class,
                parameterValues.get(PARAM_URL_TEMPLATE_ARGUMENTS));

        final NodeRef payloadTemplate = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, parameterValues.get(PARAM_PAYLOAD_TEMPLATE));
        final String payloadTemplateArguments = DefaultTypeConverter.INSTANCE.convert(String.class,
                parameterValues.get(PARAM_PAYLOAD_TEMPLATE_ARGUMENTS));
        final String payloadMimetype = DefaultTypeConverter.INSTANCE.convert(String.class, parameterValues.get(PARAM_PAYLOAD_MIMETYPE));

        final String headers = DefaultTypeConverter.INSTANCE.convert(String.class, parameterValues.get(PARAM_HEADERS));

        if (payloadTemplate == null)
        {
            throw new IllegalArgumentException(PARAM_PAYLOAD_TEMPLATE + " must be provided as an action parameter");
        }

        if (urlTemplate == null || urlTemplate.trim().isEmpty())
        {
            throw new IllegalArgumentException(PARAM_URL_TEMPLATE + " must be provided as an action parameter");
        }
        final Map<String, Object> defaultModel = this.buildDefaultModel();

        final String url = this.generateURL(actionedUponNodeRef, urlTemplate, urlTemplateArguments, defaultModel);

        LOGGER.debug("Preparing call to webhook at URL {}", url);

        final String payload = this.generatePayload(actionedUponNodeRef, payloadTemplate, payloadTemplateArguments, defaultModel);

        this.callWebhook(payloadTemplate, payloadMimetype, headers, url, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addParameterDefinitions(final List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_URL_TEMPLATE, DataTypeDefinition.TEXT, true,
                this.getParamDisplayLabel(PARAM_URL_TEMPLATE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_URL_TEMPLATE_ARGUMENTS, DataTypeDefinition.TEXT, false,
                this.getParamDisplayLabel(PARAM_URL_TEMPLATE_ARGUMENTS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAYLOAD_TEMPLATE, DataTypeDefinition.NODE_REF, true,
                this.getParamDisplayLabel(PARAM_PAYLOAD_TEMPLATE), false, NAME + ".templates"));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAYLOAD_TEMPLATE_ARGUMENTS, DataTypeDefinition.TEXT, false,
                this.getParamDisplayLabel(PARAM_PAYLOAD_TEMPLATE_ARGUMENTS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_PAYLOAD_MIMETYPE, DataTypeDefinition.TEXT, false,
                this.getParamDisplayLabel(PARAM_PAYLOAD_MIMETYPE), false, NAME + ".allowedMimetypes"));
        paramList.add(new ParameterDefinitionImpl(PARAM_HEADERS, DataTypeDefinition.TEXT, false, this.getParamDisplayLabel(PARAM_HEADERS)));
    }

    protected Map<String, Object> buildDefaultModel()
    {
        final NodeRef person = this.repository.getPerson();
        final Map<String, Object> defaultModel = this.templateService.buildDefaultModel(person, this.repository.getCompanyHome(),
                person != null ? this.repository.getUserHome(person) : null, null, null);
        defaultModel.put("sysAdminParams", this.sysAdminParams);
        defaultModel.put("encodeuri", new UrlEncodeMethod());
        defaultModel.put("xmldate", new ISO8601DateFormatMethod());
        defaultModel.put("jsonUtils", new JSONUtils());
        defaultModel.put("stringUtils", new ScriptableUtils());
        return defaultModel;
    }

    protected String generateURL(final NodeRef actionedUponNodeRef, final String urlTemplate, final String urlTemplateArguments,
            final Map<String, Object> defaultModel)
    {
        LOGGER.debug("Processing URL template {}", urlTemplate);
        final Map<String, Object> urlTemplateModel = new HashMap<>(defaultModel);
        if (actionedUponNodeRef != null)
        {
            urlTemplateModel.put("document", actionedUponNodeRef);
        }
        processTemplateModelArguments(PARAM_URL_TEMPLATE_ARGUMENTS, urlTemplateArguments, urlTemplateModel);
        final String url = this.templateService.processTemplateString(TEMPLATE_TYPE_FREEMARKER, urlTemplate, urlTemplateModel);
        return url;
    }

    protected String generatePayload(final NodeRef actionedUponNodeRef, final NodeRef payloadTemplate,
            final String payloadTemplateArguments, final Map<String, Object> defaultModel)
    {
        LOGGER.debug("Processing payload template {}", payloadTemplate);
        final Map<String, Object> payloadTemplateModel = new HashMap<>(defaultModel);
        if (actionedUponNodeRef != null)
        {
            payloadTemplateModel.put("document", actionedUponNodeRef);
        }
        processTemplateModelArguments(PARAM_PAYLOAD_TEMPLATE_ARGUMENTS, payloadTemplateArguments, payloadTemplateModel);
        final String payload = this.templateService.processTemplate(TEMPLATE_TYPE_FREEMARKER, payloadTemplate.toString(),
                payloadTemplateModel);
        LOGGER.trace("Generated webhook payload: {}", payload);
        return payload;
    }

    protected void setPostPayload(final NodeRef payloadTemplate, final String payloadMimetype, final String payload, final HttpPost post)
    {
        ContentType contentType = null;
        if (payloadMimetype != null && !payloadMimetype.isEmpty())
        {
            contentType = DEFAULT_CONTENT_TYPES.get(payloadMimetype);
        }
        if (contentType == null)
        {
            final String templateName = DefaultTypeConverter.INSTANCE.convert(String.class,
                    this.nodeService.getProperty(payloadTemplate, ContentModel.PROP_NAME));
            // can only determine mimetype from template name if it contains a secondary file extension before .ftl
            if (templateName.matches("^[^\\.]+\\.[^\\.]+\\.[fF][tT][lL]$"))
            {
                final String baseTemplateName = templateName.substring(0, templateName.length() - 4);
                final String guessedMimetype = this.mimetypeService.guessMimetype(baseTemplateName);
                contentType = DEFAULT_CONTENT_TYPES.get(guessedMimetype);
                if (contentType == null && !MimetypeMap.MIMETYPE_BINARY.equals(guessedMimetype))
                {
                    contentType = ContentType.create(guessedMimetype, StandardCharsets.UTF_8);
                }
            }
        }
        if (contentType == null)
        {
            contentType = ContentType.APPLICATION_OCTET_STREAM;
        }
        post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    }

    protected void callWebhook(final NodeRef payloadTemplate, final String payloadMimetype, final String headers, final String url,
            final String payload)
    {
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(url);

        post.addHeader("User-Agent", this.userAgent);
        this.setPostPayload(payloadTemplate, payloadMimetype, payload, post);
        processPostHeaders(PARAM_HEADERS, headers, post);

        try
        {
            LOGGER.debug("Performing webhook call to URL {}", url);
            final HttpResponse response = client.execute(post);
            final StatusLine statusLine = response.getStatusLine();
            LOGGER.debug("Webhook call to URL {} responded with status {}", url, statusLine);

            if (LOGGER.isTraceEnabled())
            {
                final HttpEntity entity = response.getEntity();
                final Header contentType = entity.getContentType();
                final Header contentEncoding = entity.getContentEncoding();
                final String contentTypeVal = contentType != null ? contentType.getValue() : MimetypeMap.MIMETYPE_BINARY;
                final String contentEncodingVal = contentEncoding != null ? contentEncoding.getValue() : StandardCharsets.UTF_8.name();

                if (contentTypeVal.startsWith(MimetypeMap.PREFIX_TEXT) || contentTypeVal.startsWith(MimetypeMap.MIMETYPE_JSON))
                {
                    final StringBuilder responseBodyBuilder = new StringBuilder((int) entity.getContentLength());
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(entity.getContent(), contentEncodingVal)))
                    {
                        String line;
                        while ((line = r.readLine()) != null)
                        {
                            if (responseBodyBuilder.length() > 0)
                            {
                                responseBodyBuilder.append('\n');
                            }
                            responseBodyBuilder.append(line);
                        }
                    }
                    LOGGER.trace("Webhook call to URL {} responded with response messsage: {}", url, responseBodyBuilder);
                }
            }

            final int statusCode = statusLine.getStatusCode();

            if (statusCode >= 400)
            {
                LOGGER.warn("Webhook call failed with HTTP error {} and status message {}", statusCode, statusLine.getReasonPhrase());
                throw new ActionServiceException(
                        "Webhook responded with HTTP error code " + statusCode + ", status message: " + statusLine.getReasonPhrase());
            }
        }
        catch (final IOException ex)
        {
            LOGGER.warn("Webhook call failed", ex);
            throw new ActionServiceException("Error executing webhook call", ex);
        }
    }

    /**
     * Processes the arguments for a specific template model from a provided, potential multi-line parameter containing key-value pairs.
     *
     * @param parameterName
     *            the arguments parameter name
     * @param parameterValue
     *            the arguments parameter value
     * @param model
     *            the template model to fill
     */
    protected static void processTemplateModelArguments(final String parameterName, final String parameterValue,
            final Map<String, Object> model)
    {
        if (parameterValue != null && !parameterValue.trim().isEmpty())
        {
            final String[] lines = parameterValue.split("\\n");
            for (final String line : lines)
            {
                LOGGER.debug("Processing template argument line {}", line);
                final String[] keyVals = line.split("=");
                if (keyVals.length >= 2)
                {
                    final String key = keyVals[0].trim();
                    final String value = line.substring(line.indexOf('=', key.length()) + 1).trim();
                    model.put(key, value);
                }
                else
                {
                    throw new IllegalArgumentException(
                            parameterName + " must be provided as multiple text lines of =-separated keys + values");
                }
            }
        }
    }

    /**
     * Processes the headers for a POST operation from a provided, potential multi-line parameter containing key-value pairs.
     *
     * @param parameterName
     *            the arguments parameter name
     * @param parameterValue
     *            the arguments parameter value
     * @param post
     *            the post operation to parameterise with headers
     */
    protected static void processPostHeaders(final String parameterName, final String parameterValue, final HttpPost post)
    {
        if (parameterValue != null && !parameterValue.trim().isEmpty())
        {
            final String[] lines = parameterValue.split("\\n");
            for (final String line : lines)
            {
                LOGGER.debug("Processing headers line {}", line);
                final String[] keyVals = line.split("=");
                if (keyVals.length >= 2)
                {
                    final String key = keyVals[0].trim();
                    final String value = line.substring(line.indexOf('=', key.length()) + 1).trim();
                    post.addHeader(key, value);
                }
                else
                {
                    throw new IllegalArgumentException(
                            parameterName + " must be provided as multiple text lines of =-separated keys + values");
                }
            }
        }
    }
}
