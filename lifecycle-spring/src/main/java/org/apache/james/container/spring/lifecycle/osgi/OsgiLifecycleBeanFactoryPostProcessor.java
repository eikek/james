/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.container.spring.lifecycle.osgi;

import org.apache.james.container.spring.lifecycle.ConfigurableBeanPostProcessor;
import org.apache.james.container.spring.lifecycle.LogEnabledBeanPostProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.osgi.extender.OsgiBeanFactoryPostProcessor;


public class OsgiLifecycleBeanFactoryPostProcessor implements OsgiBeanFactoryPostProcessor {

    private ConfigurableBeanPostProcessor configurationProcessor;
    private LogEnabledBeanPostProcessor loggingProcessor;
    private CommonAnnotationBeanPostProcessor annotationProcessor;

    public void setConfigurationProcessor(ConfigurableBeanPostProcessor configurationProcessor) {
        this.configurationProcessor = configurationProcessor;
    }

    public void setLoggingProcessor(LogEnabledBeanPostProcessor loggingProcessor) {
        this.loggingProcessor = loggingProcessor;
    }

    public void setAnnotationProcessor(CommonAnnotationBeanPostProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    @Override
    public void postProcessBeanFactory(BundleContext context, ConfigurableListableBeanFactory factory) throws BeansException, InvalidSyntaxException, BundleException {
        // We need to set the beanfactory by hand. This MAY be a bug in spring-dm but I'm not sure yet
        loggingProcessor.setBeanFactory(factory);
        factory.addBeanPostProcessor(loggingProcessor);
        configurationProcessor.setBeanFactory(factory);
        factory.addBeanPostProcessor(configurationProcessor);
        annotationProcessor.setBeanFactory(factory);
        factory.addBeanPostProcessor(annotationProcessor);

    }

}