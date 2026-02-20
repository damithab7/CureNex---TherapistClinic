package lk.damithab.curenex.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import lk.damithab.curenex.fragment.PastBookingsFragment;
import lk.damithab.curenex.fragment.UpcomingBookingsFragment;

public class ViewBookingsViewPagerAdapter extends FragmentStateAdapter {

    public ViewBookingsViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new UpcomingBookingsFragment();
        }
        return new PastBookingsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
