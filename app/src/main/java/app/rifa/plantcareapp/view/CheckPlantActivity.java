package app.rifa.plantcareapp.view;

import static app.rifa.plantcareapp.utils.Constants.PLANT_INTENT_EXTRAS_KEY;
import static app.rifa.plantcareapp.utils.FirebaseConstants.FIREBASE_IMAGE_REFERENCE;
import static app.rifa.plantcareapp.utils.ProgressUtils.getProgressBarFill;
import static app.rifa.plantcareapp.utils.TimeUtils.getCurrentDate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import app.rifa.plantcareapp.R;
import app.rifa.plantcareapp.base.BaseActivity;
import app.rifa.plantcareapp.core.check.CheckContract;
import app.rifa.plantcareapp.core.check.CheckPresenter;
import app.rifa.plantcareapp.core.myplants.MyPlantsInteractor;
import app.rifa.plantcareapp.databinding.ActivityCheckPlantBinding;
import app.rifa.plantcareapp.model.Plant;
import app.rifa.plantcareapp.model.UserPlant;
import app.rifa.plantcareapp.view.adapter.AdviceAdapter;
import app.rifa.plantcareapp.view.adapter.MyPlantsAdapter;

public class CheckPlantActivity extends BaseActivity implements CheckContract.View {

    private ActivityCheckPlantBinding binding;
    private CheckContract.Presenter presenter;
    private Plant plant;
    private LinearLayoutManager advicesLayoutManager;
    private AdviceAdapter checkPlantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCheckPlantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        plant = (Plant) getIntent().getSerializableExtra(PLANT_INTENT_EXTRAS_KEY);
        presenter = new CheckPresenter(this);

        init();

        binding.btAddPlant.setOnClickListener(v -> openAddActivity());
    }

    private void init() {
        setPlantPhoto();

        if (plant != null) {
            binding.tvCommonName.setText(plant.getCommonName());
            binding.tvLatinName.setText(plant.getLatinName());
            binding.tvCategory.setText(plant.getType());

            binding.tvDescription.setText(plant.getDescription());

            initWateringFrequency(plant.getWateringFrequency());
            initFertilizingFrequency(plant.getFertilizingFrequency());
            initSprayingFrequency(plant.getSprayingFrequency());

            binding.btAddPlant.setOnClickListener(v -> openAddActivity());

            if (plant.getAdvicesList() == null || plant.getAdvicesList().size() == 0) {
                binding.tvAdvices.setVisibility(View.GONE);
                binding.llAdvices.setVisibility(View.GONE);
            }

            initAdapter();
        }
    }

    private void initAdapter() {
        advicesLayoutManager = new LinearLayoutManager(this);
        binding.rvAdvices.setLayoutManager(advicesLayoutManager);
        checkPlantAdapter = new AdviceAdapter(this, plant.getAdvicesList());
        binding.rvAdvices.setAdapter(checkPlantAdapter);
    }

    private void openAddActivity() {
        // Создаем объект UserPlant, который будем добавлять в базу данных
        UserPlant userPlant = new UserPlant();
        String now = getCurrentDate();
        // Заполняем объект userPlant данными
        userPlant.setName(plant.getCommonName());
        userPlant.setWateringFrequency(plant.getWateringFrequency());
        userPlant.setFertilizingFrequency(plant.getFertilizingFrequency());
        userPlant.setSprayingFrequency(plant.getSprayingFrequency());
        userPlant.setLastWatering(now); // или текущая дата
        userPlant.setLastFertilizing(now); // или текущая дата
        userPlant.setLastSpraying(now); // или текущая дата
        userPlant.setImage(plant.getImage()); // Ссылка на картинку

        // Показываем прогресс-бар
        showLoading();

        // Добавление растения в базу данных с использованием слушателя
        MyPlantsInteractor.performAddPlant(userPlant, new MyPlantsInteractor.OnPlantAddedListener() {
            @Override
            public void onSuccess() {
                // Прячем прогресс-бар перед переходом
                hideLoading();

                // После того как растение добавлено, переходим на главную страницу
                Intent intent = new Intent(CheckPlantActivity.this, MainActivity.class);
                intent.putExtra(PLANT_INTENT_EXTRAS_KEY, plant);  // передаем объект 'plant' если нужно
                startActivity(intent);
                finish();  // Завершаем текущую активность, если нужно, чтобы не возвращаться назад
            }

            @Override
            public void onFailure(String error) {
                // Прячем прогресс-бар в случае ошибки
                hideLoading();

                // Обработка ошибки, если добавление не удалось
                Toast.makeText(CheckPlantActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }




    public void initWateringFrequency(long wateringFrequency) {
        if (wateringFrequency != 0) {
            binding.tvWateringDays.setText(getString(R.string.days, wateringFrequency));
            binding.pbWater.setProgress((int) getProgressBarFill(wateringFrequency));
        } else {
            binding.tvWateringDays.setText(getString(R.string.never));
            binding.pbWater.setProgress(0);
        }
    }

    public void initFertilizingFrequency(long fertilizingFrequency) {
        if (fertilizingFrequency != 0) {
            binding.tvFertilizingDays.setText(getString(R.string.days, fertilizingFrequency));
            binding.pbFertilizer.setProgress((int) getProgressBarFill(fertilizingFrequency));
        } else {
            binding.tvFertilizingDays.setText(getString(R.string.never));
            binding.pbFertilizer.setProgress(0);
        }
    }

    public void initSprayingFrequency(long sprayingFrequency) {
        if (sprayingFrequency != 0) {
            binding.tvSprayingDays.setText(getString(R.string.days, sprayingFrequency));
            binding.pbSpraying.setProgress((int) getProgressBarFill(sprayingFrequency));
        } else {
            binding.tvSprayingDays.setText(getString(R.string.never));
            binding.pbSpraying.setProgress(0);
        }
    }

    public void setPlantPhoto() {
        if (plant.getImage() != null) {
            Glide
                    .with(this)
                    .load(plant.getImage())  // Используем прямую ссылку на картинку с Imgur
                    .into(binding.ivPhoto);
        }
    }
}