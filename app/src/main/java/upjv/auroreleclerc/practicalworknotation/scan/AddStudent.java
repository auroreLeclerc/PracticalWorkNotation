package upjv.auroreleclerc.practicalworknotation.scan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.HashMap;

import upjv.auroreleclerc.practicalworknotation.DatabaseHelper;
import upjv.auroreleclerc.practicalworknotation.R;

public class AddStudent extends Scan {
	private TextView id;
	private TextView name;
	private TextView surname;
	private Button add;
	private DatabaseHelper db = new DatabaseHelper(AddStudent.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_student);
		this.id = findViewById(R.id.id);
		this.name = findViewById(R.id.name);
		this.surname = findViewById(R.id.surname);
		this.add = findViewById(R.id.add);

		initialiseDetectorsAndSources(findViewById(R.id.surface_view), Barcode.ITF);
	}

	public void add(View view) {
		if (this.add.getText().toString().equals("Ajouter")) {
			this.db.addStudent(this.id.getText().toString(), this.name.getText().toString(), this.surname.getText().toString());
			Intent intentionResult = new Intent();
			setResult(RESULT_OK, intentionResult);
			this.finish();
		}
		else {
			new AlertDialog.Builder(this)
					.setTitle("Suppression")
					.setMessage("Etes vous sûr de supprimer "+this.name.getText().toString()+" "+this.surname.getText().toString()+" ?")
					.setIcon(R.drawable.emoji_u1f9d1_200d_1f393)
					.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> this.db.removeStudent(this.id.getText().toString()))
					.setNegativeButton(android.R.string.no, null)
					.show();
		}
	}


	@Override
	public void dataExtractionToView(String barcodeData, SurfaceView surfaceView) {
		HashMap<String, String> student = db.getStudent(barcodeData);
		if (!student.isEmpty()) {
			id.post(() -> {
				id.setText(barcodeData);
				name.setText(student.get("name"));
				surname.setText(student.get("surname"));
				add.setText("Supprimer");
			});
		}
		else {
			id.post(() -> {
				id.setText(barcodeData);
				name.setText(null);
				surname.setText(null);
				add.setText("Ajouter");
			});
		}
	}
}
