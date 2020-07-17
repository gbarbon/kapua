/*******************************************************************************
 * Copyright (c) 2017, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.elasticsearch.client.rest;

import com.codahale.metrics.Counter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpHeaders;
import org.apache.http.ParseException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.kapua.commons.metric.MetricServiceFactory;
import org.eclipse.kapua.commons.metric.MetricsService;
import org.eclipse.kapua.commons.util.RandomUtils;
import org.eclipse.kapua.service.elasticsearch.client.AbstractDatastoreClient;
import org.eclipse.kapua.service.elasticsearch.client.ClientProvider;
import org.eclipse.kapua.service.elasticsearch.client.ModelContext;
import org.eclipse.kapua.service.elasticsearch.client.QueryConverter;
import org.eclipse.kapua.service.elasticsearch.client.SchemaKeys;
import org.eclipse.kapua.service.elasticsearch.client.exception.ClientActionResponseException;
import org.eclipse.kapua.service.elasticsearch.client.exception.ClientCommunicationException;
import org.eclipse.kapua.service.elasticsearch.client.exception.ClientErrorCodes;
import org.eclipse.kapua.service.elasticsearch.client.exception.ClientException;
import org.eclipse.kapua.service.elasticsearch.client.exception.ClientInternalError;
import org.eclipse.kapua.service.elasticsearch.client.model.BulkUpdateRequest;
import org.eclipse.kapua.service.elasticsearch.client.model.BulkUpdateResponse;
import org.eclipse.kapua.service.elasticsearch.client.model.IndexRequest;
import org.eclipse.kapua.service.elasticsearch.client.model.IndexResponse;
import org.eclipse.kapua.service.elasticsearch.client.model.InsertRequest;
import org.eclipse.kapua.service.elasticsearch.client.model.InsertResponse;
import org.eclipse.kapua.service.elasticsearch.client.model.ResultList;
import org.eclipse.kapua.service.elasticsearch.client.model.TypeDescriptor;
import org.eclipse.kapua.service.elasticsearch.client.model.UpdateRequest;
import org.eclipse.kapua.service.elasticsearch.client.model.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Client implementation based on Elasticsearch rest client.
 * <p>
 * The Elasticsearch client provider is instantiated as singleton.
 *
 * @since 1.0.0
 */
public class RestDatastoreClient extends AbstractDatastoreClient<RestClient> {

    private static final Logger logger = LoggerFactory.getLogger(RestDatastoreClient.class);

    private static final String GET_ACTION = "GET";
    private static final String DELETE_ACTION = "DELETE";
    private static final String POST_ACTION = "POST";
    private static final String PUT_ACTION = "PUT";
    private static final String HEAD_ACTION = "HEAD";

    private static final String INDEX_ALL = "ALL";

    private static final String KEY_DOC = "doc";
    private static final String KEY_DOC_AS_UPSERT = "doc_as_upsert";
    private static final String KEY_DOC_ID = "_id";
    private static final String KEY_DOC_INDEX = "_index";
    private static final String KEY_DOC_TYPE = "_type";

    private static final String KEY_ITEMS = "items";
    private static final String KEY_RESULT = "result";
    private static final String KEY_STATUS = "status";
    private static final String KEY_UPDATE = "update";

    private static final String KEY_HITS = "hits";
    private static final String KEY_TOTAL = "total";

    private static final ObjectMapper MAPPER;

    private static final String MSG_EMPTY_ERROR = "Empty error message";

    private static final String CLIENT_CANNOT_PARSE_INDEX_RESPONSE_ERROR_MSG = "Cannot convert the indexes list";
    private static final String CLIENT_HITS_MAX_VALUE_EXCEEDED = "Total hits exceeds integer max value";
    private static final String CLIENT_COMMUNICATION_TIMEOUT_MSG = "Elasticsearch client timeout";
    private static final String CLIENT_GENERIC_ERROR_MSG = "Generic client error";
    private static final String QUERY_CONVERTED_QUERY = "Query - converted query: '{}'";
    private static final String COUNT_CONVERTED_QUERY = "Count - converted query: '{}'";

