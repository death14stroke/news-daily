package com.andruid.magic.newsdaily.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.fragment.NewsFragment;
import com.cleveroad.loopbar.adapter.ILoopBarPagerAdapter;

import java.util.List;

import static com.andruid.magic.newsdaily.data.Constants.NAV_ITEMS;
import static com.andruid.magic.newsdaily.data.Constants.POS_BIZ;
import static com.andruid.magic.newsdaily.data.Constants.POS_ENT;
import static com.andruid.magic.newsdaily.data.Constants.POS_GEN;
import static com.andruid.magic.newsdaily.data.Constants.POS_HEALTH;
import static com.andruid.magic.newsdaily.data.Constants.POS_SCIENCE;
import static com.andruid.magic.newsdaily.data.Constants.POS_SPORTS;
import static com.andruid.magic.newsdaily.data.Constants.POS_TECH;

public class CustomPagerAdapter extends FragmentStatePagerAdapter implements ILoopBarPagerAdapter {
    private Context context;
    private List<String> categories;

    public CustomPagerAdapter(Context context, FragmentManager fm, List<String> categories) {
        super(fm);
        this.context = context;
        this.categories = categories;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if(position >= 0 && position < categories.size())
            fragment = NewsFragment.newInstance(categories.get(position));
        return fragment;
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "News";
        switch (position){
            case POS_GEN:
                title = context.getString(R.string.general);
                break;
            case POS_BIZ:
                title = context.getString(R.string.business);
                break;
            case POS_ENT:
                title = context.getString(R.string.entertainment);
                break;
            case POS_HEALTH:
                title = context.getString(R.string.health);
                break;
            case POS_SCIENCE:
                title = context.getString(R.string.science);
                break;
            case POS_SPORTS:
                title = context.getString(R.string.sports);
                break;
            case POS_TECH:
                title = context.getString(R.string.technology);
        }
        return title;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Drawable getPageDrawable(int position) {
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        switch (position){
            case POS_GEN:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_general);
                break;
            case POS_BIZ:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_business);
                break;
            case POS_ENT:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_entertainment);
                break;
            case POS_HEALTH:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_health);
                break;
            case POS_SCIENCE:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_science);
                break;
            case POS_SPORTS:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_sports);
                break;
            case POS_TECH:
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_tech);
        }
        return drawable;
    }

    @Override
    public int getCount() {
        return NAV_ITEMS;
    }
}