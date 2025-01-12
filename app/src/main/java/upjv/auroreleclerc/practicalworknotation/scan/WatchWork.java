package upjv.auroreleclerc.practicalworknotation.scan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

import upjv.auroreleclerc.practicalworknotation.DatabaseHelper;
import upjv.auroreleclerc.practicalworknotation.EditWork;
import upjv.auroreleclerc.practicalworknotation.R;

public class WatchWork extends Scan {
	private String name;
	private final DatabaseHelper db = new DatabaseHelper(WatchWork.this);

	@SuppressLint("SetTextI18n")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		this.name = intent.getStringExtra("name");
		setContentView(R.layout.watch_work);

		((TextView) findViewById(R.id.tp_name)).setText("TP: " + this.name);
		int registeredStudents = db.getRegisteredStudents(this.name);
		((TextView) findViewById(R.id.registered_students)).setText(registeredStudents + " étudiant·e·s inscrit·e·s");

		initialiseDetectorsAndSources(findViewById(R.id.surface_view), Barcode.ITF);
	}
	@Override
	public void dataExtractionToView(String barcodeData, SurfaceView surfaceView) {
		HashMap<String, String> student = db.getStudent(barcodeData);
		if(student.isEmpty()) {
			runOnUiThread(() -> Snackbar.make(surfaceView, barcodeData + " is not a registered student", Snackbar.LENGTH_SHORT).show());
		}
		else {
			Intent intention = new Intent(this, EditWork.class);
			intention.putExtra("name", student.get("name"));
			intention.putExtra("surname", student.get("surname"));
			intention.putExtra("reader", barcodeData);
			intention.putExtra("work_name", this.name);
			startActivity(intention);
		}
	}
}
