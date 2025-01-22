package app.rifa.plantcareapp.core.add;

import static app.rifa.plantcareapp.base.App.context;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import app.rifa.plantcareapp.R;
import app.rifa.plantcareapp.model.UserPlant;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddInteractor implements AddContract.Interactor {

    AddContract.Listener addListener;

    public AddInteractor(AddContract.Listener addListener) {
        this.addListener = addListener;
    }

    @Override
    public void performAddPlant(UserPlant plant) {

        addListener.onStart();
        if (plant.getImage() != null) {
            addPlantWithImage(plant);
        } else {
            addPlant(plant);
        }
    }

    private void addPlantWithImage(UserPlant plant) {
        String clientId = "2c6dd1452c7cb9c"; // Замените на ваш Client ID от Imgur

        try {
            File file = getFileFromUri(Uri.parse(plant.getImage()), context);

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

            ImgurApi imgurApi = ImgurClient.getClient().create(ImgurApi.class);
            Call<ImgurResponse> call = imgurApi.uploadImage("Client-ID " + clientId, body);

            call.enqueue(new Callback<ImgurResponse>() {
                @Override
                public void onResponse(Call<ImgurResponse> call, Response<ImgurResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body().data.link;
                        Log.d("ImgurUpload", "Изображение загружено успешно: " + imageUrl);
                        plant.setImage(imageUrl); // Устанавливаем URL изображения
                        addPlant(plant); // Продолжаем добавление растения
                    } else {
                        Log.e("ImgurUpload", "Ошибка загрузки изображения: " + response.message());
                        addListener.onFailure("Ошибка загрузки изображения: " + response.message());
                        addListener.onEnd();
                    }
                }

                @Override
                public void onFailure(Call<ImgurResponse> call, Throwable t) {
                    Log.e("ImgurUpload", "Ошибка загрузки: " + t.getMessage());
                    addListener.onFailure("Ошибка загрузки изображения: " + t.getMessage());
                    addListener.onEnd();
                }
            });

        } catch (IOException e) {
            Log.e("ImgurUpload", "Ошибка преобразования URI: " + e.getMessage());
            addListener.onFailure("Не удалось обработать изображение");
            addListener.onEnd();
        }
    }


    private File getFileFromUri(Uri uri, Context context) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);

        // Создаём временный файл
        File tempFile = File.createTempFile("temp_image", ".jpg", context.getCacheDir());
        tempFile.deleteOnExit(); // Удаляем файл после использования

        // Записываем поток данных в файл
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (inputStream != null) inputStream.close();
        }

        return tempFile;
    }

    private void addPlant(UserPlant plant) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("UserPlants");

        databaseReference.child(plant.getId()).setValue(plant)
                .addOnSuccessListener(task -> {
                    Log.d("Database", "Растение успешно добавлено: " + plant.getId());
                    addListener.onEnd();
                    addListener.onSuccess(R.string.db_plant_added, plant);
                })
                .addOnFailureListener(error -> {
                    Log.e("Database", "Ошибка при добавлении растения: " + error.getMessage());
                    addListener.onEnd();
                    addListener.onFailure(error.getMessage());
                });
    }
}