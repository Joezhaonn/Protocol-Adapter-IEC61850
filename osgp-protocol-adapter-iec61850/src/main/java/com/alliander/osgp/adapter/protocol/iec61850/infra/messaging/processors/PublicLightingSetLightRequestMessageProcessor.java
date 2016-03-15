/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.processors;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceResponseHandler;
import com.alliander.osgp.adapter.protocol.iec61850.device.requests.SetLightDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.DeviceRequestMessageProcessor;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.DeviceRequestMessageType;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.RequestMessageData;
import com.alliander.osgp.dto.valueobjects.LightValueMessageDataContainer;
import com.alliander.osgp.shared.infra.jms.Constants;

/**
 * Class for processing public lighting set light request messages
 */
@Component("iec61850PublicLightingSetLightRequestMessageProcessor")
public class PublicLightingSetLightRequestMessageProcessor extends DeviceRequestMessageProcessor {
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PublicLightingSetLightRequestMessageProcessor.class);

    public PublicLightingSetLightRequestMessageProcessor() {
        super(DeviceRequestMessageType.SET_LIGHT);
    }

    @Override
    public void processMessage(final ObjectMessage message) {
        LOGGER.debug("Processing public lighting set light request message");

        String correlationUid = null;
        String domain = null;
        String domainVersion = null;
        String messageType = null;
        String organisationIdentification = null;
        String deviceIdentification = null;
        String ipAddress = null;
        int retryCount = 0;
        boolean isScheduled = false;
        LightValueMessageDataContainer lightValueMessageDataContainer = null;

        try {
            correlationUid = message.getJMSCorrelationID();
            domain = message.getStringProperty(Constants.DOMAIN);
            domainVersion = message.getStringProperty(Constants.DOMAIN_VERSION);
            messageType = message.getJMSType();
            organisationIdentification = message.getStringProperty(Constants.ORGANISATION_IDENTIFICATION);
            deviceIdentification = message.getStringProperty(Constants.DEVICE_IDENTIFICATION);
            ipAddress = message.getStringProperty(Constants.IP_ADDRESS);
            retryCount = message.getIntProperty(Constants.RETRY_COUNT);
            isScheduled = message.propertyExists(Constants.IS_SCHEDULED) ? message
                    .getBooleanProperty(Constants.IS_SCHEDULED) : false;

                    lightValueMessageDataContainer = (LightValueMessageDataContainer) message.getObject();

        } catch (final JMSException e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            LOGGER.debug("correlationUid: {}", correlationUid);
            LOGGER.debug("domain: {}", domain);
            LOGGER.debug("domainVersion: {}", domainVersion);
            LOGGER.debug("messageType: {}", messageType);
            LOGGER.debug("organisationIdentification: {}", organisationIdentification);
            LOGGER.debug("deviceIdentification: {}", deviceIdentification);
            LOGGER.debug("ipAddress: {}", ipAddress);
            return;
        }

        final RequestMessageData requestMessageData = new RequestMessageData(lightValueMessageDataContainer, domain,
                domainVersion, messageType, retryCount, isScheduled);

        final DeviceResponseHandler deviceResponseHandler = new DeviceResponseHandler() {

            @Override
            public void handleResponse(final DeviceResponse deviceResponse) {
                PublicLightingSetLightRequestMessageProcessor.this.handleEmptyDeviceResponse(deviceResponse,
                        PublicLightingSetLightRequestMessageProcessor.this.responseMessageSender,
                        requestMessageData.getDomain(), requestMessageData.getDomainVersion(),
                        requestMessageData.getMessageType(), requestMessageData.getRetryCount());
            }

            @Override
            public void handleException(final Throwable t, final DeviceResponse deviceResponse) {
                PublicLightingSetLightRequestMessageProcessor.this.handleUnableToConnectDeviceResponse(deviceResponse,
                        t, requestMessageData.getMessageData(),
                        PublicLightingSetLightRequestMessageProcessor.this.responseMessageSender, deviceResponse,
                        requestMessageData.getDomain(), requestMessageData.getDomainVersion(),
                        requestMessageData.getMessageType(), requestMessageData.isScheduled(),
                        requestMessageData.getRetryCount());
            }
        };

        try {

            LOGGER.info("Calling DeviceService function: {} for domain: {} {}", messageType, domain, domainVersion);

            final SetLightDeviceRequest deviceRequest = new SetLightDeviceRequest(organisationIdentification,
                    deviceIdentification, correlationUid, lightValueMessageDataContainer, domain, domainVersion,
                    messageType, ipAddress, retryCount, isScheduled);

            this.deviceService.setLight(deviceRequest, deviceResponseHandler);
        } catch (final Exception e) {
            this.handleError(e, correlationUid, organisationIdentification, deviceIdentification, domain,
                    domainVersion, messageType, retryCount);
        }
    }
}
