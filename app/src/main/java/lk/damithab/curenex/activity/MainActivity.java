package lk.damithab.curenex.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;


import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.BasicSearchAdapter;
import lk.damithab.curenex.databinding.ActivityMainBinding;
import lk.damithab.curenex.databinding.SideNavHeaderBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.dialog.WelcomeDialog;
import lk.damithab.curenex.fragment.AccountFragment;
import lk.damithab.curenex.fragment.CartFragment;
import lk.damithab.curenex.fragment.EmptyCartFragment;
import lk.damithab.curenex.fragment.HomeFragment;
import lk.damithab.curenex.fragment.ListingFragment;
import lk.damithab.curenex.fragment.ServiceFragment;
import lk.damithab.curenex.fragment.ShopFragment;
import lk.damithab.curenex.fragment.TherapistFragment;
import lk.damithab.curenex.helper.SQLiteHelper;
import lk.damithab.curenex.listener.FirestoreCallback;
import lk.damithab.curenex.model.Category;
import lk.damithab.curenex.model.Clinic;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.User;
import lk.damithab.curenex.util.AnimationUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener, SensorEventListener {
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

    private FirebaseStorage storage;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static final float SHAKE_THRESHOLD = 1f; // m/S**2
    private static final float MAX_SHAKE_THRESHOLD = 20f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 0;
    private long mLastShakeTime;

    private long accelerationStartTime = 0;

    private int MAX_HOLD_TIME = 500; //1.5 seconds

    private int cartCount;

    private List<Object> basicSearchList;
    private List<Object> advanceSearchList;

    private MenuItem searchItem;

    private String clinicNo;

    private SpinnerDialog spinner;

    private final ActivityResultLauncher<String> callPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    makePhoneCall();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

    /// Permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getFMCToken();

        setContentView(binding.getRoot());

        spinner = new SpinnerDialog();

        basicSearchList = new ArrayList<>();
        loadSearchData();

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

        /// Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        emergencyBtn = binding.emergencyCallBtn;

        emergencyBtn.setOnClickListener(v -> {
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

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        /// Check user verification status
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (!currentUser.isEmailVerified()) {
                    firebaseAuth.signOut();
                    clearNavigationHeader();
                }
            });
        }
//        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());

        /// Load Clinic Data and store it in sqLite to reduct loadingTimes and access the information anywhere
        firebaseFirestore.collection("clinic").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {

                            Clinic clinicDetails = qds.toObjects(Clinic.class).get(0);

                            binding.emergencyCallBtn.setText(clinicDetails.getEmergency());

                            SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(MainActivity.this);
                            SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

                            ContentValues values = new ContentValues();
                            values.put("id", 1);
                            values.put("name", clinicDetails.getName());
                            values.put("address", clinicDetails.getAddress());
                            values.put("emergency", clinicDetails.getEmergency());
                            values.put("email", clinicDetails.getEmail());
                            values.put("phone", clinicDetails.getPhone());
                            clinicNo = clinicDetails.getEmergency();

                            db.insertWithOnConflict("clinic", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            db.close();
                        }
                    }
                });


        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).collection("cart").get()
                    .addOnSuccessListener(qds -> {
                        int count = qds.size();
                        cartCount = count;
                        if (count > 0) {
                            bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
                        } else {
                            bottomNavigationView.removeBadge(R.id.nav_cart);
                        }
                    });

            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            sideNavHeaderBinding.drawerHeaderName.setText(user.getFirstName() + " " + user.getLastName());
                            sideNavHeaderBinding.drawerHeaderEmail.setText(user.getEmail());

                            if (user.getProfileUrl().startsWith("https")) {
                                Glide.with(binding.getRoot())
                                        .load(user.getProfileUrl())
                                        .centerCrop()
                                        .into(sideNavHeaderBinding.drawerHeaderImage);
                            } else {
                                storage.getReference(user.getProfileUrl())
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            Glide.with(binding.getRoot())
                                                    .load(uri)
                                                    .centerCrop()
                                                    .into(sideNavHeaderBinding.drawerHeaderImage);
                                        });
                            }

                        }
