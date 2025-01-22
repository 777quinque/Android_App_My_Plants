package app.rifa.plantcareapp.core.myplants;

import app.rifa.plantcareapp.base.BaseListenerContract;
import app.rifa.plantcareapp.base.BasePresenterContract;
import app.rifa.plantcareapp.base.BaseViewContract;
import app.rifa.plantcareapp.model.UserPlant;

import java.util.List;

public interface MyPlantsContract {
    interface View extends BaseViewContract {
        void updateMyPlants(List<UserPlant> plantList);

        void plantDeleted(String id);
    }

    interface Presenter extends BasePresenterContract {
        void getMyPlantsList();

        void deletePlant(UserPlant plant);

        void updatePlantNeeds(UserPlant plant, boolean isWatered, boolean isFertilized, boolean isSprayed);
    }

    interface Interactor {
        void fetchMyPlantList();

        void performDeletePlant(UserPlant plant);

        void performUpdatePlantNeeds(UserPlant plant, boolean isWatered, boolean isFertilized, boolean isSprayed);
    }

    interface Listener extends BaseListenerContract {
        void onSuccess(List<UserPlant> plantList);

        void onFailure(String message);

        void onSuccess(int message);
        
        void onSuccess(UserPlant plant);

        void onPlantDeleted(String id);
    }
}
