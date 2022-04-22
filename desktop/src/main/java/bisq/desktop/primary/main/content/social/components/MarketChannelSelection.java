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

package bisq.desktop.primary.main.content.social.components;

import bisq.application.DefaultApplicationService;
import bisq.desktop.common.observable.FxBindings;
import bisq.desktop.common.utils.Icons;
import bisq.desktop.overlay.OverlayWindow;
import bisq.i18n.Res;
import bisq.social.chat.Channel;
import bisq.social.chat.MarketChannel;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MarketChannelSelection extends ChannelSelection {
    private final Controller controller;

    public MarketChannelSelection(DefaultApplicationService applicationService) {
        controller = new Controller(applicationService);
    }

    public Pane getRoot() {
        return controller.view.getRoot();
    }

    protected static class Controller extends bisq.desktop.primary.main.content.social.components.ChannelSelection.Controller {
        private final Model model;
        @Getter
        private final View view;
        private final MarketChannelsChooser marketChannelsChooser;

        protected Controller(DefaultApplicationService applicationService) {
            super(applicationService.getChatService());

            marketChannelsChooser = new MarketChannelsChooser(chatService, applicationService.getSettingsService());
            model = new Model();
            view = new View(model, this, marketChannelsChooser.getRoot());
        }

        @Override
        protected ChannelSelection.Model getChannelSelectionModel() {
            return model;
        }

        @Override
        public void onActivate() {
            super.onActivate();
            channelsPin = FxBindings.<MarketChannel, Channel<?>>bind(model.channels)
                    .to(chatService.getMarketChannels());

            selectedChannelPin = FxBindings.subscribe(chatService.getSelectedChannel(),
                    channel -> {
                        if (channel instanceof MarketChannel) {
                            model.selectedChannel.set(channel);
                        }
                    });
        }

        public void onOpenMarketsChannelChooser() {
            new OverlayWindow(view.getRoot(), marketChannelsChooser.getRoot()).show();
        }
    }

    protected static class Model extends bisq.desktop.primary.main.content.social.components.ChannelSelection.Model {
    }

    protected static class View extends bisq.desktop.primary.main.content.social.components.ChannelSelection.View<Model, Controller> {
        private final Label plusIcon;
        private final ChangeListener<Number> widthListener;

        protected View(Model model, Controller controller, Pane marketChannelsChooser) {
            super(model, controller);

            plusIcon = Icons.getIcon(AwesomeIcon.PLUS_SIGN_ALT, "14");
            plusIcon.setOpacity(0.6);
            plusIcon.setLayoutY(15);
            plusIcon.setCursor(Cursor.HAND);
            titledPaneContainer.getChildren().add(plusIcon);
            widthListener = (observableValue, oldValue, newValue) -> layoutIcon();
        }

        @Override
        protected void onViewAttached() {
            super.onViewAttached();

            layoutIcon();
            plusIcon.setOnMouseClicked(e -> controller.onOpenMarketsChannelChooser());
            titledPaneContainer.widthProperty().addListener(widthListener);
        }

        @Override
        protected void onViewDetached() {
            titledPaneContainer.widthProperty().removeListener(widthListener);
            plusIcon.setOnMouseClicked(null);
        }

        @Override
        protected String getHeadlineText() {
            return Res.get("social.marketChannels");
        }

        private void layoutIcon() {
            plusIcon.setLayoutX(root.getWidth() - 25);
        }

    }
}