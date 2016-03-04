package com.example.nikak.cookingbook;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by NIKAK on 12.02.2016.
 */
public class Recipe implements Parcelable{
    private String name = "";
    private int category = 1;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    private String categoryName = "";
    ArrayList<String> ingredients = new ArrayList<>();
    ArrayList<Uri> imageUris = new ArrayList<>();
    private String description = "";
    private int _id = 0;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    protected Recipe(Parcel in) {
        name = in.readString();
        category = in.readInt();
        ingredients = in.createStringArrayList();
        imageUris = in.createTypedArrayList(Uri.CREATOR);
        description = in.readString();
        _id = in.readInt();
        categoryName = in.readString();
    }

    public Recipe() {
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<Uri> getImageUris() {
        return imageUris;
    }

    public void setImageUris(ArrayList<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(category);
        dest.writeStringList(ingredients);
        dest.writeTypedList(imageUris);
        dest.writeString(description);
        dest.writeInt(_id);
        dest.writeString(categoryName);
    }

}