    private static final Random RANDOM = RandomUtils.getInstance();
    private static final int MAX_RETRY_ATTEMPT = ClientSettings.getInstance().getInt(ClientSettingsKey.ELASTICSEARCH_REST_TIMEOUT_MAX_RETRY, 3);
    private static final long MAX_RETRY_WAIT_TIME = ClientSettings.getInstance().getLong(ClientSettingsKey.ELASTICSEARCH_REST_TIMEOUT_MAX_WAIT, 2500);

    private static RestDatastoreClient instance;

    private Counter restCallRuntimeExecCount;
    private Counter timeoutRetryCount;
    private Counter timeoutRetryLimitReachedCount;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        instance = new RestDatastoreClient();
    }

    /**
     * Get the singleton {@link RestDatastoreClient} instance.
     *
     * @return The singleton {@link RestDatastoreClient} instance
     * @since 1.0.0
     */
    public static RestDatastoreClient getInstance() {
        return instance;
    }

    /**
     * Initialize the client provider ({@link ClientProvider}) as singleton.
     *
     * @since 1.0.0
     */
    private RestDatastoreClient() {
        super("rest");
        MetricsService metricService = MetricServiceFactory.getInstance();
        restCallRuntimeExecCount = metricService.getCounter(DatastoreRestClientMetrics.METRIC_MODULE_NAME, DatastoreRestClientMetrics.METRIC_COMPONENT_NAME, DatastoreRestClientMetrics.METRIC_RUNTIME_EXEC, DatastoreRestClientMetrics.METRIC_COUNT);
        timeoutRetryCount = metricService.getCounter(DatastoreRestClientMetrics.METRIC_MODULE_NAME, DatastoreRestClientMetrics.METRIC_COMPONENT_NAME, DatastoreRestClientMetrics.METRIC_TIMEOUT_RETRY, DatastoreRestClientMetrics.METRIC_COUNT);
        timeoutRetryLimitReachedCount = metricService.getCounter(DatastoreRestClientMetrics.METRIC_MODULE_NAME, DatastoreRestClientMetrics.METRIC_COMPONENT_NAME, DatastoreRestClientMetrics.TIMEOUT_RETRY_LIMIT_REACHED, DatastoreRestClientMetrics.METRIC_COUNT);
    }

    @Override
    public ClientProvider<RestClient> getNewInstance() {
        return EsRestClientProvider.init();
    }

    @Override
    public InsertResponse insert(InsertRequest insertRequest) throws ClientException {
        RestClient client = getClient();
        Map<String, Object> storableMap = modelContext.marshal(insertRequest.getStorable());
        logger.debug("Insert - converted object: '{}'", storableMap);
        String json;
        try {
            json = MAPPER.writeValueAsString(storableMap);
        } catch (JsonProcessingException e) {
            throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
        }

        Response insertResponse = restCallTimeoutHandler(() -> client.performRequest(
                POST_ACTION,
                getInsertTypePath(insertRequest),
                Collections.emptyMap(),
                EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), insertRequest.getTypeDescriptor().getIndex(), "INSERT");

        if (isRequestSuccessful(insertResponse)) {
            JsonNode responseNode;
            try {
                responseNode = MAPPER.readTree(EntityUtils.toString(insertResponse.getEntity()));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }

            String id = responseNode.get(KEY_DOC_ID).asText();
            String index = responseNode.get(KEY_DOC_INDEX).asText();
            String type = responseNode.get(KEY_DOC_TYPE).asText();
            return new InsertResponse(id, new TypeDescriptor(index, type));
        } else {
            throw buildExceptionFromUnsuccessfulResponse("Insert", insertResponse);
        }
    }

    @Override
    public UpdateResponse upsert(UpdateRequest updateRequest) throws ClientException {
        RestClient client = getClient();
        Map<String, Object> storableMap = modelContext.marshal(updateRequest.getStorable());
        Map<String, Object> updateRequestMap = new HashMap<>();
        updateRequestMap.put(KEY_DOC, storableMap);
        updateRequestMap.put(KEY_DOC_AS_UPSERT, true);
        logger.debug("Upsert - converted object: '{}'", updateRequestMap);
        String json;
        try {
            json = MAPPER.writeValueAsString(updateRequestMap);
        } catch (JsonProcessingException e) {
            throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
        }

        Response updateResponse = restCallTimeoutHandler(() -> client.performRequest(
                POST_ACTION,
                getUpsertPath(updateRequest.getTypeDescriptor(), updateRequest.getId()),
                Collections.emptyMap(),
                EntityBuilder.create().setText(json).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), updateRequest.getTypeDescriptor().getIndex(), "UPSERT");

        if (isRequestSuccessful(updateResponse)) {
            JsonNode responseNode;
            try {
                responseNode = MAPPER.readTree(EntityUtils.toString(updateResponse.getEntity()));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }
            String id = responseNode.get(KEY_DOC_ID).asText();
            String index = responseNode.get(KEY_DOC_INDEX).asText();
            String type = responseNode.get(KEY_DOC_TYPE).asText();
            return new UpdateResponse(id, new TypeDescriptor(index, type));
        } else {
            throw buildExceptionFromUnsuccessfulResponse("Update", updateResponse);
        }
    }

    @Override
    public BulkUpdateResponse upsert(BulkUpdateRequest bulkUpdateRequest) throws ClientException {
        RestClient client = getClient();
        StringBuilder bulkOperation = new StringBuilder();
        for (UpdateRequest upsertRequest : bulkUpdateRequest.getRequest()) {
            Map<String, Object> storableMap = modelContext.marshal(upsertRequest.getStorable());
            bulkOperation.append("{ \"update\": {\"_id\": \"")
                    .append(upsertRequest.getId())
                    .append("\", \"_type\": \"")
                    .append(upsertRequest.getTypeDescriptor().getType())
                    .append("\", \"_index\": \"")
                    .append(upsertRequest.getTypeDescriptor().getIndex())
                    .append("\"}\n");

            bulkOperation.append("{ \"doc\": ");
            try {

                bulkOperation.append(MAPPER.writeValueAsString(storableMap));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }
            bulkOperation.append(", \"doc_as_upsert\": true }\n");
        }
        Response updateResponse = restCallTimeoutHandler(() -> client.performRequest(
                POST_ACTION,
                getBulkPath(),
                Collections.emptyMap(),
                EntityBuilder.create().setText(bulkOperation.toString()).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), "multi-index", "UPSERT BULK");
        if (isRequestSuccessful(updateResponse)) {
            BulkUpdateResponse bulkResponse = new BulkUpdateResponse();
            JsonNode responseNode;
            try {
                responseNode = MAPPER.readTree(EntityUtils.toString(updateResponse.getEntity()));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }

            ArrayNode items = (ArrayNode) responseNode.get(KEY_ITEMS);
            for (JsonNode item : items) {
                JsonNode jsonNode = item.get(KEY_UPDATE);
                if (jsonNode != null) {
                    JsonNode idNode = jsonNode.get(KEY_DOC_ID);
                    String metricId = null;
                    if (idNode != null) {
                        metricId = idNode.asText();
                    }
                    String indexName = jsonNode.get(KEY_DOC_INDEX).asText();
                    String typeName = jsonNode.get(KEY_DOC_TYPE).asText();
                    int responseCode = jsonNode.get(KEY_STATUS).asInt();
                    if (!isRequestSuccessful(responseCode)) {
                        JsonNode failureNode = jsonNode.get(KEY_RESULT);
                        String failureMessage = MSG_EMPTY_ERROR;
                        if (failureNode != null) {
                            failureMessage = failureNode.asText();
                        }
                        bulkResponse.add(new UpdateResponse(metricId, new TypeDescriptor(indexName, typeName), failureMessage));
                        logger.info("Upsert failed [{}, {}, {}]", indexName, typeName, failureMessage);
                        continue;
                    }
                    bulkResponse.add(new UpdateResponse(metricId, new TypeDescriptor(indexName, typeName)));
                    logger.debug("Upsert on channel metric successfully executed [{}.{}, {}]", indexName, typeName, metricId);
                } else {
                    throw new ClientException(ClientErrorCodes.ACTION_ERROR, "Unexpected action response");
                }
            }
            return bulkResponse;
        } else {
            throw buildExceptionFromUnsuccessfulResponse("Upsert", updateResponse);
        }
    }

    @Override
    public <T> T find(TypeDescriptor typeDescriptor, Object query, Class<T> clazz) throws ClientException {
        ResultList<T> result = query(typeDescriptor, query, clazz);
        if (result.getTotalCount() == 0) {
            return null;
        } else {
            return result.getResult().get(0);
        }
    }

    @Override
    public <T> ResultList<T> query(TypeDescriptor typeDescriptor, Object query, Class<T> clazz) throws ClientException {
        RestClient client = getClient();
        JsonNode queryMap = queryConverter.convertQuery(query);
        Object queryFetchStyle = queryConverter.getFetchStyle(query);
        logger.debug(QUERY_CONVERTED_QUERY, queryMap);
        long totalCount = 0;
        ArrayNode resultsNode = null;
        Response queryResponse = restCallTimeoutHandler(() -> client.performRequest(
                GET_ACTION,
                getSearchPath(typeDescriptor),
                Collections.emptyMap(),
                EntityBuilder.create().setText(MAPPER.writeValueAsString(queryMap)).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), typeDescriptor.getIndex(), "QUERY");

        if (isRequestSuccessful(queryResponse)) {
            JsonNode responseNode;
            try {
                responseNode = MAPPER.readTree(EntityUtils.toString(queryResponse.getEntity()));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }
            JsonNode hitsNode = responseNode.get(KEY_HITS);
            totalCount = hitsNode.get(KEY_TOTAL).asLong();
            if (totalCount > Integer.MAX_VALUE) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, CLIENT_HITS_MAX_VALUE_EXCEEDED);
            }
            resultsNode = ((ArrayNode) hitsNode.get(KEY_HITS));
        } else if (queryResponse != null) {
            throw buildExceptionFromUnsuccessfulResponse("Query", queryResponse);
        }

        ResultList<T> resultList = new ResultList<>(totalCount);
        if (resultsNode != null && resultsNode.size() > 0) {
            for (JsonNode result : resultsNode) {
                Map<String, Object> object = MAPPER.convertValue(result.get(SchemaKeys.KEY_SOURCE), Map.class);
                String id = result.get(KEY_DOC_ID).asText();
                String index = result.get(KEY_DOC_INDEX).asText();
                String type = result.get(KEY_DOC_TYPE).asText();
                object.put(ModelContext.TYPE_DESCRIPTOR_KEY, new TypeDescriptor(index, type));
                object.put(ModelContext.DATASTORE_ID_KEY, id);
                object.put(QueryConverter.QUERY_FETCH_STYLE_KEY, queryFetchStyle);

                resultList.add(modelContext.unmarshal(clazz, object));
            }
        }
        return resultList;
    }

    @Override
    public long count(TypeDescriptor typeDescriptor, Object query) throws ClientException {
        RestClient client = getClient();
        JsonNode queryMap = queryConverter.convertQuery(query);
        logger.debug(COUNT_CONVERTED_QUERY, queryMap);
        long totalCount = 0;
        Response queryResponse = restCallTimeoutHandler(() -> client.performRequest(
                GET_ACTION,
                getSearchPath(typeDescriptor),
                Collections.emptyMap(),
                EntityBuilder.create().setText(MAPPER.writeValueAsString(queryMap)).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), typeDescriptor.getIndex(), "COUNT");
        if (isRequestSuccessful(queryResponse)) {
            JsonNode responseNode;
            try {
                responseNode = MAPPER.readTree(EntityUtils.toString(queryResponse.getEntity()));
            } catch (IOException e) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
            }
            JsonNode hitsNode = responseNode.get(KEY_HITS);
            totalCount = hitsNode.get(KEY_TOTAL).asLong();
            if (totalCount > Integer.MAX_VALUE) {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, CLIENT_HITS_MAX_VALUE_EXCEEDED);
            }
        } else if (queryResponse != null) {
            throw buildExceptionFromUnsuccessfulResponse("Count", queryResponse);
        }

        return totalCount;
    }

    @Override
    public void delete(TypeDescriptor typeDescriptor, String id) throws ClientException {
        logger.debug("Delete - id: '{}'", id);
        RestClient client = getClient();
        Response deleteResponse = restCallTimeoutHandler(() -> client.performRequest(
                DELETE_ACTION,
                getIdPath(typeDescriptor, id),
                Collections.emptyMap()), typeDescriptor.getIndex(), DELETE_ACTION);

        if (deleteResponse != null && !isRequestSuccessful(deleteResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Delete", deleteResponse);
        }
    }

    @Override
    public void deleteByQuery(TypeDescriptor typeDescriptor, Object query) throws ClientException {
        RestClient client = getClient();
        JsonNode queryMap = queryConverter.convertQuery(query);
        logger.debug(QUERY_CONVERTED_QUERY, queryMap);
        Response deleteResponse = restCallTimeoutHandler(() -> client.performRequest(
                POST_ACTION,
                getDeleteByQueryPath(typeDescriptor),
                Collections.emptyMap(),
                EntityBuilder.create().setText(MAPPER.writeValueAsString(queryMap)).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), typeDescriptor.getIndex(), "DELETE BY QUERY");

        if (deleteResponse != null && !isRequestSuccessful(deleteResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Delete by query", deleteResponse);
        }
    }

    @Override
    public IndexResponse isIndexExists(IndexRequest indexRequest) throws ClientException {
        logger.debug("Index exists - index name: '{}'", indexRequest.getIndex());
        RestClient client = getClient();
        Response isIndexExistsResponse = restCallTimeoutHandler(() -> client.performRequest(
                HEAD_ACTION,
                getIndexPath(indexRequest.getIndex()),
                Collections.emptyMap()), indexRequest.getIndex(), "INDEX EXIST");

        if (isIndexExistsResponse != null && isIndexExistsResponse.getStatusLine() != null) {
            if (isIndexExistsResponse.getStatusLine().getStatusCode() == 200) {
                return new IndexResponse(true);
            } else if (isIndexExistsResponse.getStatusLine().getStatusCode() == 404) {
                return new IndexResponse(false);
            }
        }

        throw buildExceptionFromUnsuccessfulResponse("Index exists", isIndexExistsResponse);
    }

    @Override
    public IndexResponse findIndexes(IndexRequest indexRequest) throws ClientException {
        logger.debug("Find indexes - index prefix: '{}'", indexRequest.getIndex());
        RestClient client = getClient();
        Response isIndexExistsResponse = restCallTimeoutHandler(() -> client.performRequest(
                GET_ACTION,
                getFindIndexPath(indexRequest.getIndex()),
                Collections.singletonMap("pretty", "true")), indexRequest.getIndex(), "INDEX EXIST");

        if (isIndexExistsResponse != null && isIndexExistsResponse.getStatusLine() != null) {
            if (isIndexExistsResponse.getStatusLine().getStatusCode() == 200) {
                try {
                    return new IndexResponse(EntityUtils.toString(isIndexExistsResponse.getEntity()).split("\n"));
                } catch (ParseException | IOException e) {
                    throw new ClientException(ClientErrorCodes.ACTION_ERROR, e, "Cannot convert the indexes list");
                }
            } else if (isIndexExistsResponse.getStatusLine().getStatusCode() == 404) {
                return new IndexResponse(null);
            }
        }

        throw buildExceptionFromUnsuccessfulResponse("Find indexes", isIndexExistsResponse);
    }

    @Override
    public void createIndex(String indexName, ObjectNode indexSettings) throws ClientException {
        logger.debug("Create index - object: '{}'", indexSettings);
        RestClient client = getClient();
        Response createIndexResponse = restCallTimeoutHandler(() -> {
            Response response = client.performRequest(
                    PUT_ACTION,
                    getIndexPath(indexName),
                    Collections.emptyMap(),
                    EntityBuilder.create().setText(MAPPER.writeValueAsString(indexSettings)).setContentType(ContentType.APPLICATION_JSON).build(),
                    new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
            return response;
        }, indexName, "CREATE INDEX");

        if (!isRequestSuccessful(createIndexResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Create index", createIndexResponse);
        }
    }

    @Override
    public boolean isMappingExists(TypeDescriptor typeDescriptor) throws ClientException {
        logger.debug("Mapping exists - mapping name: '{} - {}'", typeDescriptor.getIndex(), typeDescriptor.getType());
        RestClient client = getClient();
        Response isMappingExistsResponse = restCallTimeoutHandler(() -> client.performRequest(
                GET_ACTION,
                getMappingPath(typeDescriptor),
                Collections.emptyMap()), typeDescriptor.getIndex(), "MAPPING EXIST");

        if (isMappingExistsResponse != null && isMappingExistsResponse.getStatusLine() != null) {
            if (isMappingExistsResponse.getStatusLine().getStatusCode() == 200) {
                return true;
            } else if (isMappingExistsResponse.getStatusLine().getStatusCode() == 404) {
                return false;
            }
        }

        throw buildExceptionFromUnsuccessfulResponse("Mapping exists", isMappingExistsResponse);
    }

    @Override
    public void putMapping(TypeDescriptor typeDescriptor, JsonNode mapping) throws ClientException {
        logger.debug("Create mapping - object: '{}, index: {}, type: {}'", mapping, typeDescriptor.getIndex(), typeDescriptor.getType());
        RestClient client = getClient();
        Response createMappingResponse = restCallTimeoutHandler(() -> client.performRequest(
                PUT_ACTION,
                getMappingPath(typeDescriptor),
                Collections.emptyMap(),
                EntityBuilder.create().setText(MAPPER.writeValueAsString(mapping)).setContentType(ContentType.APPLICATION_JSON).build(),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())), typeDescriptor.getIndex(), "PUT MAPPING");

        if (!isRequestSuccessful(createMappingResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Create mapping", createMappingResponse);
        }
    }

    @Override
    public void refreshAllIndexes() throws ClientException {
        logger.debug("Refresh all indexes");
        RestClient client = getClient();
        Response refreshIndexResponse = restCallTimeoutHandler(() -> client.performRequest(
                POST_ACTION,
                getRefreshAllIndexesPath(),
                Collections.emptyMap()), INDEX_ALL, "REFRESH INDEX");

        if (!isRequestSuccessful(refreshIndexResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Refresh all indexes", refreshIndexResponse);
        }
    }

    @Override
    public void deleteAllIndexes() throws ClientException {
        logger.debug("Delete all indexes");
        RestClient client = getClient();
        Response deleteIndexResponse = restCallTimeoutHandler(() -> client.performRequest(
                DELETE_ACTION,
                getIndexPath("_all"),
                Collections.emptyMap()), INDEX_ALL, "DELETE INDEX");

        if (!isRequestSuccessful(deleteIndexResponse)) {
            throw buildExceptionFromUnsuccessfulResponse("Delete all indexes", deleteIndexResponse);
        }
    }

    @Override
    public void deleteIndexes(String... indexes) throws ClientException {
        logger.debug("Delete indexes");
        RestClient client = getClient();
        for (String index : indexes) {
            logger.debug("Delete index {}", index);
            Response deleteIndexResponse = restCallTimeoutHandler(() -> {
                logger.debug("Deleting index {}", index);
                return client.performRequest(
                        DELETE_ACTION,
                        getIndexPath(index),
                        Collections.emptyMap());
            }, index, "DELETE INDEX");

            // for that call the deleteIndexResponse=null case could be considered as good response since if an index doesn't exist (404) the delete could be considered successful.
            // the deleteIndexResponse is null also if the error is due to a bad index request (400) but this error, except if there is an application bug, shouldn't never happen.
            if (deleteIndexResponse == null) {
                logger.debug("Deleting index {} : index does not exist", index);
            } else if (!isRequestSuccessful(deleteIndexResponse)) {
                throw buildExceptionFromUnsuccessfulResponse("Delete indexes", deleteIndexResponse);
            }
            logger.debug("Deleting index {} DONE", index);
        }
    }

    private <T> T restCallTimeoutHandler(Callable<T> restAction, String index, String operationName) throws ClientException {
        int retryCount = 0;
        try {
            do {
                try {
                    return restAction.call();
                } catch (RuntimeException e) {
                    restCallRuntimeExecCount.inc();
                    if (e.getCause() instanceof TimeoutException) {
                        timeoutRetryCount.inc();
                        if (retryCount < MAX_RETRY_ATTEMPT - 1) {
                            // try again
                            try {
                                Thread.sleep((long) (MAX_RETRY_WAIT_TIME * (0.5 + RANDOM.nextFloat() / 2)));
                            } catch (InterruptedException e1) {
                                // DO NOTHING
                            }
                        }
                    } else {
                        throw e;
                    }
                }
            } while (++retryCount <= MAX_RETRY_ATTEMPT);
        } catch (ResponseException re) {
            if (re.getResponse().getStatusLine().getStatusCode() == 404) {
                logger.warn("Resource for index '{}' not found on action '{}'! {}", index, operationName, re.getLocalizedMessage());
                return null;
            } else if (re.getResponse().getStatusLine().getStatusCode() == 400) {
                logger.warn("Bad request for index '{}' on action '{}'! {}", index, operationName, re.getLocalizedMessage());
                return null;
            } else {
                throw new ClientException(ClientErrorCodes.ACTION_ERROR, re);
            }
        } catch (IOException e) {
            throw new ClientException(ClientErrorCodes.ACTION_ERROR, e);
        } catch (Exception e) {
            throw new ClientInternalError(e);
        }
        timeoutRetryLimitReachedCount.inc();

        throw new ClientCommunicationException();
    }

    private boolean isRequestSuccessful(Response response) {
        if (response != null && response.getStatusLine() != null) {
            return isRequestSuccessful(response.getStatusLine().getStatusCode());
        } else {
            return false;
        }
    }

    private boolean isRequestSuccessful(int responseCode) {
        return (200 <= responseCode && responseCode <= 299);
    }

    /**
     * Builds a {@link ClientActionResponseException} from the {@link Response} tring to get the reason from it.
     *
     * @param action   The action that was performed
     * @param response The {@link Response} from Elasticsearch
     * @return The {@link ClientActionResponseException} to throw.
     * @since 1.3.0
     */
    private ClientException buildExceptionFromUnsuccessfulResponse(String action, Response response) {
        String reason;
        if (response != null && response.getStatusLine() != null) {
            reason = response.getStatusLine().getReasonPhrase();
        } else {
            reason = "Unknown. Cannot get the reason from Response";
        }

        return new ClientActionResponseException(action, reason);
    }

    private String getRefreshAllIndexesPath() {
        return "/_all/_refresh";
    }

    private String getIndexPath(String index) {
        return String.format("/%s", index);
    }

    private String getFindIndexPath(String index) {
        return String.format("/_cat/indices?h=index&index=%s", index);
    }

    private String getBulkPath() {
        return "/_bulk";
    }

    private String getInsertTypePath(InsertRequest request) {
        if (request.getId() != null) {
            return String.format("%s?id=%s&version=1&version_type=external", getTypePath(request.getTypeDescriptor()), request.getId());
        } else {
            return getTypePath(request.getTypeDescriptor());
        }
    }

    private String getTypePath(TypeDescriptor typeDescriptor) {
        return String.format("/%s/%s", typeDescriptor.getIndex(), typeDescriptor.getType());
    }

    private String getDeleteByQueryPath(TypeDescriptor typeDescriptor) {
        return String.format("/%s/%s/_delete_by_query", typeDescriptor.getIndex(), typeDescriptor.getType());
    }

    private String getMappingPath(TypeDescriptor typeDescriptor) {
        return String.format("/%s/_mapping/%s", typeDescriptor.getIndex(), typeDescriptor.getType());
    }

    private String getIdPath(TypeDescriptor typeDescriptor, String id) throws UnsupportedEncodingException {
        return String.format("/%s/%s/%s", typeDescriptor.getIndex(), typeDescriptor.getType(), URLEncoder.encode(id, "UTF-8"));
    }

    private String getUpsertPath(TypeDescriptor typeDescriptor, String id) throws UnsupportedEncodingException {
        return String.format("/%s/%s/%s/_update", typeDescriptor.getIndex(), typeDescriptor.getType(), URLEncoder.encode(id, "UTF-8"));
    }

    private String getSearchPath(TypeDescriptor typeDescriptor) {
        return String.format("/%s/%s/_search", typeDescriptor.getIndex(), typeDescriptor.getType());
    }

}
