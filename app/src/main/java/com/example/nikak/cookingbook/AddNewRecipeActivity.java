package com.example.nikak.cookingbook;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddNewRecipeActivity extends AppCompatActivity {

    @Bind(R.id.spinner_units)
    Spinner spinnerUnits;
    @Bind(R.id.spinner_category)
    Spinner spinnerCategories;
    ListView listView;
    @Bind(R.id.et_ingredient)
    EditText etIngredient;
    @Bind(R.id.et_ingredient_amount)
    EditText etAmount;
    @Bind(R.id.et_description)
    EditText etDescription;
    @Bind(R.id.et_name)
    EditText etName;
    @Bind(R.id.linear)
    LinearLayout linearLayout;

    ArrayAdapter<String> adapterIngredients;
    long selectedUnit = 0;

    boolean update = false;

    Recipe recipe;

    //RESULT
    int PICK_IMAGE = 133;
    String mCurrentPhotoPath = "123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_new_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View header = getLayoutInflater().inflate(R.layout.header_add_recipe, null);
        View footer = getLayoutInflater().inflate(R.layout.footer_add_recipe, null);

        listView = (ListView) findViewById(R.id.listView2);
        listView.addHeaderView(header, "header", false);
        listView.addFooterView(footer, "footer", false);

        ButterKnife.bind(this);

        recipe = new Recipe();  //recipe instance


        if (savedInstanceState != null) {
            recipe = savedInstanceState.getParcelable("recipe");
            mCurrentPhotoPath = savedInstanceState.getString("path");
            etIngredient.setText(savedInstanceState.getString("ingredient"));
            etAmount.setText(savedInstanceState.getString("amount"));
            selectedUnit = savedInstanceState.getLong("unit");
            setViews();
        } else {

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                update = true;
                recipe = bundle.getParcelable("recipe");
                setViews();
            }
        }

        createUnitsSpinner(); //add items to units spinner

        createCategorySpinner(); //add categories to categories spinner

        createIngredientsListView(); //create adapterIngredients for ingredients


        Log.d("OnCreate", mCurrentPhotoPath);


    }


    private void setViews() {
        etName.setText(recipe.getName());
        etDescription.setText(recipe.getDescription());
        for (Uri uri : recipe.imageUris) {
            addImage(uri);
        }
    }

    private void addImage(final Uri uri) {

        if (!uri.toString().equals("drawable")) {
            final SimpleDraweeView draweeView = new SimpleDraweeView(this);

            int width = 200, height = 200;
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
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

            draweeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    startActivity(intent);

                }
            });

            draweeView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecipeActivity.this);
                    builder.setTitle(getString(R.string.delete_image));
                    builder.setMessage(getString(R.string.delete_img_question));
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String path = getPath(uri);
                            File file = new File(path).getAbsoluteFile();
                            file.delete();
                            int i = getIndex(recipe.getImageUris(), uri);
                            recipe.imageUris.remove(i);
                            ((ViewGroup) draweeView.getParent()).removeView(draweeView);

                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), null);
                    builder.show();
                    return true;
                }
            });
            draweeView.setClickable(true);
            linearLayout.addView(draweeView);
        }
    }

    private void createUnitsSpinner() {
        final ArrayAdapter<CharSequence> adapterUnits = ArrayAdapter.createFromResource
                (this, R.array.units, android.R.layout.simple_spinner_item);
        adapterUnits.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(adapterUnits);
        spinnerUnits.setSelection((int) selectedUnit);
    }

    private void createIngredientsListView() {
        adapterIngredients = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, recipe.ingredients);
        listView.setAdapter(adapterIngredients);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == 0 || position == adapterIngredients.getCount() + 1) {
                    return false;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecipeActivity.this);
                    builder.setTitle(getString(R.string.delete_ingredient_label));
                    builder.setMessage(getString(R.string.delete_ingredient));
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapterIngredients.remove(adapterIngredients.getItem(position - 1));
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), null);
                    builder.show();
                    return true;
                }

            }
        });
    }

    private void createCategorySpinner() {
        ArrayList<CharSequence> categories = new ArrayList<>();
        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                DBHelper.TABLE_CATEGORIES, new String[]{DBHelper.COLUMN_CATEGORY_NAME},
                null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CATEGORY_NAME));
                    categories.add(name);
                    Log.d("categories name",name);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        ArrayAdapter<CharSequence> adapterCategory = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, categories);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapterCategory);
        spinnerCategories.setSelection(recipe.getCategory() - 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", mCurrentPhotoPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", mCurrentPhotoPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", mCurrentPhotoPath);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("onRestart", mCurrentPhotoPath);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", mCurrentPhotoPath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", mCurrentPhotoPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE) && (resultCode == Activity.RESULT_OK)) {
            if (data == null) // if data is null - we choose camera
            {
                Log.d("mCurrentPhotoPath", mCurrentPhotoPath);
                Uri uri = getImageContentUri(this, new File(mCurrentPhotoPath));
                recipe.imageUris.add(uri);
                addImage(uri);
            } else {
                Log.d("image path", getPath(data.getData()));
                try {
                    String selectedImagePath = getPath(data.getData());
                    String[] splitted = selectedImagePath.split("\\.");
                    String type = splitted[splitted.length - 1];
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "IMG_" + timeStamp + "_";
                    String path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/CookingBook/" + imageFileName + "." + type;
                    copyFile(selectedImagePath, path);
                    Uri uri = getImageContentUri(this, new File(path));
                    recipe.imageUris.add(uri);
                    addImage(uri);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", mCurrentPhotoPath);
        outState.putString("ingredient", etIngredient.getText().toString());
        outState.putString("amount", etAmount.getText().toString());
        outState.putLong("unit", spinnerUnits.getSelectedItemId());
        recipe.setCategory((int) spinnerCategories.getSelectedItemId() + 1);
        recipe.setDescription(etDescription.getText().toString());
        recipe.setName(etName.getText().toString());
        outState.putParcelable("recipe", recipe);
    }

    public void onBtnAddIngredientClick(View view) {
        //TODO: item is not highlighted on long click
        if (!etIngredient.getText().toString().equals("") && !etAmount.getText().toString().equals("")) {
            String ingredient = etIngredient.getText() + " "
                    + etAmount.getText()
                    + " " + spinnerUnits.getSelectedItem().toString();
            adapterIngredients.add(ingredient);
            adapterIngredients.notifyDataSetChanged();
            etAmount.setText("");
            etIngredient.setText("");
        } else {
            Toast toast = Toast.makeText(this, getString(R.string.fill_fields), Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        recipe.setName(etName.getText().toString());
        recipe.setDescription(etDescription.getText().toString());
        recipe.setCategory((int) spinnerCategories.getSelectedItemId() + 1);

        switch (id) {
            case android.R.id.home: {
                showDialog(db);

                return true;

            }

            case R.id.save_recipe:
                //noinspection SimplifiableIfStatement
            {

                return saveOrUpdate(db);

            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog(final SQLiteDatabase db) {
        if (!update) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecipeActivity.this);
            builder.setTitle(getString(R.string.save_recipe_title));
            builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveOrUpdate(db);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NavUtils.navigateUpFromSameTask(AddNewRecipeActivity.this);
                }
            });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewRecipeActivity.this);
            builder.setTitle(getString(R.string.save_recipe_title));
            builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveOrUpdate(db);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NavUtils.navigateUpFromSameTask(AddNewRecipeActivity.this);
                    Intent intent = new Intent(getApplicationContext(), ShowRecipeActivity.class);
                    intent.putExtra("recipeId", recipe.get_id());
                    startActivity(intent);
                    finish();
                }
            });
            builder.show();

        }
    }

    private boolean saveOrUpdate(SQLiteDatabase db) {
        if (!recipe.getDescription().equals("") && !recipe.getName().equals("")
                && recipe.getIngredients().size() != 0) {
            if (!update) {
                insertRecipe(db);

                return true;
            } else {
                updateRecipe(db);
                return true;
            }
        } else
            Toast.makeText(this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show();
        return false;
    }

    public void updateRecipe(SQLiteDatabase db) {
        db.beginTransaction();
        //update recipe
        int recipeId = recipe.get_id();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_RECIPE_NAME, recipe.getName());
        values.put(DBHelper.COLUMN_RECIPE_CATEGORY_ID, recipe.getCategory());
        values.put(DBHelper.COLUMN_RECIPE_DESCRIPTION, recipe.getDescription());
        db.update(DBHelper.TABLE_RECIPES, values, "_id=" + String.valueOf(recipeId), null);


        // delete images
        db.delete(DBHelper.TABLE_IMAGES, "recipe_id=" + String.valueOf(recipeId), null);
        //insert images
        if (recipe.getImageUris().size() != 0 ) {
            if(recipe.getImageUris().size()>1 && recipe.getImageUris().get(0).toString().equals("drawable"))
            {
                recipe.imageUris.remove(0);
            }
            for (Uri uri : recipe.imageUris) {
                ContentValues valuesImages = new ContentValues();
                valuesImages.put(DBHelper.COLUMN_IMAGE_URI, uri.toString());
                valuesImages.put(DBHelper.COLUMN_IMAGE_RECIPE_ID, recipeId);
                db.insert(DBHelper.TABLE_IMAGES, null, valuesImages);
            }
        } else {
            ContentValues valuesImages = new ContentValues();
            valuesImages.put(DBHelper.COLUMN_IMAGE_URI, "drawable");
            valuesImages.put(DBHelper.COLUMN_IMAGE_RECIPE_ID, recipeId);
            db.insert(DBHelper.TABLE_IMAGES, null, valuesImages);
        }

        //delete ingredients
        db.delete(DBHelper.TABLE_INGREDIENTS, "recipe_id=" + String.valueOf(recipeId), null);
        //insert ingredients to the db
        String[] s = getResources().getStringArray(R.array.units);
        List<String> strings = Arrays.asList(s);
        for (String ingredient : recipe.ingredients) {
            ContentValues valuesIngredient = new ContentValues();
            String[] split = ingredient.split(" ");
            int unitId = getIndex(strings, split[split.length - 1]);
            double amount = Double.parseDouble((split[split.length - 2]));
            String ingrName = "";
            for (int i = 0; i < split.length - 2; i++) {
                ingrName += split[i] + " ";
            }

            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_NAME, ingrName);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_AMOUNT, amount);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_UNIT_ID, unitId);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_RECIPE_ID, recipeId);

            db.insert(DBHelper.TABLE_INGREDIENTS, null, valuesIngredient);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        Intent intent = new Intent(getApplicationContext(), ShowRecipeActivity.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        DBHelper dbHelper = DBHelper.getInstance(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        recipe.setName(etName.getText().toString());
        recipe.setDescription(etDescription.getText().toString());
        recipe.setCategory((int) spinnerCategories.getSelectedItemId() + 1);
        showDialog(db);
    }

    public void insertRecipe(SQLiteDatabase db) {
        db.beginTransaction();
        //insert recipe to the db
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_RECIPE_NAME, recipe.getName());
        values.put(DBHelper.COLUMN_RECIPE_CATEGORY_ID, recipe.getCategory());
        values.put(DBHelper.COLUMN_RECIPE_DESCRIPTION, recipe.getDescription());
        int recipeId = (int) db.insert(DBHelper.TABLE_RECIPES, null, values);
        Log.i("name category_id", String.valueOf(recipe.getCategory()));

        //insert images to the db
        if (recipe.getImageUris().size() != 0) {
            for (Uri uri : recipe.imageUris) {
                ContentValues valuesImages = new ContentValues();
                valuesImages.put(DBHelper.COLUMN_IMAGE_URI, uri.toString());
                valuesImages.put(DBHelper.COLUMN_IMAGE_RECIPE_ID, recipeId);
                Log.i("name uri", String.valueOf(uri.toString()));
                db.insert(DBHelper.TABLE_IMAGES, null, valuesImages);
            }
        } else {
            ContentValues valuesImages = new ContentValues();
            valuesImages.put(DBHelper.COLUMN_IMAGE_URI, "drawable");
            valuesImages.put(DBHelper.COLUMN_IMAGE_RECIPE_ID, recipeId);
            db.insert(DBHelper.TABLE_IMAGES, null, valuesImages);
        }

        //insert ingredients to the db
        String[] s = getResources().getStringArray(R.array.units);
        List<String> strings = Arrays.asList(s);
        for (String ingredient : recipe.ingredients) {
            ContentValues valuesIngredient = new ContentValues();
            String[] split = ingredient.split(" ");
            int unitId = getIndex(strings, split[split.length - 1]);
            double amount = Double.parseDouble(split[split.length - 2]);
            String ingrName = "";
            for (int i = 0; i < split.length - 2; i++) {
                ingrName += split[i] + " ";
            }

            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_NAME, ingrName);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_AMOUNT, amount);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_UNIT_ID, unitId);
            valuesIngredient.put(DBHelper.COLUMN_INGREDIENT_RECIPE_ID, recipeId);

            db.insert(DBHelper.TABLE_INGREDIENTS, null, valuesIngredient);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        setResult(Activity.RESULT_OK, new Intent());
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_recipe_menu, menu);
        return true;
    }

    public void onButtonAddImgClick(View view) {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File

        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));
        }
        Intent chooser = Intent.createChooser(galleryIntent, getString(R.string.add_image));
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        startActivityForResult(chooser, PICK_IMAGE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/CookingBook/");
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        image.delete();
        Log.d("mCurrentPhotoPath1", mCurrentPhotoPath);
        return image;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void copyFile(String selectedImagePath, String string) throws IOException {
        InputStream in = new FileInputStream(selectedImagePath);
        OutputStream out = new FileOutputStream(string);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public Integer getIndex(List<?> list, Object item) {
        int i = 0;
        for (Object o : list) {
            if (item.equals(o)) {
                return i;
            }
            i++;

        }
        return null;
    }


}
