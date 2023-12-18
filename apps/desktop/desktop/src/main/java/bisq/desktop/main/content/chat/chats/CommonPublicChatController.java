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

package bisq.desktop.main.content.chat.chats;

import bisq.bisq_easy.NavigationTarget;
import bisq.chat.ChatChannelDomain;
import bisq.desktop.ServiceProvider;
import bisq.desktop.main.content.chat.ChatController;
import bisq.desktop.main.content.chat.common.ChatToolbox;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class CommonPublicChatController extends ChatController<CommonPublicChatView, CommonPublicChatModel> {
    public CommonPublicChatController(ServiceProvider serviceProvider,
                                      ChatChannelDomain chatChannelDomain,
                                      NavigationTarget navigationTarget,
                                      Optional<ChatToolbox> toolbox) {
        super(serviceProvider, chatChannelDomain, navigationTarget, toolbox);
    }

    @Override
    public CommonPublicChatModel createAndGetModel(ChatChannelDomain chatChannelDomain) {
        return new CommonPublicChatModel(chatChannelDomain);
    }

    @Override
    public CommonPublicChatView createAndGetView() {
        return new CommonPublicChatView(model, this, chatMessagesComponent.getRoot(), channelSidebar.getRoot());
    }
}
