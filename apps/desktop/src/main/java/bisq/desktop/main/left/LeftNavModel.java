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

package bisq.desktop.main.left;

import bisq.bisq_easy.NavigationTarget;
import bisq.desktop.ServiceProvider;
import bisq.desktop.common.threading.UIThread;
import bisq.desktop.common.view.Model;
import bisq.network.NetworkService;
import bisq.network.common.TransportType;
import bisq.network.identity.NetworkId;
import bisq.network.p2p.message.EnvelopePayloadMessage;
import bisq.network.p2p.node.CloseReason;
import bisq.network.p2p.node.Connection;
import bisq.network.p2p.node.Node;
import bisq.network.p2p.services.peergroup.PeerGroupService;
import bisq.settings.SettingsService;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Getter
public class LeftNavModel implements Model {
    private final boolean isWalletEnabled;
    private final NetworkService networkService;
    private final SettingsService settingsService;
    private final Set<NavigationTarget> navigationTargets = new HashSet<>();
    private final List<LeftNavButton> leftNavButtons = new ArrayList<>();
    private final ObjectProperty<NavigationTarget> selectedNavigationTarget = new SimpleObjectProperty<>();
    private final ObjectProperty<LeftNavButton> selectedNavigationButton = new SimpleObjectProperty<>();

    private final StringProperty torNumConnections = new SimpleStringProperty("0");
    private final StringProperty torNumTargetConnections = new SimpleStringProperty("0");
    private final BooleanProperty torEnabled = new SimpleBooleanProperty(false);
    private final StringProperty i2pNumConnections = new SimpleStringProperty("0");
    private final StringProperty i2pNumTargetConnections = new SimpleStringProperty("0");
    private final BooleanProperty i2pEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty menuHorizontalExpanded = new SimpleBooleanProperty();
    private final BooleanProperty authorizedRoleVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty newVersionAvailable = new SimpleBooleanProperty(false);
    @Setter
    private String version;

    public LeftNavModel(ServiceProvider serviceProvider) {
        isWalletEnabled = serviceProvider.getWalletService().isPresent();
        networkService = serviceProvider.getNetworkService();
        settingsService = serviceProvider.getSettingsService();

        torEnabled.set(networkService.isTransportTypeSupported(TransportType.TOR));
        i2pEnabled.set(networkService.isTransportTypeSupported(TransportType.I2P));

        networkService.getSupportedTransportTypes().forEach(type ->
                networkService.getServiceNodesByTransport().findServiceNode(type).ifPresent(serviceNode -> {
                    serviceNode.getPeerGroupManager().ifPresent(peerGroupManager -> {
                        PeerGroupService peerGroupService = peerGroupManager.getPeerGroupService();
                        switch (type) {
                            case TOR:
                                torNumTargetConnections.set(String.valueOf(peerGroupService.getTargetNumConnectedPeers()));
                                break;
                            case I2P:
                                i2pNumTargetConnections.set(String.valueOf(peerGroupService.getTargetNumConnectedPeers()));
                                break;
                        }

                        Node defaultNode = serviceNode.getDefaultNode();
                        defaultNode.addListener(new Node.Listener() {
                            @Override
                            public void onMessage(EnvelopePayloadMessage envelopePayloadMessage, Connection connection, NetworkId networkId) {
                            }

                            @Override
                            public void onConnection(Connection connection) {
                                onNumConnectionsChanged(type, peerGroupService);
                            }

                            @Override
                            public void onDisconnect(Connection connection, CloseReason closeReason) {
                                onNumConnectionsChanged(type, peerGroupService);
                            }
                        });
                    });
                })
        );
    }

    private void onNumConnectionsChanged(TransportType transportType, PeerGroupService peerGroupService) {
        UIThread.run(() -> {
            switch (transportType) {
                case TOR:
                    torNumConnections.set(String.valueOf(peerGroupService.getNumConnections()));
                    break;
                case I2P:
                    i2pNumConnections.set(String.valueOf(peerGroupService.getNumConnections()));
                    break;
            }
        });
    }


}