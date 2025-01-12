package upjv.auroreleclerc.practicalworknotation.scan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

import upjv.auroreleclerc.practicalworknotation.DatabaseHelper;
import upjv.auroreleclerc.practicalworknotation.R;

public class AddToTeam extends Scan {
	private final DatabaseHelper db = new DatabaseHelper(AddToTeam.this);
	private String parentReader;
	private String workName;
	private TextView id;
	private TextView name;
	private TextView surname;
	private Button add;
	private String reader = "N/A";

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		String title = "Group selection";
		setTitle(title);
		inflater.inflate(R.menu.group_menu, menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_student);
		this.id = findViewById(R.id.id);
		this.name = findViewById(R.id.name);
		this.surname = findViewById(R.id.surname);
		Intent intention = getIntent();
		this.parentReader = intention.getStringExtra("reader");
		this.workName = intention.getStringExtra("work");
		this.add = findViewById(R.id.add);

		initialiseDetectorsAndSources(findViewById(R.id.surface_view), Barcode.ITF);
	}
	@Override
	public void dataExtractionToView(String barcodeData, SurfaceView surfaceView) {
		HashMap<String, String> student = this.db.getStudent(barcodeData);
		if(student.isEmpty()) {
			runOnUiThread(() -> Snackbar.make(surfaceView, barcodeData + " is not a registered student", Snackbar.LENGTH_SHORT).show());
		}
		else {
			HashMap<String, String[]> students = db.getStudents(Integer.parseInt(student.get("team")));
			if (!students.isEmpty()) {
				id.post(() -> {
					id.setText(barcodeData);
					name.setText(student.get("name"));
					surname.setText(student.get("surname"));
					add.setText("Séparer");
					this.reader=student.get("reader");
				});
			}
			else {
				id.post(() -> {
					id.setText(barcodeData);
					name.setText(student.get("name"));
					surname.setText(student.get("surname"));
				});
			}
		}
	}

	public void add(View view) {
		if (this.add.getText().toString().equals("Ajouter")) {
			this.db.setTeam(this.parentReader, id.getText().toString(), this.workName);
			Intent intentionResult = new Intent();
			setResult(RESULT_OK, intentionResult);
			this.finish();
		}
		else {
			new AlertDialog.Builder(this)
				.setTitle("Séparer")
				.setMessage("Etes vous sûr de retirer "+this.name.getText().toString()+" "+this.surname.getText().toString()+" ?")
				.setIcon(R.drawable.emoji_u1f9d1_200d_1f393)
				.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
					this.db.setTeam(this.reader, 0);
					Intent intentionResult = new Intent();
					setResult(RESULT_OK, intentionResult);
					this.finish();
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
		}
	}
}
