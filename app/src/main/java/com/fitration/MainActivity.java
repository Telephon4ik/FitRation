package com.fitration;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitration.auth.LoginActivity;
import com.fitration.databinding.ActivityMainBinding;
import com.fitration.models.User;
import com.fitration.ui.coach.fragments.ClientsFragment;
import com.fitration.ui.coach.fragments.CoachProfileFragment;
import com.fitration.ui.user.fragments.DietFragment;
import com.fitration.ui.user.fragments.ProfileFragment;
import com.fitration.viewmodels.AuthViewModel;
import com.fitration.viewmodels.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Проверяем авторизацию напрямую
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            navigateToLogin();
            return;
        }

        initViewModels();
    }

    private void initViewModels() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userViewModel.loadUser(firebaseUser.getUid()).observe(this, user -> {
                if (user != null) {
                    userViewModel.setCurrentUser(user);
                    setupBottomNavigation(user.getRole());
                    loadInitialFragment(user.getRole());
                } else {
                    // Если пользователь не найден в Firestore
                    navigateToLogin();
                }
            });
        }
    }

    private void setupBottomNavigation(String role) {
        BottomNavigationView bottomNav = binding.bottomNavigationView;

        if (role.equals("USER")) {
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.bottom_nav_menu_user);
            bottomNav.setOnNavigationItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_diet) {
                    selectedFragment = new DietFragment();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });
        } else if (role.equals("COACH")) {
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.bottom_nav_menu_coach);
            bottomNav.setOnNavigationItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_clients) {
                    selectedFragment = new ClientsFragment();
                } else if (itemId == R.id.navigation_coach_profile) {
                    selectedFragment = new CoachProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });
        }
    }

    private void loadInitialFragment(String role) {
        Fragment initialFragment;
        int selectedItemId;

        if (role.equals("USER")) {
            initialFragment = new DietFragment(); // Заменяем HomeFragment на DietFragment
            selectedItemId = R.id.navigation_diet;
        } else {
            initialFragment = new ClientsFragment();
            selectedItemId = R.id.navigation_clients;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit();

        binding.bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authViewModel.logout();
            navigateToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}