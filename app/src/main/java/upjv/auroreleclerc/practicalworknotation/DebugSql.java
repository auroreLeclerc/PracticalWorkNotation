package upjv.auroreleclerc.practicalworknotation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class DebugSql extends AppCompatActivity {
	private final DatabaseHelper db = new DatabaseHelper(DebugSql.this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug_sql);

		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), db.dump()));

		TabLayout tabLayout = findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);
	}
}
