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

package bisq.desktop.primary.main.content.user.user_profile.create.step1;

import bisq.application.DefaultApplicationService;
import bisq.desktop.common.view.Navigation;
import bisq.desktop.common.view.NavigationTarget;
import bisq.desktop.primary.main.content.user.user_profile.create.step2.CreateNewProfileStep2Controller;
import bisq.desktop.primary.overlay.OverlayController;
import bisq.desktop.primary.overlay.onboarding.create_profile.CreateProfileController;
import bisq.desktop.primary.overlay.onboarding.create_profile.CreateProfileModel;
import bisq.desktop.primary.overlay.onboarding.create_profile.CreateProfileView;
import javafx.application.Platform;

public class CreateNewProfileStep1Controller extends CreateProfileController {
    private final DefaultApplicationService applicationService;

    public CreateNewProfileStep1Controller(DefaultApplicationService applicationService) {
        super(applicationService);
        this.applicationService = applicationService;
    }

    @Override
    protected CreateProfileView getGenerateProfileView() {
        return new CreateNewProfileStep1View((CreateNewProfileStep1Model) model, this);
    }

    @Override
    protected CreateProfileModel getGenerateProfileModel() {
        return new CreateNewProfileStep1Model();
    }

    @Override
    protected void onCreateUserProfile() {
        CreateNewProfileStep2Controller.InitData initData = new CreateNewProfileStep2Controller.InitData(
                model.getKeyPairAndId(),
                model.getPooledIdentity(),
                model.getProofOfWork().orElseThrow(),
                model.getNickName().get(),
                model.getNym().get());
        Navigation.navigateTo(NavigationTarget.CREATE_PROFILE_STEP2, initData);
    }

    void onCancel() {
        OverlayController.hide();
    }

    void onQuit() {
        applicationService.shutdown().thenAccept(result -> Platform.exit());
    }
}