//                        if (spinner.isAdded()) {
//                            spinner.dismissAllowingStateLoss();
//                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FireStore", "Error: " + e.getMessage());
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
        } else {
//            spinner.dismissAllowingStateLoss();
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

                if (firebaseAuth.getCurrentUser() != null) {
                    loadFragment(new AccountFragment());
                    bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
                } else {
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

//    public void resetToolbarToDefault() {
//        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
//        if (toolbar != null) {
//            toolbar.setTitle("CureNex");
//            toolbar.setNavigationIcon(null);
//            toolbar.setVisibility(View.VISIBLE);
//
//            // Reload your default menu if you cleared it
//            toolbar.getMenu().clear();
//            getMenuInflater().inflate(R.menu.toolbar_menu, toolbar.getMenu());
//        }
//    }

    private void loadSearchData(){
        firebaseFirestore.collection("services").get().addOnSuccessListener(serviceSnaps -> {
            basicSearchList.addAll(serviceSnaps.toObjects(Service.class));

            // Fetch Categories
            firebaseFirestore.collection("categories").get().addOnSuccessListener(catSnaps -> {
                basicSearchList.addAll(catSnaps.toObjects(Category.class));
            });
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        searchItem = menu.findItem(R.id.action_search);

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
                if (s.isEmpty()) {
                    binding.searchResultsRecyclerView.setAdapter(null);
                    return false;
                }
                if(s.length() < 4){
                    performBasicSearch(s);
                }else{
//                    performAdvancedSearch(s.toLowerCase());
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String s) {


                return true;
            }
        });

        return true;
    }

    private void performBasicSearch(String query) {
        List<Object> filteredResults = new ArrayList<>();
        Log.d("MainActivity", "performBasicSearch: "+query);
        for (Object item : basicSearchList) {
            String name = "";
            if (item instanceof Service) name = ((Service) item).getName();
            if (item instanceof Category) name = ((Category) item).getCategoryName();

            if(name.toLowerCase().contains(query)){
                Log.d("MainActivity", "performBasicSearch name: "+name);
                filteredResults.add(item);
            }
        }

        setBasicSearchAdapter(filteredResults);
    }
    private void setBasicSearchAdapter(List<Object> combinedList){
        binding.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if(!combinedList.isEmpty()){
            BasicSearchAdapter adapter = new BasicSearchAdapter(combinedList, object->{
                if (searchItem != null) {
                    searchItem.collapseActionView();
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = this.getCurrentFocus();
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Fragment selectedFragment;
                Bundle bundle = new Bundle();

                if (object instanceof Service) {
                    Service service = (Service) object;
                    selectedFragment = new TherapistFragment();
                    bundle.putString("serviceId", service.getServiceId());
                } else {
                    Category category = (Category) object;
                    selectedFragment = new ListingFragment();
                    bundle.putString("categoryId", category.getCategoryId());
                }

                selectedFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.navContainerView, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            });
            binding.searchResultsRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    private void performAdvanceSearch(String query){
        List<Object> combinedList = new ArrayList<>();

        firebaseFirestore.collection("services")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(serviceSnapshots -> {
                    combinedList.addAll(serviceSnapshots.toObjects(Service.class));

                    // Second Query: Categories
                    firebaseFirestore.collection("categories")
                            .orderBy("categoryName")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .get()
                            .addOnSuccessListener(categorySnapshots -> {
                                combinedList.addAll(categorySnapshots.toObjects(Category.class));
                                setBasicSearchAdapter(combinedList);
                            });

                });

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
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }else{
                spinner.show(getSupportFragmentManager(), "SpinnerDialog");
                firebaseFirestore.collection("users").document(firebaseAuth.getUid()).collection("cart").get()
                        .addOnSuccessListener(qds -> {
                            spinner.dismiss();
                            int count = qds.size();
                            if (count > 0) {
                                loadFragment(new CartFragment());
                            } else {
                                loadFragment(new EmptyCartFragment());
                            }
                        }).addOnFailureListener(aVoid->{
                            spinner.dismiss();
                        });
            }

            navigationView.getMenu().findItem(R.id.side_nav_cart).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_cart).setChecked(true);

        } else if (itemId == R.id.nav_account) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new AccountFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);


        } else if (itemId == R.id.side_nav_notifications) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_bookings) {
            if (firebaseAuth.getCurrentUser() == null) {
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
            welcomeDialog.setOnContinueClickListener(view -> {
                checkAndRequestPermission();
            });
            welcomeDialog.show(getSupportFragmentManager(), "welcome_dialog");

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(welcomeScreenShownPref, true);
            editor.commit();
        }

    }

    public void clearNavigationHeader() {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.side_nav_menu);

        navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
        navigationView.inflateHeaderView(R.layout.side_nav_header);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                String format = String.format("X:%.2f Y:%.2f Z:%.2f", event.values[0], event.values[1], event.values[2]);
                long curTime = System.currentTimeMillis();
                if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    double acceleration = Math.abs(Math.sqrt(Math.pow(x, 2) +
                            Math.pow(y, 2) +
                            Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH);
//                    Log.d("MainActivity", "Acceleration is " + acceleration + "m/s^2");

                    if (acceleration < MAX_SHAKE_THRESHOLD && acceleration > SHAKE_THRESHOLD) {
                        mLastShakeTime = curTime;

                        if (accelerationStartTime == 0) {
                            accelerationStartTime = curTime;
                        }

                        if ((curTime - accelerationStartTime) >= MAX_HOLD_TIME) {
                            Log.d("MainActivity", "Continuous shake for 1.5 seconds");

                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                                    == PackageManager.PERMISSION_GRANTED) {
                                    makePhoneCall();
                            } else {
                                    callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
                            }
                            accelerationStartTime = 0;
                        }
                    } else {
                        accelerationStartTime = 0;
                    }
                }
                break;
        }
    }

    private void getFMCToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();
                        Log.d("FMC Token", token);
                    }
                });
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + clinicNo));
        startActivity(intent);
    }


    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}