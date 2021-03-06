/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.services.kinesisvideo;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.MalformedURLException;

import com.amazonaws.services.kinesisvideo.model.APIName;
import com.amazonaws.services.kinesisvideo.model.ComparisonOperator;
import com.amazonaws.services.kinesisvideo.model.CreateStreamRequest;
import com.amazonaws.services.kinesisvideo.model.CreateStreamResult;
import com.amazonaws.services.kinesisvideo.model.DeleteStreamRequest;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointRequest;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointResult;
import com.amazonaws.services.kinesisvideo.model.ListStreamsRequest;
import com.amazonaws.services.kinesisvideo.model.ListStreamsResult;
import com.amazonaws.services.kinesisvideo.model.StreamInfo;
import com.amazonaws.services.kinesisvideo.model.StreamNameCondition;

public class KinesisVideoIntegrationTest extends KinesisVideoIntegrationTestBase {
    private static final String streamPrefix = "kinesisvideo-integration-test-";

    @After
    public void tearDown() {
        String currentNextToken = null;

        do {
            ListStreamsResult listStreamsResult = kvClient.listStreams(new ListStreamsRequest()
                    .withStreamNameCondition(new StreamNameCondition()
                            .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                            .withComparisonValue(streamPrefix))
                    .withNextToken(currentNextToken));
            currentNextToken = listStreamsResult.getNextToken();

            for (StreamInfo info : listStreamsResult.getStreamInfoList()) {
                if (info.getStreamName().startsWith(streamPrefix)) {
                    kvClient.deleteStream(new DeleteStreamRequest()
                            .withStreamARN(info.getStreamARN()));
                }
            }
        } while (currentNextToken != null);
    }

	@Test
	public void testGetDataEndpointResult() {
        String streamName = streamPrefix + System.currentTimeMillis();

        CreateStreamResult createStreamResult = kvClient.createStream(new CreateStreamRequest()
                .withStreamName(streamName)
                .withDataRetentionInHours(2)
                .withDeviceName("integration-test-device"));

		GetDataEndpointResult dataEndpoint = kvClient.getDataEndpoint(new GetDataEndpointRequest()
                .withStreamName(streamName)
                .withAPIName(APIName.GET_HLS_STREAMING_SESSION_URL));

        String endpoint = dataEndpoint.getDataEndpoint();
        try {
            // URL construction checks for malformed URL
            new URL(endpoint);
        } catch (MalformedURLException e) {
            fail("Data endpoint is not a valid URL");
        }
	}
}