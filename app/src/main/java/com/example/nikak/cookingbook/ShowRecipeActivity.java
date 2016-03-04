package com.example.nikak.cookingbook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShowRecipeActivity extends AppCompatActivity {

    @Bind(R.id.tv_category)
    TextView tvCategory;
    @Bind(R.id.tv_description)
    TextView tvDescription;
    @Bind(R.id.linear)
    LinearLayout linearLayout;
    Recipe recipe = new Recipe();
    ListView listView;
    Toolbar toolbar;
    int id;

    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;
    ArrayAdapter<String> adapter;

    int EDIT_RECIPE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_recipe);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        View header = getLayoutInflater().inflate(R.layout.header_show_recipe, null);
        View footer = getLayoutInflater().inflate(R.layout.footer_show_recipe, null);

        listView = (ListView) findViewById(R.id.lvIngredients);
        listView.addHeaderView(header, "header", false);
        listView.addFooterView(footer, "footer", false);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            recipe.set_id(bundle.getInt("recipeId"));

            dbHelper = DBHelper.getInstance(this);
            sqLiteDatabase = dbHelper.getWritableDatabase();
            String[] selectionArgs = new String[]{String.valueOf(recipe.get_id())};
            Cursor cursor = sqLiteDatabase.query(
                    DBHelper.TABLE_RECIPES, null, "_id=?", selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String categoryId = String.valueOf(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_RECIPE_CATEGORY_ID)));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_RECIPE_NAME));
                recipe.setName(name);
                String description = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_RECIPE_DESCRIPTION));
                recipe.setDescription(description);
                addCategory(categoryId);
                addIngredients(selectionArgs);
                addImages(selectionArgs);
            }
        } else {
            recipe = savedInstanceState.getParcelable("recipe");
        }


        toolbar.setTitle(recipe.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recipe.ingredients);
        listView.setAdapter(adapter);

        String[] split = recipe.getDescription().split("\\n");
        tvCategory.setLines(split.length);
        tvCategory.setText(recipe.getCategoryName());
        tvDescription.setText(recipe.getDescription());
        addDrawees();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("recipe", recipe);
    }


    private void addImages(String[] selectionArgs) {
        Cursor cursor3 = sqLiteDatabase.query(
                DBHelper.TABLE_IMAGES, null, "recipe_id=?", selectionArgs, null, null, null);
        if (cursor3 != null && cursor3.moveToFirst()) {
            do {
                String image = cursor3.getString(cursor3.getColumnIndex(DBHelper.COLUMN_IMAGE_URI));
                final Uri uri = Uri.parse(image);
                recipe.imageUris.add(Uri.parse(image));
            } while (cursor3.moveToNext());
            cursor3.close();
        }
    }

    private void addDrawees() {

        for (Uri uri :
                recipe.imageUris) {

            final Uri uriTmp = uri;
            SimpleDraweeView draweeView = new SimpleDraweeView(this);
            int width = 200, height = 200;
            ImageRequest request;
            Log.d("uri show recipe", uri.toString());
            if (!uri.toString().equals("drawable")) {
                request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .setResizeOptions(new ResizeOptions(width, height))
                        .build();
                draweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(uriTmp);
                        startActivity(intent);

                    }
                });
            } else {
                request = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.egg)
                        .setResizeOptions(new ResizeOptions(width, height))
                        .build();
            }
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);

            RoundingParams roundingParams = RoundingParams.asCircle();
            GenericDraweeHierarchyBuilder builder =
                    new GenericDraweeHierarchyBuilder(getResources());
            builder.setRoundingParams(roundingParams);
            GenericDraweeHierarchy hierarchy = builder
                    .setRoundingParams(roundingParams)
                    .build();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            draweeView.setLayoutParams(params);
            draweeView.setHierarchy(hierarchy);
            draweeView.setPadding(4, 4, 4, 4);


            draweeView.setClickable(true);
            linearLayout.addView(draweeView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share_recipe) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            //intent.setType("text/plain");
            String ingredients = "";
            for (String ingr :
                    recipe.getIngredients()) {
                ingredients += ingr + "\n";
            }
            String message = String.format(
                    "%s\n" + getResources().getString(R.string.ingredients_label) + ":\n%s"
                            + getResources().getString(R.string.description_label) + ":\n%s",
                    recipe.getName(), ingredients, recipe.getDescription());
            if (!recipe.imageUris.get(0).toString().equals("drawable")) {
                //send recipe in message
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, recipe.getImageUris().get(0));
            } else {
                intent.setType("text/plain");
            }
            intent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(intent);

        }
        if (id == R.id.edit_recipe) {
            Intent intent = new Intent(getApplicationContext(), AddNewRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void addCategory(String categoryId) {

        Cursor cursor1 = sqLiteDatabase.query(
                DBHelper.TABLE_CATEGORIES, null, "_id=?", new String[]{categoryId}, null, null, null);
        if (cursor1 != null && cursor1.moveToFirst()) {
            String categoryName = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_CATEGORY_NAME));
            recipe.setCategory(Integer.parseInt(categoryId));
            recipe.setCategoryName(categoryName);
        }
        cursor1.close();
    }

    private void addIngredients(String[] selectionArgs) {
        Cursor cursor2 = sqLiteDatabase.query(
                DBHelper.TABLE_INGREDIENTS, null, "recipe_id=?", selectionArgs, null, null, null);
        Log.d("recipe ingr count", String.valueOf(cursor2.getCount()));
        String[] units = getResources().getStringArray(R.array.units);
        if (cursor2 != null && cursor2.moveToFirst()) {
            do {
                String ingName = cursor2.getString(
                        cursor2.getColumnIndex(DBHelper.COLUMN_INGREDIENT_NAME));
                double amount = cursor2.getDouble(
                        cursor2.getColumnIndex(DBHelper.COLUMN_INGREDIENT_AMOUNT));
                String unit = units[cursor2.getInt(cursor2.getColumnIndex(DBHelper.COLUMN_INGREDIENT_UNIT_ID))];
                String ingredient = ingName + " " + amount + " " + unit;
                recipe.ingredients.add(ingredient);

            } while (cursor2.moveToNext());
            cursor2.close();
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(ShowRecipeActivity.this);
        super.onBackPressed();
    }
}
