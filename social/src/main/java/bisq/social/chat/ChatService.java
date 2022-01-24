/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.social.chat;

import bisq.common.util.StringUtils;
import bisq.identity.Identity;
import bisq.identity.IdentityService;
import bisq.network.NetworkService;
import bisq.network.p2p.message.Message;
import bisq.network.p2p.services.confidential.MessageListener;
import bisq.persistence.Persistence;
import bisq.persistence.PersistenceClient;
import bisq.persistence.PersistenceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Manages chatChannels and persistence of the chatModel.
 * ChatUser and ChatIdentity management is not implemented yet. Not 100% clear yet if ChatIdentity management should
 * be rather part of the identity module.
 */
@Slf4j
@Getter
public class ChatService implements PersistenceClient<ChatStore>, MessageListener {
    public interface Listener {
        void onChannelAdded(Channel channel);

        void onChannelSelected(Channel channel);

        void onChatMessageAdded(Channel channel, ChatMessage newChatMessage);
    }

    private final PersistenceService persistenceService;
    private final IdentityService identityService;
    private final NetworkService networkService;
    private final ChatStore chatStore = new ChatStore();
    private final Persistence<ChatStore> persistence;
    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();

    public ChatService(PersistenceService persistenceService, IdentityService identityService, NetworkService networkService) {
        this.persistenceService = persistenceService;
        this.identityService = identityService;
        this.networkService = networkService;
        persistence = persistenceService.getOrCreatePersistence(this, "db", chatStore);

        networkService.addMessageListener(this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // PersistenceClient
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void applyPersisted(ChatStore persisted) {
        synchronized (chatStore) {
            chatStore.applyPersisted(persisted);
        }
    }

    @Override
    public ChatStore getClone() {
        synchronized (chatStore) {
            return chatStore.getClone();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // MessageListener
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onMessage(Message message) {
        if (message instanceof ChatMessage chatMessage) {
            String domainId = chatMessage.getChannelId();
            String userName = findUserName(domainId).orElse("Maker@" + StringUtils.truncate(domainId, 8));
            ChatIdentity chatIdentity = getOrCreateChatIdentity(userName, domainId);
            ChatPeer chatPeer = new ChatPeer(chatMessage.getSenderUserName(), chatMessage.getSenderNetworkId());
            PrivateChannel privateChannel = getOrCreatePrivateChannel(chatMessage.getChannelId(),
                    chatPeer,
                    chatIdentity);
            addChatMessage(chatMessage, privateChannel);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Channels
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public PrivateChannel getOrCreatePrivateChannel(String id, ChatPeer chatPeer, ChatIdentity chatIdentity) {
        PrivateChannel privateChannel = new PrivateChannel(id, chatPeer, chatIdentity);
        Optional<PrivateChannel> previousChannel;
        synchronized (chatStore) {
            previousChannel = chatStore.findPrivateChannel(id);
            if (previousChannel.isEmpty()) {
                chatStore.getPrivateChannels().add(privateChannel);
            }
        }
        if (previousChannel.isEmpty()) {
            persist();
            listeners.forEach(listener -> listener.onChannelAdded(privateChannel));
            return privateChannel;
        } else {
            return previousChannel.get();
        }
    }

    public void selectChannel(Channel channel) {
        chatStore.setSelectedChannel(channel);
        persist();
        listeners.forEach(listener -> listener.onChannelSelected(channel));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ChatMessage
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public void addChatMessage(ChatMessage chatMessage, Channel privateChannel) {
        synchronized (chatStore) {
            privateChannel.addChatMessage(chatMessage);
        }
        persist();
        listeners.forEach(listener -> listener.onChatMessageAdded(privateChannel, chatMessage));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ChatIdentity
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public Optional<ChatIdentity> findChatIdentity(String domainId) {
        if (chatStore.getUserNameByDomainId().containsKey(domainId)) {
            String userName = chatStore.getUserNameByDomainId().get(domainId);
            Identity identity = identityService.getOrCreateIdentity(domainId).join();
            return Optional.of(new ChatIdentity(userName, identity));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> findUserName(String domainId) {
        //todo add mapping strategy
        return Optional.ofNullable(chatStore.getUserNameByDomainId().get(domainId));
    }

    public ChatIdentity getOrCreateChatIdentity(String userName, String domainId) {
        synchronized (chatStore) {
            if (chatStore.getUserNameByDomainId().containsKey(domainId)) {
                checkArgument(chatStore.getUserNameByDomainId().get(domainId).equals(userName));
            } else {
                chatStore.getUserNameByDomainId().put(domainId, userName);
            }
        }
        persist();
        Identity identity = identityService.getOrCreateIdentity(domainId).join(); //todo
        return new ChatIdentity(userName, identity);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}