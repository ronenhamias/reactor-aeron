/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.ipc.aeron.client;

import io.aeron.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.FluxProcessor;
import reactor.ipc.aeron.AeronWrapper;
import reactor.ipc.aeron.AeronInbound;
import reactor.ipc.aeron.AeronFlux;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Anatoly Kadyshev
 */
final class AeronClientInbound implements AeronInbound, Disposable {

    private final ClientPooler pooler;

    private final AeronFlux flux;

    public AeronClientInbound(AeronWrapper wrapper, String channel, int streamId, UUID sessionId, String name) {
        Objects.requireNonNull(sessionId, "sessionId");

        Subscription subscription = wrapper.addSubscription(channel, streamId, "receiving data", sessionId);

        this.pooler = new ClientPooler(subscription, sessionId, name);
        this.flux = new AeronFlux(FluxProcessor.create(emitter -> {
            pooler.setFluxSink(emitter);
            pooler.initialise();
        }));
    }

    @Override
    public AeronFlux receive() {
        return flux;
    }

    @Override
    public void dispose() {
        pooler.shutdown();
    }

}