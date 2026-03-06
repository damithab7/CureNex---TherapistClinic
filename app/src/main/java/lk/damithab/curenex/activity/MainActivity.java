package lk.damithab.curenex.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;


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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivityMainBinding;
import lk.damithab.curenex.databinding.SideNavHeaderBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.dialog.WelcomeDialog;
import lk.damithab.curenex.fragment.AccountFragment;
import lk.damithab.curenex.fragment.CartFragment;
import lk.damithab.curenex.fragment.HomeFragment;
import lk.damithab.curenex.fragment.ServiceFragment;
import lk.damithab.curenex.fragment.ShopFragment;
import lk.damithab.curenex.helper.SQLiteHelper;
import lk.damithab.curenex.model.Clinic;
import lk.damithab.curenex.model.User;
import lk.damithab.curenex.util.AnimationUtil;

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

    @SuppressLint("ClickableViewAccessibility")
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
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();


        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());

        /// Load Clinic Data and store it in sqLite to reduct loadingTimes and access the information anywhere
        firebaseFirestore.collection("clinic").get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot qds) {
                                if(!qds.isEmpty()){

                                    Clinic clinicDetails = qds.toObjects(Clinic.class).get(0);

                                    binding.emergencyCallBtn.setText(clinicDetails.getEmergency());

                                    SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(MainActivity.this);
                                    SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

                                    ContentValues values = new ContentValues();
                                    values.put("id", 1);
                                    values.put("name", clinicDetails.getName());
                                    values.put("address",clinicDetails.getAddress());
                                    values.put("emergency",clinicDetails.getEmergency());
                                    values.put("email",clinicDetails.getEmail());
                                    values.put("phone",clinicDetails.getPhone());

                                    db.insertWithOnConflict("clinic", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                    db.close();
                                }
                            }
                        });



        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).collection("cart").get()
                    .addOnSuccessListener(qds -> {
                        int count = qds.size();

                        if (count > 0) {
                            bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
                        } else {
                            bottomNavigationView.removeBadge(R.id.nav_cart);
                        }
                    });
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
                        spinner.dismiss();
                    }).addOnFailureListener(e->{
                        Log.e("FireStore", "Error: "+e.getMessage());
                    });

            /// Hide side nav items
            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
            /// Visible side nav items
            navigationView.getMenu().findItem(R.id.side_nav_home).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_shop).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_notifications).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_bookings).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_support).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(false);
        }else{
            spinner.dismiss();
        }

        sideNavHeaderBinding.getRoot().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            // Triggers immediately when the finger touches the header
                            Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.click_anim);
                            view.startAnimation(anim);
                        }

                        // Return false so the click listener still works for navigation
                        return false;
                    }
                });
        sideNavHeaderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }

                if(firebaseAuth.getCurrentUser() != null) {
                    loadFragment(new AccountFragment());
                    bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
                }else{
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.navContainerView, fragment);
        transaction.commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.navContainerView, fragment).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                binding.navContainerView.setVisibility(View.GONE);
                binding.searchResultsRecyclerView.setVisibility(View.VISIBLE);
                binding.bottomNavigationView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                binding.navContainerView.setVisibility(View.VISIBLE);
                binding.searchResultsRecyclerView.setVisibility(View.GONE);
                AnimationUtil.bottomSlideUp(binding.bottomNavigationView);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }
        });

        return true;
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

        if (itemId == R.id.nav_home || itemId == R.id.side_nav_home) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        } else if (itemId == R.id.nav_service) {
            loadFragment(new ServiceFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_service).setChecked(true);

        } else if (itemId == R.id.side_nav_shop || itemId == R.id.nav_shop) {
            loadFragment(new ShopFragment());
            navigationView.getMenu().findItem(R.id.side_nav_shop).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_shop).setChecked(true);

        } else if (itemId == R.id.nav_cart || itemId == R.id.side_nav_cart) {
            if(firebaseAuth.getCurrentUser() == null){
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new CartFragment());
            navigationView.getMenu().findItem(R.id.side_nav_cart).setChecked(true);
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
            if(firebaseAuth.getCurrentUser() == null){
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_bookings) {
            if(firebaseAuth.getCurrentUser() == null){
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            Intent intent = new Intent(MainActivity.this, BookingHistoryActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_login) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_support) {
            Intent intent = new Intent(MainActivity.this, SupportActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_logout) {

            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);

            firebaseAuth.signOut();
            loadFragment(new HomeFragment());

            clearNavigationHeader();
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

    public void clearNavigationHeader(){
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.side_nav_menu);

        navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
        navigationView.inflateHeaderView(R.layout.side_nav_header);
    }
}