package app.rifa.plantcareapp.core.main;

import app.rifa.plantcareapp.base.BaseListenerContract;
import app.rifa.plantcareapp.base.BasePresenterContract;
import app.rifa.plantcareapp.base.BaseViewContract;
import app.rifa.plantcareapp.model.User;

public interface MainContract {
    interface View extends BaseViewContract {
        void requireLogin();

        void setUser(User username);

        void setEmail(String email);
    }

    interface Presenter extends BasePresenterContract {
        void checkIfUserIsLoggedIn();
        void onDrawerOptionLogoutClick();
    }

    interface Interactor {
        void performGetUserData();

        void performLogout();
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(User user);

        void onFailure();
    }
}
