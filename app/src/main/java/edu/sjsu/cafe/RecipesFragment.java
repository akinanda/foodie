package edu.sjsu.cafe;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static edu.sjsu.cafe.AppUtility.PERMISSIONS_REQUEST_PHONE_CALL;


public class RecipesFragment extends Fragment {

    private View recipesView;
    private String JSONResponse;
    ProgressBar spinner;

    ImageView recipeImageView;
    TextView recipeTitle;
    TextView view_recipe_details, share_recipe;
    Button searchButton;
    EditText searchRecipeBox;

    private String phoneNumberToCall = "";
    private String searchQuery = "";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);
        recipesView = view;
        spinner = (ProgressBar) recipesView.findViewById(R.id.progressBar);
        searchRecipeBox = (EditText)view.findViewById(R.id.searchRecipe);

        searchButton = (Button)view.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!AppUtility.isNullOrEmpty(searchRecipeBox.getText().toString())) {
                    try {
                        searchQuery = "&q=" + URLEncoder.encode(searchRecipeBox.getText().toString(), "utf-8");
                    } catch (Exception ex) {

                    }
                    getRecipes();
                }

            }
        });


        getRecipes();
        return view;
    }

    private void getRecipes() {

        if (!DetectConnection.isInternetAvailable(getContext())) {
            showErrorMessage();
            return;
        }
        String recipesAPIUrl = getString(R.string.food2forkAPI) + "?key=228b95f78fd84ffcc610c5287a30074e"+searchQuery;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(recipesAPIUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showErrorMessage();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showErrorMessage();
                } else {
                    try {
                        JSONResponse = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinner.setVisibility(View.GONE);
                                processResponse(JSONResponse);
                            }
                        });
                    } catch (Exception ex) {
                        showErrorMessage();
                    }
                }
            }
        });
    }


    private void processResponse(String responseBody) {
        if (!AppUtility.isNullOrEmpty(responseBody)) {
            try {
                JSONObject jsonRootObject = new JSONObject(responseBody);
                JSONArray recipes = jsonRootObject.optJSONArray("recipes");

                LinearLayout recipesLayout = (LinearLayout) recipesView.findViewById(R.id.recipes_container);
                LinearLayout recipeContainer, textContainer;

                if(recipesLayout.getChildCount() > 0) {
                    recipesLayout.removeAllViews();
                }

                Context recipeContext = getActivity();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);


                String recipeTitleText, recipeSocialRank, recipeThumbnail;

                if(recipes.length()==0) {
                    Toast.makeText(this.getActivity(), "No Recipes", Toast.LENGTH_LONG);
                }
                for (int i = 0; i < recipes.length(); i++) {
                    JSONObject recipe = recipes.getJSONObject(i);
                    final String recipeUrl = recipe.optString("source_url").toString();
                    recipeTitleText = recipe.optString("title").toString();
                    recipeSocialRank = recipe.optString("social_rank").toString();
                    recipeThumbnail = recipe.optString("image_url").toString();

                    recipeContainer = new LinearLayout(recipeContext);
                    recipeContainer.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    containerParams.setMargins(10, 15, 10, 0);
                    recipeContainer.setLayoutParams(containerParams);
                    recipeContainer.setPadding(20, 30, 20, 10);
                    recipeContainer.setGravity(Gravity.CENTER_HORIZONTAL);
                    recipeContainer.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    recipesLayout.addView(recipeContainer);

                    recipeTitle = new TextView(recipeContext);
                    recipeTitle.setTextAppearance(recipeContext, R.style.RecipeTitle);
                    recipeTitle.setPadding(0, 15, 0, 10);
                    recipeTitle.setText(recipeTitleText);
                    recipeContainer.addView(recipeTitle);


                    if(!AppUtility.isNullOrEmpty(recipeThumbnail)) {
                    recipeImageView = new ImageView(recipeContext);
                    recipeImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    recipeContainer.addView(recipeImageView);

                        Picasso.with(recipeContext)
                                .load(recipeThumbnail)
                                .into(recipeImageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        recipeImageView.setAdjustViewBounds(true);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }

                    textContainer = new LinearLayout(recipeContext);
                    textContainer.setOrientation(LinearLayout.HORIZONTAL);
                    textContainer.setLayoutParams(layoutParams);
                    textContainer.setPadding(0, 15, 0, 0);
                    recipeContainer.addView(textContainer);



                    view_recipe_details = new TextView(recipeContext);
                    view_recipe_details.setLayoutParams(layoutParams);
                    view_recipe_details.setText("View Recipe");
                    view_recipe_details.setPadding(0, 20, 0, 20);
                    view_recipe_details.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    view_recipe_details.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipeUrl));
                            startActivity(browserIntent);
                        }
                    });


                    share_recipe = new TextView(recipeContext);
                    share_recipe.setLayoutParams(layoutParams);
                    share_recipe.setText("Share");
                    share_recipe.setPadding(0, 20, 0, 20);
                    share_recipe.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    share_recipe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, recipeUrl);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                        }
                    });

                    textContainer.addView(view_recipe_details);
                    textContainer.addView(share_recipe);

                }
            } catch (Exception ex) {
                showErrorMessage();
            }
        }
    }


    private void showErrorMessage() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (spinner!=null && spinner.isShown()) {
                    spinner.setVisibility(View.GONE);
                }
                Toast toast = Toast.makeText(RecipesFragment.this.getActivity()
                            , "Unable to get Recipes."
                            , Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }, 500);

    }

    public void call(String number) {
        phoneNumberToCall = number;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && RecipesFragment.this.getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, AppUtility.PERMISSIONS_REQUEST_PHONE_CALL);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(RecipesFragment.this.getActivity(),"Please call " + number, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_PHONE_CALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                call(phoneNumberToCall);
            } else {
                Toast.makeText(RecipesFragment.this.getActivity(), "Permission to call denied!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
