package upjv.auroreleclerc.practicalworknotation.scan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import upjv.auroreleclerc.practicalworknotation.DatabaseHelper;
import upjv.auroreleclerc.practicalworknotation.R;

public class AddWork extends Scan {
	private TextView nameView;
	private TextView questionsView;
	private TextView bodyView;
	private String name = "N/A";
	private JSONArray questions;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_work);
		this.nameView = findViewById(R.id.tp_name);
		this.questionsView = findViewById(R.id.tp_questions);
		this.bodyView = findViewById(R.id.tp_body);

		initialiseDetectorsAndSources(findViewById(R.id.surface_view), Barcode.QR_CODE);
	}
	@SuppressLint("SetTextI18n")
    @Override
	public void dataExtractionToView(String barcodeData, SurfaceView surfaceView) {
		bodyView.post(() -> {
			try {
				JSONObject decoder = new JSONObject(barcodeData);
				AddWork.this.name = (String) decoder.get("name");
				AddWork.this.questions = (JSONArray) decoder.get("questions");
				nameView.setText(name);
				questionsView.setText(questions.length() + " questions");
				StringBuilder prettyBody = new StringBuilder();
				for (int i = 0; i < questions.length(); i++) {
					prettyBody.append(i + 1).append(". ").append(questions.getString(i)).append('\n');
				}
				bodyView.setText(prettyBody.toString());
			} catch (JSONException e) {
				nameView.setText("Invalid JSON format !!!");
				bodyView.setText(e.toString());
			} catch (Exception e) {
				nameView.setText("Internal Error");
				bodyView.setText(e.toString());
			}
		});
	}
	public void add(View view) {
		DatabaseHelper db = new DatabaseHelper(AddWork.this);
		try {
			db.addWork(name, questions);
			Intent intentionResult = new Intent();
			setResult(RESULT_OK, intentionResult);
			this.finish();
		} catch (Exception e) {
			nameView.setText("Could not add the TP");
			bodyView.setText(e.toString());
		}
	}
}
