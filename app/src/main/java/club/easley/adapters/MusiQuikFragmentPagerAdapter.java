package club.easley.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import club.easley.musiquik.R;


public class MusiQuikFragmentPagerAdapter extends FragmentPagerAdapter {

    List<Fragment> listFragments;
    Context context;

    public MusiQuikFragmentPagerAdapter(Context context, FragmentManager fm, List<Fragment> listFragments) {
        super(fm);
        this.listFragments = listFragments;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getStringArray(R.array.tabs)[position];
    }


}
