package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class StringRecyclerAdapter extends RecyclerView.Adapter<StringRecyclerAdapter.StringViewHolder> {
    private List<String> stringList;
    private boolean announce = false;
    private View.OnClickListener onClickListener = null;

    public StringRecyclerAdapter(List<String> strings) {
        this(strings, true);
    }

    public StringRecyclerAdapter(String[] strings) {
        this(strings, true);
    }

    public StringRecyclerAdapter(List<String> strings, boolean announce)
    {
        this.stringList = strings;
        this.announce = announce;
    }

    public StringRecyclerAdapter(String[] strings, boolean announce)
    {
        this.stringList = Arrays.asList(strings);
        this.announce = announce;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        onClickListener = listener;
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(StringViewHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
    }

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new StringViewHolder(itemView, onClickListener);
    }


    public class StringViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        public StringViewHolder(View v, @Nullable View.OnClickListener listener)
        {
            super(v);
            title = v.findViewById(R.id.text1);
            v.setOnClickListener((listener == null) ? this : listener);
        }

        @Override
        public void onClick(View v) {
            if (announce) Toast.makeText(v.getContext(), title.getText(), Toast.LENGTH_SHORT).show();
        }

    }
}
