package lk.damithab.curenex.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivityMainBinding;
import lk.damithab.curenex.databinding.SideNavHeaderBinding;
import lk.damithab.curenex.dialog.WelcomeDialog;
import lk.damithab.curenex.fragment.AccountFragment;
import lk.damithab.curenex.fragment.CartFragment;
import lk.damithab.curenex.fragment.HomeFragment;
import lk.damithab.curenex.fragment.ServiceFragment;
import lk.damithab.curenex.fragment.ShopFragment;
import lk.damithab.curenex.model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private Button emergencyBtn;

    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    /// WelcomeScreen
    SharedPreferences mPrefs;

    private static final String welcomeScreenShownPref = "welcomeScreenShown",
            PREFERENCE_NAME = "welcome_screen";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding =ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// Initializing SideNavHeaderBinding layout by getting headerView from sideNavigation
        View headerView = binding.sideNavigationView.getHeaderView(0);

        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.mainDrawerlayout;
        toolbar = binding.mainToolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        emergencyBtn = binding.emergencyCallBtn;

        emergencyBtn.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0712342342"));
            startActivity(intent);
        });

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();


        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if(ds.exists()) {
                            User user = ds.toObject(User.class);
                            sideNavHeaderBinding.drawerHeaderName.setText(user.getFirstName()+" "+user.getLastName());
                            sideNavHeaderBinding.drawerHeaderEmail.setText(user.getEmail());

                            Glide.with(MainActivity.this)
                                    .load(user.getProfileUrl())
                                    .circleCrop()
                                    .into(sideNavHeaderBinding.drawerHeaderImage);
                        }
                    }).addOnFailureListener(e->{
                        Log.e("FireStore", "Error: "+e.getMessage());
                    });

            /// Hide side nav items
            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
            /// Visible side nav items
            navigationView.getMenu().findItem(R.id.side_nav_shop).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_notifications).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_bookings).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_support).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.navContainerView, fragment);
        transaction.commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.navContainerView, fragment).commit();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        navigationView.setCheckedItem(-1);

        Menu navMenu = navigationView.getMenu();
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int g = 0; g < navMenu.size(); g++) {
            navMenu.getItem(g).setChecked(false);
        }

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }

        if (itemId == R.id.nav_home) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        } else if (itemId == R.id.nav_service) {
            loadFragment(new ServiceFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_service).setChecked(true);

        } else if (itemId == R.id.side_nav_shop || itemId == R.id.nav_shop) {
            loadFragment(new ShopFragment());
            navigationView.getMenu().findItem(R.id.side_nav_shop).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_shop).setChecked(true);

        } else if (itemId == R.id.nav_cart) {
            if(firebaseAuth.getCurrentUser() == null){
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new CartFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_cart).setChecked(true);

        } else if (itemId == R.id.nav_account) {
            if(firebaseAuth.getCurrentUser() == null){
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new AccountFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);


        } else if (itemId == R.id.side_nav_notifications) {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_bookings) {
            Intent intent = new Intent(MainActivity.this, BookingHistoryActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_login) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);

            navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
            navigationView.inflateHeaderView(R.layout.side_nav_header);
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /// Welcome Screen Custom Dialog
        mPrefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);

        if (!welcomeScreenShown) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getSupportFragmentManager(), "welcome_dialog");

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.commit();
        }
    }
}