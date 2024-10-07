package com.ishaanbhela.geeksformovies;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyAdapter;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class movieDetails extends AppCompatActivity {

    private TextView movieTitle, movieTagline, movieGenres, movieReleaseDate, movieRuntime, movieRating, movieOverview, movieBudget, movieRevenue, movieLanguages;
    private ImageView moviePoster;
    private RecyclerView productionCompaniesRecyclerView;
    private List<productionCompanyModel> productionCompanyList;
    private productionCompanyAdapter productionCompaniesAdapter;
    private Button saveUnsave;

    private int movieId;
    private String title, posterPath, tagline, releaseDate, overview, genres, languages;
    private int runtime;
    private long budget, revenue;
    private Double rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movieTitle = findViewById(R.id.movie_title);
        movieTagline = findViewById(R.id.movie_tagline);
        movieGenres = findViewById(R.id.movie_genres);
        movieReleaseDate = findViewById(R.id.movie_release_date);
        movieRuntime = findViewById(R.id.movie_runtime);
        movieRating = findViewById(R.id.movie_rating);
        movieOverview = findViewById(R.id.movie_overview);
        movieBudget = findViewById(R.id.movie_budget);
        movieRevenue = findViewById(R.id.movie_revenue);
        movieLanguages = findViewById(R.id.movie_languages);
        moviePoster = findViewById(R.id.movie_poster);
        productionCompaniesRecyclerView = findViewById(R.id.production_companies_recycler);
        saveUnsave = findViewById(R.id.saveUnsave);

        productionCompanyList = new ArrayList<>();
        SqLiteHelper dbHelper = new SqLiteHelper(this);

        movieId = getIntent().getIntExtra("MOVIE_ID", -1);

        // Call the API to get movie details
        getMovieDetails(movieId);

        if(dbHelper.isMovieSaved(movieId)){
            saveUnsave.setText("Delete from Saved Movies");
        }

        saveUnsave.setOnClickListener(v -> {
            if (saveUnsave.getText().equals("Save")) {
                // Save movie and production companies to the database
                dbHelper.insertMovie(movieId, title, posterPath, tagline, releaseDate, runtime, rating,
                        overview, budget, revenue, genres, languages, productionCompanyList);
                saveUnsave.setText("Delete from Saved Movies");
            } else {
                // Delete movie and its associated production companies
                dbHelper.deleteMovie(movieId);
                saveUnsave.setText("Save");
            }
        });
    }

    private void getMovieDetails(int movieId) {

        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?append_to_response=genres&language=en-US";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Populate UI with data
                        title = response.getString("title");
                        movieTitle.setText(title);

                        tagline = response.getString("tagline");
                        movieTagline.setText(tagline);

                        releaseDate = response.getString("release_date");
                        movieReleaseDate.setText(releaseDate);

                        runtime = response.getInt("runtime");
                        movieRuntime.setText("Runtime: " + runtime + " min");

                        rating = response.getDouble("vote_average");
                        movieRating.setText("Rating: " + rating + "/10");

                        overview = response.getString("overview");
                        movieOverview.setText(overview);

                        posterPath = response.getString("poster_path");

                        // Load Poster Image using Picasso
                        Glide.with(movieDetails.this).load("https://image.tmdb.org/t/p/w500" + posterPath)
                                .error(R.drawable.placeholder)
                                .placeholder(R.drawable.placeholder)
                                        .into(moviePoster);

                        // Budget and Revenue
                        budget = response.getLong("budget");
                        movieBudget.setText("Budget: $" + budget);

                        revenue = response.getLong("revenue");
                        movieRevenue.setText("Revenue: $" + revenue);

                        // Genres
                        JSONArray genresArray = response.getJSONArray("genres");
                        StringBuilder genres = new StringBuilder();
                        for (int i = 0; i < genresArray.length(); i++) {
                            JSONObject genre = genresArray.getJSONObject(i);
                            genres.append(genre.getString("name"));
                            if (i != genresArray.length() - 1) {
                                genres.append(", ");
                            }
                        }
                        this.genres = genres.toString();
                        movieGenres.setText(this.genres);

                        // Spoken Languages
                        JSONArray languagesArray = response.getJSONArray("spoken_languages");
                        StringBuilder languages = new StringBuilder();
                        for (int i = 0; i < languagesArray.length(); i++) {
                            JSONObject language = languagesArray.getJSONObject(i);
                            languages.append(language.getString("english_name"));
                            if (i != languagesArray.length() - 1) {
                                languages.append(", ");
                            }
                        }
                        this.languages = languages.toString();
                        movieLanguages.setText("Languages: " + this.languages);

                        // Production Companies
                        JSONArray productionCompaniesArray = response.getJSONArray("production_companies");
                        productionCompanyList.clear();
                        for (int i = 0; i < productionCompaniesArray.length(); i++) {
                            JSONObject company = productionCompaniesArray.getJSONObject(i);
                            String companyName = company.getString("name");
                            String logoPath = company.optString("logo_path", null);  // Sometimes logo_path can be null
                            if(!logoPath.equals("null")){
                                productionCompanyList.add(new productionCompanyModel(logoPath, companyName));
                            }

                        }
                        productionCompaniesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        productionCompaniesAdapter = new productionCompanyAdapter(productionCompanyList, this);
                        productionCompaniesRecyclerView.setAdapter(productionCompaniesAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    error.printStackTrace();
                }){

            @Override
            public Map<String, String> getHeaders() {
                String api = "YOUR_API_KEY";
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + api); // Use your auth token here
                headers.put("Content-Type", "application/json;charset=utf-8");
                headers.put("Accept", "application/json");
                return headers;
            }

        };

        queue.add(jsonObjectRequest);

    }
}