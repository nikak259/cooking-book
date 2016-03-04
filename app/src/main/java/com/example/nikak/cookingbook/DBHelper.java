package com.example.nikak.cookingbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by NIKAK on 09.02.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static DBHelper dbHelper;

    // Columns of the RECIPES table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_ID = "_id";
    public static final String COLUMN_RECIPE_NAME = "name";
    //public static final String COLUMN_RECIPE_TIME = "time";
    public static final String COLUMN_RECIPE_CATEGORY_ID = "category_id";
    public static final String COLUMN_RECIPE_DESCRIPTION = "description";

    // Columns of the INGREDIENTS table
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COLUMN_INGREDIENT_ID = "_id";
    public static final String COLUMN_INGREDIENT_NAME = "name";
    public static final String COLUMN_INGREDIENT_AMOUNT = "amount";
    public static final String COLUMN_INGREDIENT_UNIT_ID = "unit_id";
    public static final String COLUMN_INGREDIENT_RECIPE_ID = "recipe_id";

    // Columns of the  CATEGORIES table
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_DESCRIPTION = "description";

    // Columns of the IMAGES table
    public static final String TABLE_IMAGES = "images";
    public static final String COLUMN_IMAGE_ID = "_id";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_IMAGE_RECIPE_ID = "recipe_id";

    // Database info
    public static final String DATABASE_NAME = "cookingbook.db";
    public static final int DATABASE_VERSION = 1;

    // create table CATEGORIES
    public static final String SQL_CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " ("
                    + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , "
                    + COLUMN_CATEGORY_NAME + " TEXT NOT NULL, "
                    + COLUMN_CATEGORY_DESCRIPTION + " TEXT NOT NULL" + ");";

    // create table RECIPES
    public static final String SQL_CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " ("
                    + COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + COLUMN_RECIPE_NAME + " TEXT NOT NULL, "
                    + COLUMN_RECIPE_DESCRIPTION + " TEXT NOT NULL, "
                    + COLUMN_RECIPE_CATEGORY_ID + " INTEGER REFERENCES "
                    + TABLE_CATEGORIES + " (" + COLUMN_CATEGORY_ID + ") " + ");";

    // create table INGREDIENTS
    public static final String SQL_CREATE_TABLE_INGREDIENTS =
            "CREATE TABLE " + TABLE_INGREDIENTS + " ("
                    + COLUMN_INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + COLUMN_INGREDIENT_NAME + " TEXT NOT NULL,"
                    + COLUMN_INGREDIENT_AMOUNT + " REAL NOT NULL,"
                    + COLUMN_INGREDIENT_UNIT_ID + " TEXT NOT NULL, "
                    + COLUMN_INGREDIENT_RECIPE_ID + " INTEGER REFERENCES "
                    + TABLE_RECIPES + " (" + COLUMN_RECIPE_ID + ") " + " ON UPDATE CASCADE"
                    + " ON DELETE CASCADE " + " );";

    // create table IMAGES
    public static final String SQL_CREATE_TABLE_IMAGES =
            "CREATE TABLE " + TABLE_IMAGES + " ("
                    + COLUMN_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + COLUMN_IMAGE_URI + " TEXT NOT NULL, "
                    + COLUMN_IMAGE_RECIPE_ID + " INTEGER REFERENCES "
                    + TABLE_RECIPES + " (" + COLUMN_RECIPE_ID + ") " + ");";

    Context context;


    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null)
            dbHelper = new DBHelper(context);
        return dbHelper;

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES);
        db.execSQL(SQL_CREATE_TABLE_RECIPES);
        db.execSQL(SQL_CREATE_TABLE_INGREDIENTS);
        db.execSQL(SQL_CREATE_TABLE_IMAGES);

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, "Other");
        values.put(COLUMN_CATEGORY_DESCRIPTION, "Description of the other category");
        db.insert(TABLE_CATEGORIES, null, values);

        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_CATEGORY_NAME, "Dessert");
        values2.put(COLUMN_CATEGORY_DESCRIPTION, "Anything sweet you should place in this category");
        db.insert(TABLE_CATEGORIES, null, values2);

        ContentValues values3 = new ContentValues();
        values3.put(COLUMN_CATEGORY_NAME, "Soups");
        values3.put(COLUMN_CATEGORY_DESCRIPTION, "Description");
        db.insert(TABLE_CATEGORIES, null, values3);

        ContentValues values4 = new ContentValues();
        values4.put(COLUMN_CATEGORY_NAME, "Salads");
        values4.put(COLUMN_CATEGORY_DESCRIPTION, "Anything");
        db.insert(TABLE_CATEGORIES, null, values4);



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);

    }
}
