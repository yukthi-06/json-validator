package com.example.jsonvalidator;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.jsonvalidator.ui.AboutActivity;
import com.example.jsonvalidator.ui.HelpActivity;
import com.example.jsonvalidator.ui.SettingsFragment;
import com.example.jsonvalidator.ui.ValidationFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateActionBarNavigation(toggle);
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof SettingsFragment) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.title_settings);
                }
                navigationView.setCheckedItem(R.id.nav_settings);
            } else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                navigationView.setCheckedItem(R.id.nav_main);
            }
        });
        updateActionBarNavigation(toggle);

        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_main) {
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragment = new ValidationFragment();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.title_settings);
                }
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
            
            if (fragment != null) {
                androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment);
                if (!(fragment instanceof ValidationFragment)) {
                    transaction.addToBackStack(null);
                }
                transaction.commit();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ValidationFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_main);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void updateActionBarNavigation(ActionBarDrawerToggle toggle) {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount > 0) {
            toggle.setDrawerIndicatorEnabled(false);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toggle.setToolbarNavigationClickListener(v -> onBackPressed());
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();
        }
    }
}
