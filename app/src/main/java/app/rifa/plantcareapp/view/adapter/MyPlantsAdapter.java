package app.rifa.plantcareapp.view.adapter;

import static app.rifa.plantcareapp.utils.AlertDialogUtils.showDeletePlantDialog;
import static app.rifa.plantcareapp.utils.ProgressUtils.daysFromLastAction;
import static app.rifa.plantcareapp.utils.TimeUtils.DATE_FORMAT;
import static app.rifa.plantcareapp.utils.FirebaseConstants.FIREBASE_IMAGE_REFERENCE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import app.rifa.plantcareapp.R;
import app.rifa.plantcareapp.core.myplants.MyPlantsContract;
import app.rifa.plantcareapp.databinding.ItemMyPlantBinding;
import app.rifa.plantcareapp.model.UserPlant;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class MyPlantsAdapter extends RecyclerView.Adapter<MyPlantsAdapter.ViewHolder> {

    private final Context context;
    private List<UserPlant> plantList;
    private MyPlantsContract.Presenter presenter;

    public MyPlantsAdapter(Context context, MyPlantsContract.Presenter presenter, List<UserPlant> plantList) {
        this.context = context;
        this.plantList = plantList;
        this.presenter = presenter;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyPlantBinding binding;

        public ViewHolder(ItemMyPlantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemCount() {
        if (plantList == null) {
            return 0;
        } else {
            return plantList.size();
        }
    }

    @NonNull
    @Override
    public MyPlantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemMyPlantBinding.inflate(LayoutInflater.from(context),
                        parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantsAdapter.ViewHolder holder, int position) {

        UserPlant plant = plantList.get(position);
        holder.binding.tvName.setText(plant.getName());
        initPlantPhoto(holder, plant);

        if (plant.isCareNeeded()) holder.binding.warning.setVisibility(View.VISIBLE);
        else holder.binding.warning.setVisibility(View.INVISIBLE);

        holder.binding.main.setOnClickListener(v -> changeExpandableVisibility(holder));
        holder.binding.btDelete.setOnClickListener(v -> showDeletePlantDialog(context, presenter, plant));
        holder.binding.btConfirm.setOnClickListener(v -> {
            boolean isWatered;
            boolean isFertilized;
            boolean isSprayed;

            isWatered = holder.binding.cbWatering.isChecked();
            isFertilized = holder.binding.cbFertilizing.isChecked();
            isSprayed = holder.binding.cbSpraying.isChecked();

            presenter.updatePlantNeeds(plant, isWatered, isFertilized, isSprayed);
            changeExpandableVisibility(holder);
        });

        initProgressBars(holder, plant);
    }

    private void initProgressBars(@NonNull ViewHolder holder, UserPlant plant) {
        try {
            if (plant.getWateringFrequency() > 0) {
                holder.binding.rlWater.setVisibility(View.VISIBLE);
                initProgressBar(holder.binding.tvWater,
                        holder.binding.pbWater,
                        plant.getWateringFrequency(),
                        DATE_FORMAT.parse(plant.getLastWatering()));
            } else {
                holder.binding.rlWater.setVisibility(View.GONE);
            }

            if (plant.getFertilizingFrequency() > 0) {
                holder.binding.rlFertilization.setVisibility(View.VISIBLE);
                initProgressBar(holder.binding.tvFertilization,
                        holder.binding.pbFertilizer,
                        plant.getFertilizingFrequency(),
                        DATE_FORMAT.parse(plant.getLastFertilizing()));
            } else {
                holder.binding.rlFertilization.setVisibility(View.GONE);
            }

            if (plant.getSprayingFrequency() > 0) {
                holder.binding.rlSpraying.setVisibility(View.VISIBLE);
                initProgressBar(holder.binding.tvSpraying,
                        holder.binding.pbSpraying,
                        plant.getSprayingFrequency(),
                        DATE_FORMAT.parse(plant.getLastSpraying()));
            } else {
                holder.binding.rlSpraying.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void updatePlantList(List<UserPlant> newPlantList) {
        this.plantList = newPlantList;
        notifyDataSetChanged();  // Notify the adapter that the data set has changed
    }

    private void initProgressBar(TextView text, ProgressBar progressBar, long frequency, Date lastAction) {
        long daysFromLastAction = daysFromLastAction(lastAction);
        long fill = 100 - daysFromLastAction * 100 / frequency;
        long daysLeft = frequency - daysFromLastAction;
        progressBar.setProgress((int) fill);
        if (daysLeft > 1) {
            text.setText("In " + daysLeft + " days");
        }
        if (daysLeft == 1) {
            text.setText("In 1 day");
        }
        if (daysLeft < 1) {
            text.setText("Today");
        }
    }

    private void changeExpandableVisibility(@NonNull ViewHolder holder) {
        if (holder.binding.expandable.getVisibility() == View.VISIBLE) {
            holder.binding.expandable.setVisibility(View.GONE);
            holder.binding.btExpand.setImageDrawable(context.getDrawable(R.drawable.ic_down));
        } else {
            holder.binding.expandable.setVisibility(View.VISIBLE);
            holder.binding.btExpand.setImageDrawable(context.getDrawable(R.drawable.ic_up));
        }
    }

    private void initPlantPhoto(@NonNull ViewHolder holder, UserPlant plant) {
        if (plant.getImage() != null) {
            Glide
                    .with(context)
                    .load(plant.getImage()) // Загружаем по URL
                    .into(holder.binding.ivPhoto);
        } else {
            Glide
                    .with(context)
                    .load(context.getDrawable(R.drawable.ic_flower_selected)) // Заглушка
                    .into(holder.binding.ivPhoto);
        }
    }
}
