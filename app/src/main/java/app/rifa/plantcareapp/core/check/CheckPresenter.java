package app.rifa.plantcareapp.core.check;

import app.rifa.plantcareapp.base.BasePresenter;

public class CheckPresenter extends BasePresenter implements CheckContract.Presenter, CheckContract.Listener {

    private final CheckContract.View checkView;

    public CheckPresenter(CheckContract.View checkView) {
        super(checkView);
        this.checkView = checkView;
    }
}