package app.rifa.plantcareapp.view;

import static app.rifa.plantcareapp.utils.Constants.PERMISSION_CAMERA;
import static app.rifa.plantcareapp.utils.Constants.PERMISSION_STORAGE;
import static app.rifa.plantcareapp.utils.Constants.PICK_IMAGE_CAMERA;
import static app.rifa.plantcareapp.utils.Constants.PICK_IMAGE_GALLERY;
import static app.rifa.plantcareapp.utils.Constants.PLANT_INTENT_EXTRAS_KEY;
import static app.rifa.plantcareapp.utils.Constants.WRITE_EXTERNAL_STORAGE;
import static app.rifa.plantcareapp.utils.ProgressUtils.daysToProgress;
import static app.rifa.plantcareapp.utils.ProgressUtils.progressToDays;
import static app.rifa.plantcareapp.utils.SeekBarUtils.initSeekBarGroupWithText;
import static app.rifa.plantcareapp.utils.TimeUtils.getCurrentDate;
import static app.rifa.plantcareapp.utils.TimeUtils.getTimestamp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import app.rifa.plantcareapp.R;
import app.rifa.plantcareapp.base.BaseFragment;
import app.rifa.plantcareapp.core.add.AddContract;
import app.rifa.plantcareapp.core.add.AddPresenter;
import app.rifa.plantcareapp.databinding.FragmentAddPlantBinding;
import app.rifa.plantcareapp.model.Plant;
import app.rifa.plantcareapp.model.UserPlant;

public class AddPlantFragment extends BaseFragment implements AddContract.View {

    FragmentAddPlantBinding binding;
    AddContract.Presenter presenter;

    private Uri photoURI;

    // Константы для разрешений и результатов
    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_STORAGE = 2;
    private static final int WRITE_EXTERNAL_STORAGE = 3;
    private static final int PICK_IMAGE_CAMERA = 4;
    private static final int PICK_IMAGE_GALLERY = 5;

    // Запуск фото с камеры
    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    binding.ivPhoto.setImageURI(photoURI);
                }
            }
    );

    // Запуск выбора фото из галереи
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    photoURI = result.getData().getData();
                    binding.ivPhoto.setImageURI(photoURI);
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPlantBinding.inflate(getLayoutInflater());
        presenter = new AddPresenter(this);

        init();

        return binding.getRoot();
    }

    private void init() {
        initSeekBars();
        initButtons();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Plant plant = (Plant) bundle.getSerializable(PLANT_INTENT_EXTRAS_KEY);
            initPlantData(plant);
        }
    }

    private void initPlantData(Plant plant) {
        binding.etName.setText(plant.getCommonName());

        binding.fertilizingSettings.sbFrequency.setProgress(daysToProgress(plant.getFertilizingFrequency()));
        binding.wateringSettings.sbFrequency.setProgress(daysToProgress(plant.getWateringFrequency()));
        binding.sprayingSettings.sbFrequency.setProgress(daysToProgress(plant.getSprayingFrequency()));
    }

    private void initSeekBars() {
        initSeekBarGroupWithText(
                getContext(),
                binding.wateringSettings.sbFrequency,
                binding.wateringSettings.tvFrequency,
                binding.wateringSettings.ivPlus,
                binding.wateringSettings.ivMinus,
                10
        );
        initSeekBarGroupWithText(
                getContext(),
                binding.fertilizingSettings.sbFrequency,
                binding.fertilizingSettings.tvFrequency,
                binding.fertilizingSettings.ivPlus,
                binding.fertilizingSettings.ivMinus,
                30
        );
        initSeekBarGroupWithText(
                getContext(),
                binding.sprayingSettings.sbFrequency,
                binding.sprayingSettings.tvFrequency,
                binding.sprayingSettings.ivPlus,
                binding.sprayingSettings.ivMinus,
                0
        );
    }

    private void initButtons() {
        binding.ivPhoto.setOnClickListener(v -> showChoosePhotoDialog());

        binding.toolbar.btAddPlant.setOnClickListener(v -> {
            String photoURIString = null;
            if(photoURI != null) {
                photoURIString = photoURI.toString();
            }

            presenter.addPlant(
                    new UserPlant(
                            "" + getTimestamp(),
                            binding.etName.getText().toString(),
                            progressToDays(binding.wateringSettings.sbFrequency.getProgress()),
                            progressToDays(binding.fertilizingSettings.sbFrequency.getProgress()),
                            progressToDays(binding.sprayingSettings.sbFrequency.getProgress()),
                            getCurrentDate(),
                            getCurrentDate(),
                            getCurrentDate(),
                            photoURIString
                    )
            );
        });
    }

    public void setNameError(int error) {
        binding.etName.setError(getString(error));
    }

    public void plantAdded(int message, UserPlant plant) {
        showMessage(message);
    }

    public void setPlantPhoto(String uri) {
        Glide
                .with(this)
                .load(uri)
                .into(binding.ivPhoto);
    }

    private void showChoosePhotoDialog() {
        final CharSequence[] options = getResources().getStringArray(R.array.photo_options);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.choose_photo);

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals(getString(R.string.take_photo))) {
                tryPickPhotoFromCamera();

            } else if (options[item].equals(getString(R.string.gallery))) {
                tryPickPhotoFromGallery();

            } else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void tryPickPhotoFromCamera() {
        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Пояснение пользователю, почему нужно разрешение
                new AlertDialog.Builder(getContext())
                        .setMessage("Разрешение не предоставлено")
                        .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA))
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            }
        } else {
            pickPhotoFromCamera();
        }
    }

    private void tryPickPhotoFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickPhotoFromGallery(); // Новый подход через Photo Picker
        } else {
            Intent storageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            storageIntent.setType("image/*");
            galleryLauncher.launch(storageIntent);
        }
    }

    private void pickPhotoFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "new picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");
        photoURI = getActivity().getApplication().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        cameraLauncher.launch(cameraIntent);
    }

    private void pickPhotoFromGallery() {
        Intent storageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        storageIntent.setType("image/*");
        galleryLauncher.launch(storageIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPhotoFromCamera();
                } else {
                    showMessage(R.string.permissions_denied);
                }
                break;
            }
            case PERMISSION_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPhotoFromGallery();
                } else {
                    showMessage(R.string.permissions_denied);
                }
                break;
            }
            case WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPhotoFromCamera();
                } else {
                    showMessage(R.string.permissions_denied);
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_GALLERY:
                    assert data != null;
                    photoURI = data.getData();
                    binding.ivPhoto.setImageURI(photoURI);
                    break;
                case PICK_IMAGE_CAMERA:
                    binding.ivPhoto.setImageURI(photoURI);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
