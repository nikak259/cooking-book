package com.example.nikak.cookingbook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.searchView)
    EditText searchView;
    int ADD_ITEM = 122;
    Cursor cursor;
    RecipeListAdapter adapter;
    Cursor cursor2;
    SQLiteDatabase sqLiteDatabase;
    DBHelper dbHelper;
    String RAW_QUERY =
            "SELECT r._id as _id, r.name as name, img.image_uri as uri, MIN(img._id)" +
                    " FROM recipes r INNER JOIN images img" +
                    " ON r._id=img.recipe_id" +
                    " GROUP BY r._id;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemActivity();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //create dbHelper and get db
        dbHelper = DBHelper.getInstance(getApplicationContext());
        sqLiteDatabase = dbHelper.getWritableDatabase();

        // add adapter to listview and set listeners
        setListView();

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String FILTER_QUERY =
                        "SELECT r._id as _id, r.name as name, img.image_uri as uri, MIN(img._id)" +
                                " FROM recipes r INNER JOIN images img" +
                                " ON r._id=img.recipe_id" +
                                " WHERE r.name LIKE '%" +constraint.toString() +"%'" +
                                " GROUP BY r._id;";
                return sqLiteDatabase.rawQuery(FILTER_QUERY, null);
            }
        });




    }

    private void setListView() {
       /* adapter = new RecipeListAdapter(getApplicationContext(),
                sqLiteDatabase.rawQuery(RAW_QUERY, null), true);*/
        String[] from = new String[]{};
        int[] to = new int[]{};
        adapter = new RecipeListAdapter(getApplicationContext(),R.layout.listview_item,
                sqLiteDatabase.rawQuery(RAW_QUERY, null), from,to);


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ShowRecipeActivity.class);
                intent.putExtra("recipeId",adapter.getRecipeId(position));
                startActivity(intent); }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.delete_recipe_label));
                builder.setMessage(getString(R.string.delete_recipe));

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = adapter.getRecipeId(position);
                        sqLiteDatabase.delete(DBHelper.TABLE_IMAGES,
                                DBHelper.COLUMN_IMAGE_RECIPE_ID + "=" + id, null);
                        sqLiteDatabase.delete(DBHelper.TABLE_INGREDIENTS,
                                DBHelper.COLUMN_INGREDIENT_RECIPE_ID, null);
                        sqLiteDatabase.delete(DBHelper.TABLE_RECIPES, "_id=" + id, null);
                        adapter.changeCursor(sqLiteDatabase.rawQuery(RAW_QUERY, null));
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.show_recipe_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_recipe) {

            addItemActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addItemActivity() {
        Intent intent = new Intent(getApplicationContext(), AddNewRecipeActivity.class);
        startActivityForResult(intent, ADD_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ITEM && resultCode == Activity.RESULT_OK) {
            adapter.changeCursor(sqLiteDatabase.rawQuery(RAW_QUERY, null));
            Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_add_recipe), Toast.LENGTH_SHORT).show();
        }
    }
}
