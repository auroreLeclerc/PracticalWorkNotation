package upjv.auroreleclerc.practicalworknotation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyFragment extends Fragment {
	String text;

	public MyFragment(String text) {
		this.text = text;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment, container, false);
		((TextView) view.findViewById(R.id.textView)).setText(this.text);
		return view;
	}
}
