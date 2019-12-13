/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package com.rukspot.sample.apimgt.workflow.apibase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.SubscriptionCreationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

public class SubscriptionWorkflow extends SubscriptionCreationWSWorkflowExecutor {
    private static final Log log = LogFactory.getLog(SubscriptionWorkflow.class);
    private static final String WF_TYPE = "apiType";
    private static final String WF_TYPE_SECURED = "secured";

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        APIIdentifier apiIdentifier = new APIIdentifier(subsWorkflowDTO.getApiProvider(), subsWorkflowDTO.getApiName(),
                subsWorkflowDTO.getApiVersion());
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
            API api = apiConsumer.getAPI(apiIdentifier);
            if (api.getAdditionalProperties().containsKey(WF_TYPE) && WF_TYPE_SECURED
                    .equalsIgnoreCase(api.getAdditionalProperties().get(WF_TYPE).toString())) {
                return super.execute(workflowDTO);
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while processing workflow request.", e);
            throw new WorkflowException("Error occurred while processing workflow request.", e);
        }

        //Default flow: complete the subscription creation
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        WorkflowResponse workflowResponse = complete(workflowDTO);
        super.publishEvents(workflowDTO);

        return workflowResponse;
    }
}
