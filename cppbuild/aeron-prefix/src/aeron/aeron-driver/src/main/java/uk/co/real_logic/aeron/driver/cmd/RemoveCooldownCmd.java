/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.aeron.driver.cmd;

import uk.co.real_logic.aeron.driver.Receiver;
import uk.co.real_logic.aeron.driver.media.ReceiveChannelEndpoint;

public class RemoveCooldownCmd implements ReceiverCmd
{
    private final int sessionId;
    private final int streamId;
    private final ReceiveChannelEndpoint channelEndpoint;

    public RemoveCooldownCmd(final ReceiveChannelEndpoint channelEndpoint, final int sessionId, final int streamId)
    {
        this.sessionId = sessionId;
        this.streamId = streamId;
        this.channelEndpoint = channelEndpoint;
    }

    public void execute(Receiver receiver)
    {
        receiver.onRemoveCooldown(channelEndpoint, sessionId, streamId);
    }
}
