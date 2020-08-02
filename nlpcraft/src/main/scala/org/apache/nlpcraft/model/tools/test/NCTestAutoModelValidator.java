/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.tools.test;

import org.apache.nlpcraft.model.*;
import org.apache.nlpcraft.model.tools.test.impl.*;
import java.util.*;

/**
 * Auto-validator for models based on {@link NCIntentSample} annotations. This class takes one or more model IDs
 * (or class names) and performs validation. Validation consists of starting an embedded probe with a given model,
 * scanning for {@link NCIntentSample} annotations and their corresponding callback methods, submitting each sample input
 * sentences from {@link NCIntentSample} annotation and checking that resulting intent matches the intent the sample
 * was attached to.
 * <p>
 * Note that this class can be used in two modes:
 * <ul>
 *     <li>
 *         As a standalone application supplying model classes using <code>NLPCRAFT_TEST_MODELS</code> system property.
 *         In this mode it can be used to automatically test models from IDE, maven builds, etc. without
 *         creating a separate, dedicated unit test for it.
 *     </li>
 *     <li>
 *         As a utility class that can be called programmatically from other classes, e.g. unit tests. See
 *         various <code>isValid(...)</code> methods for more details.
 *     </li>
 * </ul>
 * 
 * @see NCIntentSample
 * @see NCIntent
 * @see NCIntentRef
 */
public class NCTestAutoModelValidator {
    /**
     * Performs validation based on {@link NCIntentSample} annotations.
     * <p>
     * Entry point for a standalone application that expects model classes supplied
     * using <code>NLPCRAFT_TEST_MODELS</code> system property. This system property should have a comma separated
     * list of data model class names. In this mode it can be used to automatically test
     * models from IDE, maven builds, etc. without creating a separate, dedicated unit test for it.
     * <p>
     * Note that standard validation output will be printed out to the configured logger.
     * 
     * @param args These arguments are ignored.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     */
    public static void main(String[] args) throws Exception {
        NCTestAutoModelValidatorImpl.isValid();
    }

    /**
     * Performs validation based on {@link NCIntentSample} annotations. Similar to the standard
     * <code>main(String[])</code>, this method expects model classes supplied
     * using <code>NLPCRAFT_TEST_MODELS</code> system property. This system property should have a comma separated
     * list of data model class names.
     * 
     * @return <code>True</code> if no validation errors found, <code>false</code> otherwise. Note that
     *      standard validation output will be printed out to the configured logger.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     */
    public static boolean isValid() throws Exception {
        return NCTestAutoModelValidatorImpl.isValid();
    }

    /**
     * Performs validation based on {@link NCIntentSample} annotations for given model.
     *
     * @param claxx Data model class.
     * @return <code>True</code> if no validation errors found, <code>false</code> otherwise. Note that
     *      standard validation output will be printed out to the configured logger.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     */
    public static boolean isValid(Class<NCModel> claxx) throws Exception {
        return NCTestAutoModelValidatorImpl.isValidForClass(claxx);
    }

    /**
     * Performs validation based on {@link NCIntentSample} annotations for given models.
     *
     * @param mdlIds One or more ID of the model to validate.
     * @return <code>True</code> if no validation errors found, <code>false</code> otherwise. Note that
     *      standard validation output will be printed out to the configured logger.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     *
     * @see NCModelView#getId()
     */
    public static boolean isValid(String... mdlIds) throws Exception {
        return NCTestAutoModelValidatorImpl.isValidForModelIds(mdlIds);
    }

    /**
     * Performs validation based on {@link NCIntentSample} annotations for given models.
     *
     * @param mdlIds Comma separate list of one or more model IDs to validate.
     * @return <code>True</code> if no validation errors found, <code>false</code> otherwise. Note that
     *      standard validation output will be printed out to the configured logger.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     *
     * @see NCModelView#getId()
     */
    public static boolean isValid(String mdlIds) throws Exception {
        return NCTestAutoModelValidatorImpl.isValidForModelIds(mdlIds);
    }

    /**
     * Performs validation based on {@link NCIntentSample} annotations for given models.
     *
     * @param mdlIds Collection of model IDs to validate.
     * @return <code>True</code> if no validation errors found, <code>false</code> otherwise. Note that
     *      standard validation output will be printed out to the configured logger.
     * @throws Exception Thrown in case of any unexpected errors during validation. Note that standard validation
     *      output will be printed out to the configured logger.
     *
     * @see NCModelView#getId()
     */
    public static boolean isValid(Collection<String> mdlIds) throws Exception {
        return NCTestAutoModelValidatorImpl.isValidForModelIds(mdlIds);
    }
}
