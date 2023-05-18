package upjv.auroreleclerc.practicalworknotation;

import static java.security.AccessController.getContext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.AccessControlContext;
import java.util.ArrayList;

import upjv.auroreleclerc.practicalworknotation.scan.AddStudent;
import upjv.auroreleclerc.practicalworknotation.scan.AddWork;
import upjv.auroreleclerc.practicalworknotation.scan.WatchWork;

public class MainActivity extends AppCompatActivity {
	private LinearLayout linearLayout;
	private DatabaseHelper db = new DatabaseHelper(MainActivity.this);
	private final int EXPORT_CODE = 204;
	private final int CLIENT_EXPORT_CODE = 205;

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intention;
		switch (item.getItemId()) {
			case R.id.debug_sql:
				intention = new Intent(this, DebugSql.class);
				startActivity(intention);
				return true;
			case R.id.credits:
				intention = new Intent(this, Credits.class);
				startActivity(intention);
				return true;
			case R.id.export:
				intention = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(intention, EXPORT_CODE);
				return true;
			case R.id.client_export:
				intention = new Intent(Intent.ACTION_CREATE_DOCUMENT);
				intention.setType("text/csv");
				intention.putExtra(Intent.EXTRA_TITLE, "export.csv");
				intention.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(intention, CLIENT_EXPORT_CODE);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		super.onActivityResult(requestCode, resultCode, resultData);
		if (requestCode == EXPORT_CODE && resultCode == RESULT_OK) {
			ArrayList<String[]> dump = db.dump();
			Uri path = resultData.getData();
			DocumentFile folder = DocumentFile.fromTreeUri(this, path);
			for (String[] table : dump) {
				try {
					DocumentFile file = folder.createFile("text/csv", table[0] + ".csv");
					OutputStream stream = getContentResolver().openOutputStream(file.getUri());
					stream.write(table[1].getBytes());
					stream.close();
				} catch (IOException e) {
					Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
					Log.e("CSV EXPORT", e.toString());
				}
			}
		}
		else if (requestCode == CLIENT_EXPORT_CODE && resultCode == RESULT_OK) {
			ArrayList<ArrayList<String>> dump = db.dumpStudentSubject();
			StringBuilder csv = new StringBuilder();
			for (ArrayList<String> line: dump) {
				for (String element: line) {
					csv.append(element + " ; ");
				}
				csv.delete(csv.length() - 3, csv.length());
				csv.append('\n');
			}
			Uri path = resultData.getData();
			ContentResolver resolver = getContentResolver();
			try {
				OutputStream outputStream = resolver.openOutputStream(path);
				outputStream.write(csv.toString().getBytes());
				outputStream.close();
			} catch (IOException e) {
				Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
				Log.e("CSV EXPORT", e.toString());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.linearLayout = findViewById(R.id.layout);
		ArrayList<String> works = db.getWorksNames();
		for (String work: works) {
			MaterialButton button = new MaterialButton(this);
			button.setText("TP : " + work);
			button.setOnClickListener(view -> {
				Intent intention = new Intent(this, WatchWork.class);
				intention.putExtra("name", work);
				startActivity(intention);
			});
			linearLayout.addView(button, 0);
		}
		BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
		bottomNavigation.setItemIconTintList(null);
		bottomNavigation.getMenu().setGroupCheckable(0, false, true);

		bottomNavigation.setOnItemSelectedListener(item -> {
			Intent intention;
			switch (item.getItemId()) {
				case R.id.addStudent:
					intention = new Intent(MainActivity.this, AddStudent.class);
					startActivity(intention);
					return true;
				case R.id.addWork:
					intention = new Intent(MainActivity.this, AddWork.class);
					startActivity(intention);
					return true;
				case R.id.wifiDirect:
					intention = new Intent(MainActivity.this, WifiDirectActivity.class);
					startActivity(intention);
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		});
	}
}