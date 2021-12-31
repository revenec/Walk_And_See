package mvf.mikevidev.walkandsee.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mvf.mikevidev.walkandsee.repositories.LoadingPlacesActivity;
import mvf.mikevidev.walkandsee.R;
import mvf.mikevidev.walkandsee.models.WalkAndSeePlace;
import mvf.mikevidev.walkandsee.viewmodels.PlacesActivity;

public class WalkAndSeePlaceAdapter extends RecyclerView.Adapter<WalkAndSeePlaceAdapter.MyViewHolder> {
    private List<WalkAndSeePlace> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvNamePlace;
        public TextView tvAddressPlace;
        public ImageView ivImagePlace;
        public TextView tvDistance;
        public CheckBox ivSelected;

        public MyViewHolder(View v) {
            super(v);

            this.tvNamePlace = (TextView) v.findViewById(R.id.namePlace);
            this.tvAddressPlace = (TextView) v.findViewById(R.id.tvAddress);
            this.ivImagePlace = (ImageView) v.findViewById(R.id.ivPlace);
            this.tvDistance = (TextView) v.findViewById(R.id.tvDistance);
            this.ivSelected = (CheckBox) v.findViewById(R.id.cbSelect);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WalkAndSeePlaceAdapter() {
        mDataset = LoadingPlacesActivity.lstWalkAndSeePlaces;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WalkAndSeePlaceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View container = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.places_view, parent, false);

        MyViewHolder vh = new MyViewHolder(container);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        int position = holder.getAdapterPosition();
        holder.tvNamePlace.setText(mDataset.get(position).getPlaceName());
        holder.tvAddressPlace.setText(mDataset.get(position).getPlaceAddress());
        holder.tvDistance.setText(mDataset.get(position).getPlaceDistance());
        holder.ivSelected.setChecked(false);
        if(PlacesActivity.isAllSelected == true)
        {
            holder.ivSelected.setChecked(true);
        }
        holder.ivSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Log.i("INSIDE_CHECK","Inside check");
                Log.i("INSIDE_CHECK","Value coming: " + isChecked);
                WalkAndSeePlace was = mDataset.get(position);
                was.setSelected(isChecked);
                if(was.isSelected() == true)
                {
                    holder.ivSelected.setButtonDrawable(R.drawable.placeselectedlogo);
                }
                else
                {
                    holder.ivSelected.setButtonDrawable(R.drawable.placeunselectedlogo);
                }

            }
        });
        Bitmap photo = mDataset.get(position).getPlacePhoto();
        if(photo == null)
        {
            holder.ivImagePlace.setImageResource(R.drawable.empty_house);
        }
        else
        {
            Log.i("PHOTO",photo.toString());
            photo = photo.copy(Bitmap.Config.RGBA_F16, true);
            photo = photo.copy(Bitmap.Config.RGBA_F16, false);
            holder.ivImagePlace.setImageBitmap(photo);
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
