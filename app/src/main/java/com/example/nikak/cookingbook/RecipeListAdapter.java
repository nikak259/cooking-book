package com.example.nikak.cookingbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
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

import butterknife.ButterKnife;

/**
 * Created by NIKAK on 09.02.2016.
 */
public class RecipeListAdapter extends SimpleCursorAdapter implements Filterable {

    LayoutInflater layoutInflater;
    Context context;

    public RecipeListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
        this.context = context;
    }

    @Override
    public Cursor getCursor() {
        return super.getCursor();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    public int getRecipeId(int position) {

        Cursor c = getCursor();
        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    String result = "";
                    result += c.getString(c.getColumnIndex("name")) + " " +
                            c.getInt(c.getColumnIndex("_id"));

                } while (c.moveToNext());
            }
        }

        c.moveToPosition(position);
        int _id = c.getInt(c.getColumnIndex("_id"));
        return _id;
    }

/*
    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_RECIPE_NAME));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        FilterQueryProvider filter = getFilterQueryProvider();
        if (filter != null) {
            return filter.runQuery(constraint);
        }
        Cursor cursor = getCursor();
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        String RAW_QUERY =
                "SELECT r._id as _id, r.name as name, img.image_uri as uri, MIN(img._id)" +
                        " FROM recipes r INNER JOIN images img" +
                        " ON r._id=img.recipe_id" +
                        "WHERE r.name=?"+
                        " GROUP BY r._id;";
        cursor = sqLiteDatabase.rawQuery(RAW_QUERY,new String[]{(String) constraint});

        return getCursor();
    }
*/


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.recipe_name);
        textView.setText(cursor.getString(cursor.getColumnIndex("name")));
        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);

        int width = 200, height = 200;
        String uri = cursor.getString(cursor.getColumnIndex("uri"));
        ImageRequest request;
        Log.d("uri in adapter", uri);
        if (uri.equals("drawable")) {
            request = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.egg)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
        } else {
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
        }
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(imageView.getController())
                .setImageRequest(request)
                .build();
        imageView.setController(controller);

        RoundingParams roundingParams = RoundingParams.asCircle();

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(context.getResources());
        builder.setRoundingParams(roundingParams);
        GenericDraweeHierarchy hierarchy = builder
                .setRoundingParams(roundingParams)
                .build();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        imageView.setLayoutParams(params);
        imageView.setHierarchy(hierarchy);


    }


}
