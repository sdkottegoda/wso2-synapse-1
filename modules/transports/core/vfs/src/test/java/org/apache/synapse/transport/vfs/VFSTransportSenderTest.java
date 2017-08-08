/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.apache.synapse.transport.vfs;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.synapse.commons.vfs.VFSOutTransportInfo;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test Memory leak issues of VFS sender
 */
public class VFSTransportSenderTest {

    private static final int FILE_SEND_ITERATIONS = 1;

    public void testMemoryLeakWhileLockCreation() throws AxisFault, NoSuchFieldException, IllegalAccessException {
        VFSTransportSender vfsTransportSender = new VFSTransportSender();

        ConfigurationContext configurationContext = new ConfigurationContext(new AxisConfiguration());

        TransportOutDescription transportOutDescription = new TransportOutDescription("Test");

        vfsTransportSender.init(configurationContext, transportOutDescription);

        org.apache.axis2.context.MessageContext mc = new org.apache.axis2.context.MessageContext();

        for (int i = 0; i < FILE_SEND_ITERATIONS; i++) {
            String fName = "test1://foo/bar/test" + i + "-" + System.currentTimeMillis()
                    + ".ack?transport.vfs.MaxRetryCount=0&transport.vfs.ReconnectTimeout=1";
            OutTransportInfo outTransportInfo = new VFSOutTransportInfo(fName, true);
            try {

                vfsTransportSender.sendMessage(mc, fName, outTransportInfo);
            } catch (AxisFault fse) {
                //Ignore
            }
        }

        //Perform the GC
        System.gc();

        Map<?, ?> refReverseMap = getSoftReferenceMap(vfsTransportSender);
        assertEquals("If there is no memory leak, soft reference map size should be zero.", 0, refReverseMap.size());

    }

    /**
     * Get soft reference file cache map
     * @param vfsTransportSender
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private Map<?, ?> getSoftReferenceMap(VFSTransportSender vfsTransportSender)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = VFSTransportSender.class.getDeclaredField("fsManager");
        field.setAccessible(true);

        FileSystemManager fsm = (FileSystemManager) field.get(vfsTransportSender);
        FilesCache fileCache = fsm.getFilesCache();
        SoftRefFilesCache softRefFilesCache = (SoftRefFilesCache) fileCache;
        Field field1 = SoftRefFilesCache.class.getDeclaredField("refReverseMap");
        field1.setAccessible(true);
        return (Map<?, ?>) (Map) field1.get(softRefFilesCache);
    }


}