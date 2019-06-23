package com.andruid.magic.newsdaily.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.ActivityDrawerBinding;
import com.andruid.magic.newsdaily.fragment.HeadlinesFragment;
import com.andruid.magic.newsdaily.pref.SettingsFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import static com.andruid.magic.newsdaily.data.Constants.ID_HEADLINES;
import static com.andruid.magic.newsdaily.data.Constants.ID_SETTINGS;

public class DrawerActivity extends AppCompatActivity {
    private ActivityDrawerBinding binding;
    private DrawerBuilder drawerBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);
        initDrawerBuilder();
        addDrawerItems();
        Drawer drawer = drawerBuilder.build();
        drawer.setSelection(ID_HEADLINES, true);
    }

    private void initDrawerBuilder() {
        drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(binding.toolBar)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    switch ((int) drawerItem.getIdentifier()){
                        case ID_HEADLINES:
                            loadFragment(HeadlinesFragment.newInstance());
                            break;
                        case ID_SETTINGS:
                            loadFragment(SettingsFragment.newInstance());
                            break;
                    }
                    return true;
                });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private PrimaryDrawerItem buildDrawerItem(int id, int nameRes, int icon){
        return new PrimaryDrawerItem()
                .withIdentifier(id)
                .withName(nameRes)
                .withIcon(icon)
                .withIconTintingEnabled(true)
                .withIconColorRes(R.color.colorPrimaryDark)
                .withTextColorRes(R.color.colorPrimaryDark);
    }

    private void addDrawerItems() {
        PrimaryDrawerItem headlinesItem = buildDrawerItem(ID_HEADLINES, R.string.headlines,
                R.drawable.ic_general);
        PrimaryDrawerItem settingsItem = buildDrawerItem(ID_SETTINGS, R.string.settings,
                R.drawable.ic_settings);
        drawerBuilder.addDrawerItems(headlinesItem, new DividerDrawerItem(), settingsItem);
    }
}