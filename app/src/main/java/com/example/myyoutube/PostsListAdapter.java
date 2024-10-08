package com.example.myyoutube;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PostsListAdapter extends RecyclerView.Adapter<PostsListAdapter.PostViewHolder> implements Filterable {

    // Listener interface to handle post filtering results
    public interface PostsAdapterListener {
        void onPostsFiltered(int count);
    }

    // ViewHolder class to hold the views for each post item
    public class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView videoTitle;
        private final TextView videoDetails;
        private final ImageView ivPic;
        private final ImageView channelImage;

        // Constructor to initialize the views in each item
        private PostViewHolder(View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            videoDetails = itemView.findViewById(R.id.videoDetails);
            ivPic = itemView.findViewById(R.id.thumbnail);
            channelImage = itemView.findViewById(R.id.channelImage);
        }
    }

    private final LayoutInflater mInflater;
    private List<Video> posts = new ArrayList<>(); // List to hold current posts
    private List<Video> postsFull = new ArrayList<>(); // List to hold all posts (for filtering)
    private final Context context;
    private PostsAdapterListener listener; // Listener to notify about filter results
    private final VideoViewModel viewModel;

    // Constructor for initializing the adapter and listener
    public PostsListAdapter(Context context, PostsAdapterListener listener, VideoViewModel videoViewModel) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
        this.viewModel = videoViewModel;
    }

    // Create a new ViewHolder for each item
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.posts_layout, parent, false);
        return new PostViewHolder(itemView);
    }

    // Bind the data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (posts != null) {
            final Video current = posts.get(position);
            holder.videoTitle.setText(current.getTitle());
            holder.videoDetails.setText(current.getUploadedBy() + " • " + current.getViews() + " • " + "7 months ago");

            loadThumbnail(current, holder.ivPic);

            // Add click listener to each item
            holder.itemView.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, VideoViewActivity.class);
                intent.putExtra("videoId", current.get_id());
                context.startActivity(intent);
            });
        }
    }

    // Return the total number of items in the adapter
    @Override
    public int getItemCount() {
        if (posts != null)
            return posts.size();
        else return 0;
    }

    // Set the list of posts and notify the adapter
    public void setPosts(List<Video> s) {
        posts = s;
        postsFull = new ArrayList<>(s); // Copy for filtering purposes
        notifyDataSetChanged();
    }

    public void showLikedOnlyBy(String user) {
        ArrayList<Video> likedVideos = new ArrayList<>();
        for (Video video : postsFull) {
            if (video.getLikes().contains(user)) {
                likedVideos.add(video);
            }
        }
        posts = likedVideos;
        notifyDataSetChanged();
    }

    public void resetPosts() {
        posts = new ArrayList<>(postsFull);
        notifyDataSetChanged();
    }

    // Return the filter for the posts
    @Override
    public Filter getFilter() {
        return postFilter;
    }

    // Filter class to filter posts based on search input
    private Filter postFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Video> filteredList = new ArrayList<>();

            // Check if the search query is empty
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(postsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Video post : postsFull) {
                    // Filter posts by content or author
                    if (post.getTitle().toLowerCase().contains(filterPattern) || post.getUploadedBy().toLowerCase().contains(filterPattern)) {
                        filteredList.add(post);
                    }
                }
            }

            // Return the filtered results
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            posts.clear();
            posts.addAll((List) results.values);
            // Notify listener about the filtered results
            if (listener != null) {
                listener.onPostsFiltered(posts.size());
            }
            notifyDataSetChanged();
        }
    };

    public void loadThumbnail(Video video, ImageView imageView) {
        viewModel.downloadFile(video, FileType.THUMBNAIL).observe((LifecycleOwner) context, resource -> {
            if (resource != null && resource.getData() != null && resource.getData() == true) {
                // Create a file reference where the thumbnail is saved
                File thumbnailFile = FileType.THUMBNAIL.getFilePath(context, video);

                // Check if the file exists
                if (thumbnailFile.exists()) {
                    // Decode the image file into a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());

                    // Set the Bitmap into the ImageView
                    imageView.setImageBitmap(bitmap);
                } else {
                    // Handle the case where the file wasn't found (show placeholder)
                    imageView.setImageResource(R.drawable.baseline_broken_image_24);
                }
            } else {
                // Handle error or show placeholder
                imageView.setImageResource(R.drawable.baseline_broken_image_24);
            }
        });
    }
}
