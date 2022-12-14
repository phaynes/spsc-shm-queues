/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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
package uk.co.real_logic.aeron.driver.buffer;

import org.junit.Test;
import uk.co.real_logic.aeron.driver.media.UdpChannel;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FileMappingConventionTest
{
    @Test
    public void uriStringsAreValidFiles() throws IOException
    {
        assertIsValidFile(uriToDir("udp://localhost:40123@localhost:40124"));
        assertIsValidFile(uriToDir("udp://localhost:40124"));
    }

    private String uriToDir(final String uri)
    {
        final UdpChannel udpChannel = UdpChannel.parse(uri);

        return udpChannel.canonicalForm();
    }

    private void assertIsValidFile(final String channelDir) throws IOException
    {
        final File file = new File(channelDir);
        assertTrue("Can't createImage a file", file.mkdir());
        assertTrue("Failed to clean up", file.delete());
    }
}
