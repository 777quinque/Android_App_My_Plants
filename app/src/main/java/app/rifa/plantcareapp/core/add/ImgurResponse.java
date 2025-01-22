package app.rifa.plantcareapp.core.add;

public class ImgurResponse {
    public Data data;
    public boolean success;
    public int status;

    public static class Data {
        public String link; // Ссылка на загруженное изображение
    }
}
