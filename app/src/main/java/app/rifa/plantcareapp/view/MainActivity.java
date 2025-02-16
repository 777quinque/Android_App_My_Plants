package app.rifa.plantcareapp.view;

import static app.rifa.plantcareapp.base.App.context;
import static app.rifa.plantcareapp.utils.Constants.PLANT_INTENT_EXTRAS_KEY;
import static app.rifa.plantcareapp.utils.NetworkUtils.isNetworkAvailable;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import app.rifa.plantcareapp.R;
import app.rifa.plantcareapp.base.BaseActivity;
import app.rifa.plantcareapp.core.main.MainContract;
import app.rifa.plantcareapp.core.main.MainPresenter;
import app.rifa.plantcareapp.databinding.ActivityMainBinding;
import app.rifa.plantcareapp.model.Plant;
import app.rifa.plantcareapp.model.User;

public class MainActivity extends BaseActivity implements MainContract.View {

    private AppBarConfiguration mAppBarConfiguration;
    private MainPresenter presenter;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkIfPlantToAddWasGiven();
    }

    private void init() {
        presenter = new MainPresenter(this);

        if (isNetworkAvailable(getApplicationContext())) {
            initBottomNav();
            initDrawerNav();
            hideConnectionError();
            presenter.checkIfUserIsLoggedIn();
        } else {
            showConnectionError();
        }

        binding.mainLayout.content.btRefresh.setOnClickListener(v -> {
                    hideConnectionError();
                    init();
                }
        );
    }

    private void showConnectionError() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.mainLayout.content.ivError.setVisibility(View.VISIBLE);
        binding.mainLayout.content.tvError.setVisibility(View.VISIBLE);
        binding.mainLayout.content.btRefresh.setVisibility(View.VISIBLE);

        binding.mainLayout.content.container.setVisibility(View.INVISIBLE);
        binding.mainLayout.content.bottomNavView.setVisibility(View.INVISIBLE);
    }

    private void hideConnectionError() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        binding.mainLayout.content.ivError.setVisibility(View.INVISIBLE);
        binding.mainLayout.content.tvError.setVisibility(View.INVISIBLE);
        binding.mainLayout.content.btRefresh.setVisibility(View.INVISIBLE);

        binding.mainLayout.content.container.setVisibility(View.VISIBLE);
        binding.mainLayout.content.bottomNavView.setVisibility(View.VISIBLE);
    }

    private void initDrawerNav() {
        binding.navView.setNavigationItemSelectedListener(v -> {
            switch (v.getItemId()) {
                case R.id.nav_logout:
                    requireLogin();
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    public void openCloseNavigationDrawer(View view) {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void initBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(v ->
        {
            switch (v.getItemId()) {
                case R.id.bottom_my_plants:
                    openMyPlants();
                    break;
                case R.id.bottom_discover:
                    openDiscover();
                    break;
                case R.id.bottom_add_plant:
                    openAddPlant();
                    break;
                default:
                    return false;
            }
            return true;
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void openAddPlant() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AddPlantFragment())
                .commitAllowingStateLoss();
    }

    private void openAddPlant(Plant plant) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLANT_INTENT_EXTRAS_KEY, plant);

        AddPlantFragment fragment = new AddPlantFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
    }

    private void openDiscover() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new DiscoverFragment())
                .commitAllowingStateLoss();
    }

    private void openMyPlants() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MyPlantsFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void requireLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void setUser(User user) {
        View headerView = binding.navView.getHeaderView(0);

        TextView drawerNameTextView = headerView.findViewById(R.id.tv_username);
        TextView drawerEmailTextView = headerView.findViewById(R.id.tv_email);

        drawerNameTextView.setText(user.getUsername());
        drawerEmailTextView.setText(user.getEmail());

        openMyPlants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfPlantToAddWasGiven();  // Check for the plant passed from CheckPlantActivity
    }

    private void checkIfPlantToAddWasGiven() {
        Plant plant = (Plant) getIntent().getSerializableExtra(PLANT_INTENT_EXTRAS_KEY);
        getIntent().removeExtra(PLANT_INTENT_EXTRAS_KEY);
        if (plant != null) {
            openAddPlant(plant);  // Handle the new plant correctly
        }
    }

    @Override
    public void setEmail(String email) {
        View headerView = binding.navView.getHeaderView(0);
        TextView drawerEmailTextView = headerView.findViewById(R.id.tv_email);
        drawerEmailTextView.setText(email);
    }
}