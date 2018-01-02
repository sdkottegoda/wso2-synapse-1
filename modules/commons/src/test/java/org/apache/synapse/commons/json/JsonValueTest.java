/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.commons.json;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tests if certain json values are processed correctly.
 */
public class JsonValueTest extends TestCase {
    private static Log logger = LogFactory.getLog(JsonValueTest.class.getName());

    private static final String STRING_VAL = "\"Hello world!\"";
    private static final String NULL_VAL = "null";
    private static final String TRUE_VAL = "true";
    private static final String FALSE_VAL = "false";
    private static final String NUMBER_VAL_1 = "12.5";
    private static final String NUMBER_VAL_2 = "-12.5";
    private static final String NUMBER_VAL_3 = "123";
    private static final String FAIL_TEST_VAL_1 = "123abc";
    private static final String FAIL_TEST_VAL_2 = "task";
    private static final String FAIL_TEST_VAL_3 = "fun";
    private static final String FAIL_TEST_VAL_4 = "abc";
    private static final String FAIL_TEST_VAL_5 = "-abc";
    private static final String FAIL_TEST_VAL_6 = "\"Hello world!";

    /**
     * Tests JSON value enclosed in double quotes.
     */
    public void testJSONValueString() {
        runTest(STRING_VAL);
    }

    /**
     * Tests JSON value null.
     */
    public void testJSONValueNull() {
        runTest(NULL_VAL);
    }

    /**
     * Tests JSON value true.
     */
    public void testJSONValueTrue() {
        runTest(TRUE_VAL);
    }

    /**
     * Tests JSON value false.
     */
    public void testJSONValueFalse() {
        runTest(FALSE_VAL);
    }

    /**
     * Tests JSON value number with a decimal point.
     */
    public void testJSONValueNumberWithDecimal() {
        runTest(NUMBER_VAL_1);
    }

    /**
     * Tests JSON value negative number with a decimal point.
     */
    public void testJSOnValueNegativeNumber() {
        runTest(NUMBER_VAL_2);
    }

    /**
     * Tests JSON value number.
     */
    public void testJSONValueNumber() {
        runTest(NUMBER_VAL_3);
    }

    /**
     * Convenient function to test correct JSON values.
     *
     * @param value the {@link String} value to be tested
     */
    private void runTest(String value) {
        try {
            InputStream inputStream = Util.newInputStream(value.getBytes());
            MessageContext messageContext = Util.newMessageContext();
            OMElement element = JsonUtil.getNewJsonPayload(messageContext, inputStream, true, true);
            OutputStream out = Util.newOutputStream();
            JsonUtil.writeAsJson(element, out);
            assertEquals("Output JSON value is not same as input JSON value", value, out.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            fail("An exception occurred in testing JsonUtil.writeAsJson method.");
        }
    }

    /**
     * Tests incorrect JSON value: text starting with number without quotes.
     */
    public void testNonJSONValue() {
        runFailTest(FAIL_TEST_VAL_1);
    }

    /**
     * Tests incorrect JSON value: text starting with t without quotes.
     */
    public void testNonJSONValueStartingWithT() {
        runFailTest(FAIL_TEST_VAL_2);
    }

    /**
     * Tests incorrect JSON value: text starting with f without quotes.
     */
    public void testNonJSONValueStartingWithF() {
        runFailTest(FAIL_TEST_VAL_3);
    }

    /**
     * Tests incorrect JSON value: text without quotes.
     */
    public void testNonJSONValueWithoutQuotes() {
        runFailTest(FAIL_TEST_VAL_4);
    }

    /**
     * Tests incorrect JSON value: text starting with - without quotes.
     */
    public void testNonJSONValueStartingWithDash() {
        runFailTest(FAIL_TEST_VAL_5);
    }

    /**
     * Tests incorrect JSON value: text not having a closing quote.
     */
    public void testNonJSONValueWithoutCloseTags() {
        runFailTest(FAIL_TEST_VAL_6);
    }

    /**
     * Convenient function to test incorrect JSON values
     *
     * @param value the {@link String} value to be tested
     */
    private void runFailTest(String value) {
        try {
            InputStream inputStream = Util.newInputStream(value.getBytes());
            MessageContext messageContext = Util.newMessageContext();
            JsonUtil.getNewJsonPayload(messageContext, inputStream, true, true);
            fail("AxisFault exception has to be thrown");
        } catch (AxisFault ex) {
            //Test succeeds
        }
    }

    /**
     * Test with passing null element.
     *
     * @throws IOException
     */
    public void testWriteAsJsonNullElement() throws IOException {
        OutputStream out = Util.newOutputStream();
        try {
            JsonUtil.writeAsJson((OMElement) null, out);
            Assert.fail("AxisFault expected");
        } catch (AxisFault axisFault) {
            Assert.assertEquals("Invalid fault message received", "OMElement is null. Cannot convert to JSON.",
                    axisFault.getMessage());
        } finally {
            out.close();
        }
    }
}
