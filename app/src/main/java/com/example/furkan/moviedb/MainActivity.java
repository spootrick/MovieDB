package com.example.furkan.moviedb;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Creating a movie object
    Movie movie = null;

    //defining tabhost
    private TabHost tabhost;

    //search screen
    private EditText txtSearch;
    private Button btnSearch;

    //detail screen
    private TextView txtTitle;
    private TextView txtYear;
    private TextView txtIMDBrating;
    private TextView txtRated;
    private TextView txtReleased;
    private TextView txtRuntime;
    private TextView txtGenre;
    private TextView txtType;
    private TextView txtDirector;
    private TextView txtWriter;
    private TextView txtActors;
    private TextView txtPlot;
    private TextView txtLanguage;
    private TextView txtCountry;
    private TextView txtAwards;
    private TextView txtPosterURL;
    private ImageView poster;
    private ProgressBar progressBar;
    private Button btnAddToWatchLater;

    //watchlist screen
    private ListView watchList;
    private TextView txt_wtchlstTitle;
    private TextView txt_wtchlstYear;
    private TextView txt_wtchlstImbd;
    private TextView txt_wtchlstType;
    private TextView txt_wtchlstGenre;
    private TextView txt_wtchlstIsWatched;
    private TextView txt_wtchlstURL;
    private ImageView posterWatchList;
    private Button btnClearAll;


    //array list data for layout inflater
    private ArrayList<String> data = new ArrayList<String>();

    //Database Helper
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creating the database
        dbHelper = new DBHelper(this);

        //initializing the image loader
        initializeImageLoader();

        //initializing the widgets
        initializeWidgets();

        //initializing the on click listeners
        initializeListeners();

        //open the database
        dbHelper.openDB();

        //Load the watchlist on create
        loadWatchList();

        //disabling the watchlater button on create
        btnAddToWatchLater.setVisibility(View.GONE);
    }


    /**
     * This method initializes the universal image loader settings
     */
    private void initializeImageLoader() {
        // --- image loader from github ---
        // https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Quick-Setup
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.iconerror) // resource or drawable
                .showImageOnFail(R.drawable.iconerror) // resource or drawable
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start

    }

    /**
     * This method initializes the widgets and tabhost settings
     */
    private void initializeWidgets() {
        // tabhost settings
        tabhost = (TabHost) findViewById(R.id.tabhost);
        tabhost.setup();
        TabHost.TabSpec tabspec;

        tabspec = tabhost.newTabSpec("search screen");
        tabspec.setContent(R.id.layout_search);
        tabspec.setIndicator("Search", null);
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("detail screen");
        tabspec.setContent(R.id.layout_detail);
        tabspec.setIndicator("Detail", null);
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("watchlist screen");
        tabspec.setContent(R.id.layout_watchlist);
        tabspec.setIndicator("Watchlist", null);
        tabhost.addTab(tabspec);

        tabhost.setCurrentTab(0);

        //changing tabhost text color
        for (int i = 0; i < tabhost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#cccccc"));
        }

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId == "Detail") {
                    hideVirtualKeyboard();
                }
                if (tabId == "Watchlist") {
                    hideVirtualKeyboard();
                }
            }
        });


        // WIDGETS
        //Search screen widgets
        txtSearch = (EditText) findViewById(R.id.txt_search);
        btnSearch = (Button) findViewById(R.id.btn_search);

        //Detail screen widgets
        txtTitle = (TextView) findViewById(R.id.title);
        txtYear = (TextView) findViewById(R.id.year);
        txtIMDBrating = (TextView) findViewById(R.id.imdbRating);
        txtRated = (TextView) findViewById(R.id.rated);
        txtReleased = (TextView) findViewById(R.id.released);
        txtRuntime = (TextView) findViewById(R.id.runtime);
        txtGenre = (TextView) findViewById(R.id.genre);
        txtType = (TextView) findViewById(R.id.type);
        txtDirector = (TextView) findViewById(R.id.director);
        txtWriter = (TextView) findViewById(R.id.writer);
        txtActors = (TextView) findViewById(R.id.actors);
        txtPlot = (TextView) findViewById(R.id.plot);
        txtLanguage = (TextView) findViewById(R.id.language);
        txtCountry = (TextView) findViewById(R.id.country);
        txtAwards = (TextView) findViewById(R.id.awards);
        txtPosterURL = (TextView) findViewById(R.id.txt_posterUrl);
        poster = (ImageView) findViewById(R.id.poster);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnAddToWatchLater = (Button) findViewById(R.id.btn_addToWatchList);

        //Watchlist screen widgets
        watchList = (ListView) findViewById(R.id.list_watchlist);
        txt_wtchlstTitle = (TextView) findViewById(R.id.txt_watchlistTitle);
        txt_wtchlstYear = (TextView) findViewById(R.id.txt_watchlistYear);
        txt_wtchlstImbd = (TextView) findViewById(R.id.txt_watchlistImdb);
        txt_wtchlstGenre = (TextView) findViewById(R.id.txt_watchlistGenre);
        txt_wtchlstType = (TextView) findViewById(R.id.txt_watchlistType);
        txt_wtchlstIsWatched = (TextView) findViewById(R.id.txt_watchlistIsWatched);
        txt_wtchlstURL = (TextView) findViewById(R.id.txt_watchlistURL);

        posterWatchList = (ImageView) findViewById(R.id.posterWatchList);

        btnClearAll = (Button) findViewById(R.id.btn_clearAll);
    }

    /**
     * This method initializes the listeners for buttons
     */
    private void initializeListeners() {
        //button listeners
        btnSearch.setOnClickListener(this);
        btnAddToWatchLater.setOnClickListener(this);
        btnClearAll.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dbHelper.openDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDB();
    }

    /**
     * This method loads the watchlist
     */
    private void loadWatchList() {
        Cursor c = dbHelper.getAllRecords();

        WatchListCursorAdapter watchListCursorAdapter = new WatchListCursorAdapter(this, c, 0);

        watchList.setAdapter(watchListCursorAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:

                //getting the search parameter from the user
                String movieItem = txtSearch.getText().toString().trim().toLowerCase();

                // Checking for the empty search field
                if (movieItem.matches("")) {
                    Toast.makeText(this, "Please enter a movie or a TV show name", Toast.LENGTH_LONG).show();
                } else {
                    //checking internet connection
                    if (isNetworkAvailable()) {
                        // Fixing the movie name for the api query
                        // replacing all spaces with '+'
                        String searchItem = movieItem.replaceAll(" ", "+");

                        // Executing the OMDB API connection with users search item
                        new JSONTask().execute("http://www.omdbapi.com/?t=" + searchItem + "&y=&plot=full&r=json");

                        tabhost.setCurrentTabByTag("detail screen");
                    } else {
                        Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                    }

                    btnAddToWatchLater.setVisibility(View.VISIBLE);
                    btnAddToWatchLater.setEnabled(true);
                    hideVirtualKeyboard();
                }
                break;

            case R.id.btn_addToWatchList:
                long resultInsert = dbHelper.insert(getValue(txtTitle), getValue(txtYear),
                        getValue(txtIMDBrating), getValue(txtGenre), getValue(txtType),
                        getValue(txtPosterURL), "did not watched"); // initially I'm setting the is watched to not watched

                if (resultInsert == -1) {
                    Toast.makeText(this, "Some error occurred while adding to the watchlist :(", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, txtTitle.getText().toString() + " added to the watchlist", Toast.LENGTH_SHORT).show();
                }

                //prevent duplicate records on database
                btnAddToWatchLater.setEnabled(false);

                //refresh the list
                loadWatchList();
                break;
            case R.id.btn_clearAll:
                long resultDeleteAllRecords = dbHelper.deleteAllRecords();
                if (resultDeleteAllRecords == 0) {
                    Toast.makeText(this, "The watchlist is empty", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "All records deleted successfully!", Toast.LENGTH_SHORT).show();
                }
                //refreshing the watchlist
                loadWatchList();
                break;
        }
    }

    public String getValue(TextView tv) {
        return tv.getText().toString().trim();
    }


    /**
     * This class makes internet connection, retrieves data and show them in the
     * UI by using async task.
     */
    public class JSONTask extends AsyncTask<String, String, Movie> {

        /**
         * This is a worker method which runs in the background.
         *
         * @param params
         * @return
         */
        @Override
        protected Movie doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Getting url from the param array
                URL url = new URL(params[0]);

                // Making a connection to our url
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                // Retrieving the whole JSON file to the string
                String finalJSON = buffer.toString();

                // Creating json object from string buffer
                JSONObject movieObject = new JSONObject(finalJSON);

                // Creating a movie object from movie class with retrieved data
                movie = new Movie();
                movie.setTitle(movieObject.getString("Title"));
                movie.setYear(movieObject.getString("Year"));
                movie.setRated(movieObject.getString("Rated"));
                movie.setReleased(movieObject.getString("Released"));
                movie.setRuntime(movieObject.getString("Runtime"));
                movie.setGenre(movieObject.getString("Genre"));
                movie.setDirector(movieObject.getString("Director"));
                movie.setWriter(movieObject.getString("Writer"));
                movie.setActors(movieObject.getString("Actors"));
                movie.setPlot(movieObject.getString("Plot"));
                movie.setLanguage(movieObject.getString("Language"));
                movie.setCountry(movieObject.getString("Country"));
                movie.setAwards(movieObject.getString("Awards"));
                movie.setPoster(movieObject.getString("Poster"));
                movie.setMetascore(movieObject.getString("Metascore"));
                movie.setImdbRating(movieObject.getString("imdbRating"));
                movie.setImdbID(movieObject.getString("imdbID"));
                movie.setType(movieObject.getString("Type"));

                return movie;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                // Close the connection if connected
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    // Close the reader if opened
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        /**
         * This method sends the data which is retrieved from doInBackground() method to UI.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(Movie result) {
            super.onPostExecute(result);

            //If our movie object is not null we can show that in the screen
            if (result != null) {
                txtTitle.setText(result.getTitle().toString());
                txtYear.setText(result.getYear().toString());
                txtIMDBrating.setText(result.getImdbRating().toString());
                txtRated.setText(result.getRated().toString());
                txtReleased.setText(result.getReleased().toString());
                txtRuntime.setText(result.getRuntime().toString());
                txtGenre.setText(result.getGenre().toString());
                txtType.setText(result.getType().toString());
                txtDirector.setText(result.getDirector().toString());
                txtWriter.setText(result.getWriter().toString());
                txtActors.setText(result.getActors().toString());
                txtPlot.setText(result.getPlot().toString());
                txtLanguage.setText(result.getLanguage().toString());
                txtCountry.setText(result.getCountry().toString());
                txtAwards.setText(result.getAwards().toString());
                txtPosterURL.setText(result.getPoster().toString());

                //displaying the poster image
                ImageLoader.getInstance().displayImage(result.getPoster().toString(), poster, new ImageLoadingListener() {
                    //Proggress bar settings for the image
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Uups.. Sorry but the movie not found :(", Toast.LENGTH_LONG).show();
                tabhost.setCurrentTabByTag("search screen");
            }
        }
    }


    /**
     * This methods checks the device network availability.
     *
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This method hides the virtual keyboard.
     */
    private void hideVirtualKeyboard() {
        ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * Cursor Adapter Class
     * This class helps the retrieving of data from the sqlite database
     * and return the data to view.
     */
    public class WatchListCursorAdapter extends CursorAdapter {
        public WatchListCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.layout_watchlist_row, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView wtchlst_title = (TextView) view.findViewById(R.id.txt_watchlistTitle);
            TextView wtchlst_year = (TextView) view.findViewById(R.id.txt_watchlistYear);
            TextView wtchlst_imdb = (TextView) view.findViewById(R.id.txt_watchlistImdb);
            TextView wtchlst_type = (TextView) view.findViewById(R.id.txt_watchlistType);
            TextView wtchlst_genre = (TextView) view.findViewById(R.id.txt_watchlistGenre);
            TextView wtchlst_isWatched = (TextView) view.findViewById(R.id.txt_watchlistIsWatched);
            TextView wtchlst_url = (TextView) view.findViewById(R.id.txt_watchlistURL);
            ImageView wtchlst_poster = (ImageView) view.findViewById(R.id.posterWatchList);
            Button btn_delete = (Button) view.findViewById(R.id.btn_watchlistRowDelete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "You clicked delete for id: " , Toast.LENGTH_SHORT).show();
                    dbHelper.delete(0);
                    loadWatchList();
                }
            });
            btn_delete.setVisibility(View.GONE);

            Button btn_markAsWatched = (Button) view.findViewById(R.id.btn_watchlistWatched);
            btn_markAsWatched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "You clicked mark as watched for id: " , Toast.LENGTH_SHORT).show();
                    dbHelper.update(0, "watched");
                    loadWatchList();
                }
            });
            btn_markAsWatched.setVisibility(View.GONE);

            // Extract properties from cursor
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String year = cursor.getString(cursor.getColumnIndexOrThrow("year"));
            String imdb = cursor.getString(cursor.getColumnIndexOrThrow("imdb"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
            String isWatched = cursor.getString(cursor.getColumnIndexOrThrow("isWatched"));
            String url = cursor.getString(cursor.getColumnIndexOrThrow("url"));

            // Populate fields with extracted properties
            wtchlst_title.setText(title);
            wtchlst_year.setText(year);
            wtchlst_imdb.setText(imdb);
            wtchlst_type.setText(type);
            wtchlst_genre.setText(genre);
            wtchlst_isWatched.setText(isWatched);
            wtchlst_url.setText(url);

            ImageLoader.getInstance().displayImage(url, wtchlst_poster);
        }
    }
}