package upjv.auroreleclerc.practicalworknotation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MyPagerAdapter extends FragmentPagerAdapter {
	private final ArrayList<String[]> body;

	public MyPagerAdapter(@NonNull FragmentManager fm, ArrayList<String[]> body) {
		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		this.body = body;
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return this.body.get(position)[0];
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		return new MyFragment(this.body.get(position)[1]);
	}

	@Override
	public int getCount() {
		return this.body.size();
	}
}
