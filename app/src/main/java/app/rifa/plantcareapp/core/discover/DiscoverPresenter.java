package app.rifa.plantcareapp.core.discover;

import app.rifa.plantcareapp.base.BasePresenter;
import app.rifa.plantcareapp.model.Plant;

import java.util.List;

public class DiscoverPresenter extends BasePresenter
        implements DiscoverContract.Presenter, DiscoverContract.Listener {

    private DiscoverContract.View discoverView;
    private DiscoverContract.Interactor discoverInteractor;

    public DiscoverPresenter(DiscoverContract.View discoverView) {
        super(discoverView);
        this.discoverView = discoverView;
        this.discoverInteractor = new DiscoverInteractor(this);
    }

    @Override
    public void getAllPlants() {
        discoverInteractor.performGetAllPlants();
    }

    @Override
    public void getMatchingPlants(String regex) {
        discoverInteractor.performGetMatchingPlants(regex);
    }

    @Override
    public void onSuccess(List<Plant> plantList) {
        discoverView.setDiscoverPlantList(plantList);
    }

    @Override
    public void onFailure(String message) {
        discoverView.showMessage(message);
    }
}
