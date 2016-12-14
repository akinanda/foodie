package edu.sjsu.cafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etiennelawlor.imagegallery.library.ImageGalleryFragment;
import com.etiennelawlor.imagegallery.library.activities.FullScreenImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.activities.ImageGalleryActivity;
import com.etiennelawlor.imagegallery.library.adapters.FullScreenImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.adapters.ImageGalleryAdapter;
import com.etiennelawlor.imagegallery.library.enums.PaletteColorType;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import butterknife.ButterKnife;
import edu.sjsu.cafe.menu.RestaurantFragment;
import edu.sjsu.cafe.messages.MessageFragment;
import edu.sjsu.cafe.offers.OfferManager;
import edu.sjsu.cafe.offers.OffersFragment;

//import com.joanzapata.iconify.Iconify;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ImageGalleryAdapter.ImageThumbnailLoader, FullScreenImageGalleryAdapter.FullScreenImageLoader {
    private CharSequence mTitle;
    private int navItemId = 0;
    private NavigationView navigationView;
    private DrawerLayout mDrawer;
    private int offerCount = 0;
    private DrawerLayout drawer, backPressedDrawerLayout;
    private static PaletteColorType paletteColorType;
    private static LinearLayout linearLayoutForGallery;
    private static Intent galleryIntent;
    private static Boolean firstRun = true;

    private static TextView actionBarOfferCountButton;
    private static RelativeLayout actionBarOfferContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadDefaultFragment(R.id.nav_about);
        } else {
            mTitle = savedInstanceState.getCharSequence("title");
            navItemId = savedInstanceState.getInt("navItemId");
            restoreActionBar();
        }


        // Listen to drawer open/close events for updating offer count
        activateDrawerOpenListener();

        if (firstRun) {
            firstRun = false;

            // Instantiate Iconify
            Iconify.with(new FontAwesomeModule());

            // Update offer count for first time
            getOfferCount();

            //Update offer count in action bar
            setOfferCountForActionBarIcon();

            // Create gallery
            createGallery();

            //Check if first install, then give one time offer
            createFirstInstallOffer(MainActivity.this);

            //create restaurant menu
            getRestaurantMenu(MainActivity.this);

            //get images for menu item sections
            AppUtility.fetchMenuImageResource(MainActivity.this);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequence("title", mTitle);
        savedInstanceState.putInt("navItemId", navItemId);
    }


    private static void getRestaurantMenu(Context context) {
        try {
            final InputStream file = context.getAssets().open("menu.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            AppUtility.readRestaurantMenu(reader);
            reader.close();
            file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void createFirstInstallOffer(Context context) {
        if (!AppUtility.isNullOrEmpty(context.getString(R.string.activate_first_time_offer))
                && context.getString(R.string.activate_first_time_offer).toLowerCase().equals("true")) {
            String PREFS_NAME = context.getResources().getString(R.string.database_file);
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            Boolean firstRun = true;
            try {
                if (settings != null) {
                    firstRun = !settings.contains("first_run");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (firstRun) {
                try {
                    settings.edit().putBoolean("first_run", false).commit();
                    AppUtility.oneTimeDownloadOffer(context);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private void createGallery() {
        ButterKnife.bind(this);
        ImageGalleryActivity.setImageThumbnailLoader(this);
        ImageGalleryFragment.setImageThumbnailLoader(this);
        FullScreenImageGalleryActivity.setFullScreenImageLoader(this);

        // optionally set background color using Palette for full screen images
        paletteColorType = PaletteColorType.MUTED;

        galleryIntent = new Intent(this, ImageGalleryActivity.class);

        try {
            String[] images = getResources().getAssets().list("gallery");
            String[] uniqueImages = new HashSet<String>(Arrays.asList(images)).toArray(new String[0]);
            Arrays.sort(uniqueImages);
            String[] resImages = new String[uniqueImages.length];
            for (int i = 0; i < uniqueImages.length; i++) {
                resImages[i] = "file:///android_asset/gallery/" + uniqueImages[i];
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList(ImageGalleryActivity.KEY_IMAGES, new ArrayList<>(Arrays.asList(resImages)));
            bundle.putString(ImageGalleryActivity.KEY_TITLE, "Gallery");
            galleryIntent.putExtras(bundle);
        } catch (Exception ex) {

        }
    }

    private void getOfferCount() {
        OfferManager offerManager = new OfferManager(this);
        offerManager.open();
        offerManager.getOfferCount();
        refreshOfferCount();
    }

    public void refreshOfferCount() {
        Menu menu = navigationView.getMenu();
        MenuItem offers = menu.findItem(R.id.nav_offers);
        if (AppUtility.offerCount > 0) {
            setOfferCountForActionBarIcon();
        }
    }


    public void activateDrawerOpenListener() {
        View drawerView = findViewById(R.id.drawer_layout);
        if (drawerView != null && drawerView instanceof DrawerLayout) {
            mDrawer = (DrawerLayout) drawerView;
            mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    if (offerCount != AppUtility.offerCount) {
                        offerCount = AppUtility.offerCount;
                        refreshOfferCount();
                    }
                }

                @Override
                public void onDrawerClosed(View drawerView) {

                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void loadDefaultFragment(int id) {
        navigationView.getMenu().getItem(getMenuItemIndex(id)).setChecked(true);
        mTitle = "About Us";
        AboutFragment aboutFragment = new AboutFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.landing_page, aboutFragment, aboutFragment.getTag()).commit();
        restoreActionBar();
    }

    @Override
    public void onBackPressed() {
        backPressedDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        try {
            if (backPressedDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                backPressedDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (navItemId != R.id.nav_about) {
                    loadDefaultFragment(R.id.nav_about);
                } else {
                    super.onBackPressed();
                }
            }
        } catch(Exception ex) {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        //Offer page
        Intent intent = getIntent();
        if (intent.getStringExtra("reload_offer") != null && intent.getStringExtra("reload_offer").equals("true")) {
            intent.putExtra("reload_offer", "false");
            intent.setAction(null);
            setIntent(intent);
            setMenuItem(R.id.nav_offers);
        } else if (intent.getStringExtra("reload_messages") != null && intent.getStringExtra("reload_messages").equals("true")) {
            intent.putExtra("reload_messages", "false");
            intent.setAction(null);
            setIntent(intent);
            setMenuItem(R.id.nav_messages);
        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.offer_badge);
        MenuItemCompat.setActionView(item, R.layout.offer_notification_bar_button);
        View view = MenuItemCompat.getActionView(item);
        actionBarOfferCountButton = (TextView)view.findViewById(R.id.offer_count);
        actionBarOfferContainer = (RelativeLayout)view.findViewById(R.id.offer_container);

        if(AppUtility.offerCount<1) {
            actionBarOfferCountButton.setVisibility(View.INVISIBLE);
            actionBarOfferCountButton.setText("");
        } else {
            actionBarOfferCountButton.setVisibility(View.VISIBLE);
            actionBarOfferCountButton.setText(String.valueOf(AppUtility.offerCount));
        }


        actionBarOfferContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMenuItem(R.id.nav_offers);
            }
        });

        actionBarOfferCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMenuItem(R.id.nav_offers);
            }
        });

        return super.onCreateOptionsMenu(menu);

    }



    private void setOfferCountForActionBarIcon(){
        if (actionBarOfferCountButton == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AppUtility.offerCount == 0)
                    actionBarOfferCountButton.setVisibility(View.INVISIBLE);
                else {
                    actionBarOfferCountButton.setVisibility(View.VISIBLE);
                    actionBarOfferCountButton.setText(Integer.toString(AppUtility.offerCount));
                }
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        navItemId = item.getItemId();

        FragmentManager manager = getSupportFragmentManager();

        if (navItemId == R.id.nav_about) {
            mTitle = "About Us";
            AboutFragment aboutFragment = new AboutFragment();
            manager.beginTransaction().replace(R.id.landing_page, aboutFragment, aboutFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_restaurant) {
            mTitle = "Restaurant Menu";
            RestaurantFragment restaurantFragment = new RestaurantFragment();
            manager.beginTransaction().replace(R.id.landing_page, restaurantFragment, restaurantFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_offers) {
            mTitle = "Offers";
            OffersFragment offersFragment = new OffersFragment();
            manager.beginTransaction().replace(R.id.landing_page, offersFragment, offersFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_messages) {
            mTitle = "Messages";
            MessageFragment messageFragment = new MessageFragment();
            manager.beginTransaction().replace(R.id.landing_page, messageFragment, messageFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_recipes) {
            mTitle = "Recipe Search";
            RecipesFragment recipesFragment = new RecipesFragment();
            manager.beginTransaction().replace(R.id.landing_page, recipesFragment, recipesFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_gallery) {
            startActivity(galleryIntent);
        } else if (navItemId == R.id.nav_contact) {
            mTitle = "Contact";
            ContactFragment contactFragment = new ContactFragment();
            manager.beginTransaction().replace(R.id.landing_page, contactFragment, contactFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_compass) {
            mTitle = "Compass";
            CompassFragment compassFragment = new CompassFragment();
            manager.beginTransaction().replace(R.id.landing_page, compassFragment, compassFragment.getTag()).commit();
        } else if (navItemId == R.id.nav_sso) {
            mTitle = "Sign-in with Google";
            SSOFragment ssoFragment = new SSOFragment();
            manager.beginTransaction().replace(R.id.landing_page, ssoFragment, ssoFragment.getTag()).commit();
        }

        //setSelectedMenuItem();
        drawer.closeDrawer(GravityCompat.START);
        getSupportActionBar().setTitle(mTitle);
        return true;
    }

    private int getMenuItemIndex(int position) {
        if (position == R.id.nav_about) {
            return 0;
        } else if (position == R.id.nav_restaurant) {
            return 1;
        } else if (position == R.id.nav_offers) {
            return 2;
        } else if (position == R.id.nav_messages) {
            return 3;
        } else if (position == R.id.nav_recipes) {
            return 4;
        } else if (position == R.id.nav_gallery) {
            return 5;
        } else if (position == R.id.nav_contact) {
            return 6;
        } else if (position == R.id.nav_compass) {
            return 7;
        } else if (position == R.id.nav_sso) {
            return 8;
        }
        return 0;
    }

    private void setSelectedMenuItem() {
        navigationView.getMenu().getItem(getMenuItemIndex(navItemId)).setChecked(true);
    }

    public void setMenuItem(int id) {
        onNavigationItemSelected(navigationView.getMenu().getItem(getMenuItemIndex(id)));
        setSelectedMenuItem();
    }

    public void call() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + getString(R.string.phone)));
        try {
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(this, "Please call " + getString(R.string.phone), Toast.LENGTH_LONG);
        }
    }


    @Override
    public void loadFullScreenImage(ImageView iv, String imageUrl, int width, LinearLayout bglinearLayout) {
        if (!TextUtils.isEmpty(imageUrl)) {
            final ImageView imageView = iv;
            linearLayoutForGallery = bglinearLayout;
            Picasso.with(iv.getContext())
                    .load(Uri.parse(imageUrl))
                    .resize(width, 0)
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    applyPalette(palette, linearLayoutForGallery);
                                }
                            });
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            iv.setImageDrawable(null);
        }
    }

    @Override
    public void loadImageThumbnail(ImageView iv, String imageUrl, int dimension) {
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(iv.getContext())
                    .load(Uri.parse(imageUrl))
                    .resize(dimension, dimension)
                    .centerCrop()
                    .into(iv);
        } else {
            iv.setImageDrawable(null);
        }
    }


    // region Helper Methods
    private void applyPalette(Palette palette, LinearLayout bgLinearLayout) {
        int bgColor = getBackgroundColor(palette);
        if (bgColor != -1)
            bgLinearLayout.setBackgroundColor(bgColor);
    }

    private int getBackgroundColor(Palette palette) {
        int bgColor = -1;

        int vibrantColor = palette.getVibrantColor(0x000000);
        int lightVibrantColor = palette.getLightVibrantColor(0x000000);
        int darkVibrantColor = palette.getDarkVibrantColor(0x000000);

        int mutedColor = palette.getMutedColor(0x000000);
        int lightMutedColor = palette.getLightMutedColor(0x000000);
        int darkMutedColor = palette.getDarkMutedColor(0x000000);

        if (paletteColorType != null) {
            switch (paletteColorType) {
                case VIBRANT:
                    if (vibrantColor != 0) { // primary option
                        bgColor = vibrantColor;
                    } else if (lightVibrantColor != 0) { // fallback options
                        bgColor = lightVibrantColor;
                    } else if (darkVibrantColor != 0) {
                        bgColor = darkVibrantColor;
                    } else if (mutedColor != 0) {
                        bgColor = mutedColor;
                    } else if (lightMutedColor != 0) {
                        bgColor = lightMutedColor;
                    } else if (darkMutedColor != 0) {
                        bgColor = darkMutedColor;
                    }
                    break;
                case LIGHT_VIBRANT:
                    if (lightVibrantColor != 0) { // primary option
                        bgColor = lightVibrantColor;
                    } else if (vibrantColor != 0) { // fallback options
                        bgColor = vibrantColor;
                    } else if (darkVibrantColor != 0) {
                        bgColor = darkVibrantColor;
                    } else if (mutedColor != 0) {
                        bgColor = mutedColor;
                    } else if (lightMutedColor != 0) {
                        bgColor = lightMutedColor;
                    } else if (darkMutedColor != 0) {
                        bgColor = darkMutedColor;
                    }
                    break;
                case DARK_VIBRANT:
                    if (darkVibrantColor != 0) { // primary option
                        bgColor = darkVibrantColor;
                    } else if (vibrantColor != 0) { // fallback options
                        bgColor = vibrantColor;
                    } else if (lightVibrantColor != 0) {
                        bgColor = lightVibrantColor;
                    } else if (mutedColor != 0) {
                        bgColor = mutedColor;
                    } else if (lightMutedColor != 0) {
                        bgColor = lightMutedColor;
                    } else if (darkMutedColor != 0) {
                        bgColor = darkMutedColor;
                    }
                    break;
                case MUTED:
                    if (mutedColor != 0) { // primary option
                        bgColor = mutedColor;
                    } else if (lightMutedColor != 0) { // fallback options
                        bgColor = lightMutedColor;
                    } else if (darkMutedColor != 0) {
                        bgColor = darkMutedColor;
                    } else if (vibrantColor != 0) {
                        bgColor = vibrantColor;
                    } else if (lightVibrantColor != 0) {
                        bgColor = lightVibrantColor;
                    } else if (darkVibrantColor != 0) {
                        bgColor = darkVibrantColor;
                    }
                    break;
                case LIGHT_MUTED:
                    if (lightMutedColor != 0) { // primary option
                        bgColor = lightMutedColor;
                    } else if (mutedColor != 0) { // fallback options
                        bgColor = mutedColor;
                    } else if (darkMutedColor != 0) {
                        bgColor = darkMutedColor;
                    } else if (vibrantColor != 0) {
                        bgColor = vibrantColor;
                    } else if (lightVibrantColor != 0) {
                        bgColor = lightVibrantColor;
                    } else if (darkVibrantColor != 0) {
                        bgColor = darkVibrantColor;
                    }
                    break;
                case DARK_MUTED:
                    if (darkMutedColor != 0) { // primary option
                        bgColor = darkMutedColor;
                    } else if (mutedColor != 0) { // fallback options
                        bgColor = mutedColor;
                    } else if (lightMutedColor != 0) {
                        bgColor = lightMutedColor;
                    } else if (vibrantColor != 0) {
                        bgColor = vibrantColor;
                    } else if (lightVibrantColor != 0) {
                        bgColor = lightVibrantColor;
                    } else if (darkVibrantColor != 0) {
                        bgColor = darkVibrantColor;
                    }
                    break;
                default:
                    break;
            }
        }

        return bgColor;
    }
    // endregion


    @Override
    protected void onPostResume() {
        if (navItemId == R.id.nav_gallery) {
            loadDefaultFragment(R.id.nav_about);
        }
        super.onPostResume();
    }
}
