package com.ishaanbhela.geeksformovies;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ishaanbhela.geeksformovies.Database.SqLiteHelper;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyAdapter;
import com.ishaanbhela.geeksformovies.productionCompany.productionCompanyModel;

import java.util.ArrayList;
import java.util.List;

public class savedMovieDetails extends AppCompatActivity {

    private TextView movieTitle, movieTagline, movieGenres, movieReleaseDate, movieRuntime, movieRating, movieOverview, movieBudget, movieRevenue, movieLanguages;
    private ImageView moviePoster;
    private RecyclerView productionCompaniesRecyclerView;
    private List<productionCompanyModel> productionCompanyList;
    private productionCompanyAdapter productionCompaniesAdapter;
    private Button deleteMovie;

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details); // Use the same XML layout

        // Initialize UI elements
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

        deleteMovie = findViewById(R.id.saveUnsave);
        deleteMovie.setText("Delete from Saved Movies");

        productionCompanyList = new ArrayList<>();

        // Get movie ID from intent
        movieId = getIntent().getIntExtra("MOVIE_ID", -1);
        loadMovieDetails(movieId);

        deleteMovie.setOnClickListener(v -> {
            new SqLiteHelper(this).deleteMovie(movieId);
            finish();
        });
    }

    private void loadMovieDetails(int movieId) {
        SqLiteHelper dbHelper = new SqLiteHelper(this);
        Cursor cursor = dbHelper.getMovieDetails(movieId);

        if (cursor != null && cursor.moveToFirst()) {
            // Check and get column indices
            int titleIndex = cursor.getColumnIndex("title");
            int taglineIndex = cursor.getColumnIndex("tagline");
            int releaseDateIndex = cursor.getColumnIndex("release_date");
            int runtimeIndex = cursor.getColumnIndex("runtime");
            int ratingIndex = cursor.getColumnIndex("rating");
            int overviewIndex = cursor.getColumnIndex("overview");
            int budgetIndex = cursor.getColumnIndex("budget");
            int revenueIndex = cursor.getColumnIndex("revenue");
            int posterPathIndex = cursor.getColumnIndex("poster_path");
            int genresIndex = cursor.getColumnIndex("genres");
            int languagesIndex = cursor.getColumnIndex("languages");

            // Now check if the indices are valid (not -1)
            if (titleIndex != -1) {
                String title = cursor.getString(titleIndex);
                movieTitle.setText(title);
            }

            if (taglineIndex != -1) {
                String tagline = cursor.getString(taglineIndex);
                movieTagline.setText(tagline);
            }

            if (releaseDateIndex != -1) {
                String releaseDate = cursor.getString(releaseDateIndex);
                movieReleaseDate.setText(releaseDate);
            }

            if (runtimeIndex != -1) {
                int runtime = cursor.getInt(runtimeIndex);
                movieRuntime.setText("Runtime: " + runtime + " min");
            }

            if (ratingIndex != -1) {
                double rating = cursor.getDouble(ratingIndex);
                movieRating.setText("Rating: " + rating + "/10");
            }

            if (overviewIndex != -1) {
                String overview = cursor.getString(overviewIndex);
                movieOverview.setText(overview);
            }

            if (budgetIndex != -1) {
                long budget = cursor.getLong(budgetIndex);
                movieBudget.setText("Budget: $" + budget);
            }

            if (revenueIndex != -1) {
                long revenue = cursor.getLong(revenueIndex);
                movieRevenue.setText("Revenue: $" + revenue);
            }

            if (posterPathIndex != -1) {
                String posterPath = cursor.getString(posterPathIndex);
                // Load Poster Image using Glide
                Glide.with(savedMovieDetails.this).load("https://image.tmdb.org/t/p/w500" + posterPath)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(moviePoster);
            }

            if (genresIndex != -1) {
                String genres = cursor.getString(genresIndex);
                movieGenres.setText("Genres: " + genres);
            }

            if (languagesIndex != -1) {
                String languages = cursor.getString(languagesIndex);
                movieLanguages.setText("Languages: " + languages);
            }

            productionCompanyList = dbHelper.getProductionCompanies(movieId);
            productionCompaniesAdapter = new productionCompanyAdapter(productionCompanyList, this);
            productionCompaniesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            productionCompaniesRecyclerView.setAdapter(productionCompaniesAdapter);


            cursor.close();  // Close the cursor when done
        } else {
            // Handle the case when the movie is not found
            movieTitle.setText("Movie not found");
        }
    }
}
