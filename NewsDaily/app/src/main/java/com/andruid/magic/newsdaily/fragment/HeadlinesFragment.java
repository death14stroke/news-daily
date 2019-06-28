package com.andruid.magic.newsdaily.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.FragmentHeadlinesBinding;
import com.andruid.magic.newsdaily.util.AssetsUtil;
import com.andruid.magic.newsdaily.util.StringUtils;
import com.cleveroad.loopbar.widget.OnItemClickListener;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class HeadlinesFragment extends Fragment implements OnItemClickListener {
    private FragmentHeadlinesBinding binding;
    private static final String TAG = "assetslog";
    private List<String> categories;
    private String category;

    public static HeadlinesFragment newInstance() {
        return new HeadlinesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCategories();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHeadlinesBinding.inflate(inflater, container, false);
        binding.loopBar.addOnItemClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onItemClicked(int position) {
        Timber.tag(TAG).d("clicked: %d", position);
        if(position >= 0 && position < categories.size()) {
            category = categories.get(position);
            Fragment fragment = NewsFragment.newInstance(category);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            Objects.requireNonNull(getActivity()).setTitle(StringUtils.capFirstLetter(category));
        }
    }

    private void loadCategories() {
        try {
            categories = AssetsUtil.readCategories(Objects.requireNonNull(getContext()).getAssets());
            loadFirstFrag();
            Timber.tag(TAG).d("categories try %d", categories.size());
        } catch (IOException e) {
            Timber.tag(TAG).d("categories catch");
            e.printStackTrace();
        }
    }

    private void loadFirstFrag() {
        category = categories.get(0);
        Fragment fragment = NewsFragment.newInstance(category);
        getChildFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
        Objects.requireNonNull(getActivity()).setTitle(StringUtils.capFirstLetter(category));
    }
}