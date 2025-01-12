package upjv.auroreleclerc.practicalworknotation;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.HashMap;

import upjv.auroreleclerc.practicalworknotation.scan.AddToTeam;

public class EditWork extends AppCompatActivity {
	private final DatabaseHelper db = new DatabaseHelper(EditWork.this);
    private String workName;
	private String reader;
	private String name;
	private String surname;
	private int group;

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		HashMap<String, String[]> students = this.db.getStudents(this.group);
		StringBuilder title = new StringBuilder();
		for (String reader : students.keySet()) {
			if (title.length() > 0) title.append(" & ");
			title.append(students.get(reader)[0]).append(" ").append(students.get(reader)[1]);
		}
		if (title.length() == 0) title = new StringBuilder(this.name + " " + this.surname);
		setTitle(title.toString());
		inflater.inflate(R.menu.group_menu, menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_work);
        LinearLayout linearLayout = findViewById(R.id.layout);

		Intent intent = getIntent();
		this.reader = intent.getStringExtra("reader");
		this.workName = intent.getStringExtra("work_name");
		this.name = intent.getStringExtra("name");
		this.surname = intent.getStringExtra("surname");

		this.group = this.db.getGroup(this.reader);

		HashMap<Integer, String> questions = db.getQuestions(this.workName);
		HashMap<String, String[]> students = this.db.getStudents(this.group);

		for (Integer id: questions.keySet()) {
			MaterialCheckBox checkBox = new MaterialCheckBox(this);
			checkBox.setText(questions.get(id));
			checkBox.setId(id);
			checkBox.setChecked(db.getValidated(id, this.reader));
			checkBox.setOnClickListener(view -> {
				if (students.isEmpty()) db.setValidated(id, this.reader, checkBox.isChecked());
				else {
					for (String reader : students.keySet()) {
						db.setValidated(id, reader, checkBox.isChecked());
					}
				}
			});
			linearLayout.addView(checkBox);
		}
	}

	public void addToGroup(View view) {
		Intent intention = new Intent(EditWork.this, AddToTeam.class);
		intention.putExtra("reader", this.reader);
		intention.putExtra("work", this.workName);
		startActivity(intention);
	}
}
