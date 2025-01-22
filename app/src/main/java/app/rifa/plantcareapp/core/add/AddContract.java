package app.rifa.plantcareapp.core.add;

import app.rifa.plantcareapp.base.BaseListenerContract;
import app.rifa.plantcareapp.base.BasePresenterContract;
import app.rifa.plantcareapp.base.BaseViewContract;
import app.rifa.plantcareapp.model.UserPlant;

public interface AddContract {
    interface View extends BaseViewContract {
        void setNameError(int error);

        void plantAdded(int message, UserPlant plant);
    }

    interface Presenter extends BasePresenterContract {
        void addPlant(UserPlant plant);
    }

    interface Interactor {
        void performAddPlant(UserPlant plant);
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(int message, UserPlant plant);
        void onFailure(String message);
    }
}