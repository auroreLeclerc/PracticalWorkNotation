package upjv.auroreleclerc.practicalworknotation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.intellij.lang.annotations.Language;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "practical_work_notation";
	private static final int DATABASE_VERSION = 4;
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String query = "CREATE TABLE 'students' (" +
				"'reader' TEXT PRIMARY KEY, " +
				"'name' TEXT DEFAULT 'N/A', " +
				"'surname' TEXT DEFAULT 'N/A', " +
				"'team' INTEGER DEFAULT NULL REFERENCES teams(id));";
		db.execSQL(query);
		this.addStudent("666664", "Morningstar", "Lucifer", db);
		this.addStudent("205222001815", "Leclerc", "Aurore", db);
		query = "CREATE TABLE 'works' ('name' TEXT PRIMARY KEY NOT NULL);";
		db.execSQL(query);
		query = "CREATE TABLE 'questions' (" +
				"'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"'work' TEXT REFERENCES works(name) ON DELETE CASCADE, " +
				"'question' TEXT NOT NULL);";
		db.execSQL(query);
		query = "CREATE TABLE 'notation' (" +
				"'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"'student' TEXT REFERENCES students(reader) ON DELETE CASCADE, " +
				"'question' INTEGER REFERENCES questions(id) ON DELETE CASCADE, " +
				"'validated' BOOLEAN DEFAULT 1);";
		db.execSQL(query);
		query = "CREATE TABLE 'teams' (" +
				"'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"'work' INTEGER REFERENCES works(name) ON DELETE CASCADE);";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS students");
		db.execSQL("DROP TABLE IF EXISTS works");
		db.execSQL("DROP TABLE IF EXISTS questions");
		db.execSQL("DROP TABLE IF EXISTS notation");
		db.execSQL("DROP TABLE IF EXISTS teams");
		onCreate(db);
	}
	interface CursorFunction {
		void apply(Cursor cursor);
	}

	private SQLiteDatabase select(@Language("SQL") String sql, String[] selectionArgs, CursorFunction cursorFunction) {
		return this.select(sql, selectionArgs, cursorFunction, false, null);
	}
	private SQLiteDatabase select(@Language("SQL") String sql, String[] selectionArgs, CursorFunction cursorFunction, boolean keepAlive, SQLiteDatabase openedDb) {
		SQLiteDatabase db = (openedDb == null) ? this.getWritableDatabase() : openedDb;
		Cursor cursorCourses = db.rawQuery(
				sql,
				selectionArgs);
		if (cursorCourses.moveToFirst()) {
			do {
				cursorFunction.apply(cursorCourses);
			} while (cursorCourses.moveToNext());
		}
		if (!keepAlive) cursorCourses.close();
		return db;
	}
	public void addWork(String name, JSONArray questions) throws JSONException {
		ArrayList<String> newQuestions = new ArrayList<>();
		for (int i = 0; i < questions.length(); i++) {
			newQuestions.add(questions.getString(i));
		}
		this.addWork(name, newQuestions);
	}
	public void addWork(String name, ArrayList<String> questions) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		for (String question : questions) {
			values.put("name", name);
			db.insert("works", null, values);
			values.clear();
			values.put("work", name);
			values.put("question", question);
			db.insert("questions", null, values);
			values.clear();
		}
	}

	private void addStudent(String reader, String name, String surname, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put("reader", reader);
		values.put("name", name);
		values.put("surname", surname);
		db.insert("students", null, values);
	}

	public void addStudent(String reader, String name, String surname) {
		SQLiteDatabase db = this.getWritableDatabase();
		this.addStudent(reader, name, surname, db);
		db.close();
	}

	public ArrayList<String> getWorksNames() {
		ArrayList<String> names = new ArrayList<>();

		CursorFunction add = (Cursor cursorCourses) -> names.add(cursorCourses.getString(0));

		this.select("SELECT name FROM works;", null, add);

		return names;
	}

	public int getRegisteredStudents(String workName) {
		AtomicInteger registered = new AtomicInteger();

		CursorFunction add = (Cursor cursorCourses) -> registered.set(cursorCourses.getInt(0));

		this.select("SELECT COUNT(DISTINCT notation.student) FROM notation " +
						"JOIN questions ON notation.question=questions.id " +
						"JOIN works ON questions.work=works.name" +
						" WHERE works.name=?;",
				new String[]{workName}, add);

		return registered.get();
	}

	public HashMap<String, String> getStudent(String reader) {
		HashMap<String, String> student = new HashMap<>();

		CursorFunction add = (Cursor cursorCourses) -> {
			student.put("name", cursorCourses.getString(0));
			student.put("surname", cursorCourses.getString(1));
			student.put("reader", cursorCourses.getString(2));
			student.put("team", String.valueOf(cursorCourses.getInt(3)));
		};

		this.select("SELECT name, surname, reader, team FROM students WHERE reader=?;",
				new String[]{reader}, add);

		return student;
	}

	public int removeStudent(String reader) {
		SQLiteDatabase db = this.getWritableDatabase();
		int removed = db.delete("student", "reader=?", new String[]{reader});
		db.close();

		return removed;
	}

	public HashMap<Integer, String> getQuestions(String workName) {
		HashMap<Integer, String> questions = new HashMap<>();
		CursorFunction add = (Cursor cursorCourses) -> questions.put(cursorCourses.getInt(0), cursorCourses.getString(1));

		this.select("SELECT id, question FROM questions WHERE work=?;",
				new String[]{workName}, add);

		return questions;
	}

	public void setValidated(int questionId, String reader, boolean validated) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("validated", validated);
		int existing = db.update("notation", values, "question=? AND student=?", new String[]{Integer.toString(questionId), reader});
		values.clear();
		if (existing == 0) {
			values.put("student", reader);
			values.put("question", questionId);
			values.put("validated", validated);
			db.insert("notation", null, values);
		}
		db.close();
	}

	public boolean getValidated(int questionId, String reader) {
		AtomicBoolean validated = new AtomicBoolean(false);

		CursorFunction add = (Cursor cursorCourses) -> validated.set(cursorCourses.getInt(0) > 0);

		this.select("SELECT validated FROM notation WHERE question=? AND student=?;",
				new String[]{String.valueOf(questionId), reader}, add);

		return validated.get();
	}
	public String[] dumpTablesNames() {
		return this.dumpTablesNames(true);
	}
	public String[] dumpTablesNames(boolean excludeInternalTables) {
		ArrayList<String> namesList = new ArrayList<>();

		CursorFunction add = (Cursor cursorCourses) -> namesList.add(cursorCourses.getString(0));

		this.select("SELECT name FROM sqlite_master WHERE type='table';",
				null, add);

		if (excludeInternalTables) {
			for (int i = 0; i < namesList.size(); i++) {
				if (namesList.get(i).startsWith("android") || namesList.get(i).startsWith("sqlite")) {
					namesList.remove(i);
					i--;
				}
			}
		}

		String[] names = new String[namesList.size()];
		namesList.toArray(names);
		return names;
	}

	public ArrayList<String[]> dump() {
		String[] names = this.dumpTablesNames();
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<String[]> dumps = new ArrayList<>();
		for (String name : names) {
			String table = "";
			Cursor cursorCourses = db.rawQuery(
					"SELECT * FROM " + name + " ;",
					null);
			if (cursorCourses.moveToFirst()) {
				do {
					for (int i = 0; i < cursorCourses.getColumnCount(); i++) {
						if (table.length() != 0 && !table.endsWith("\n")) table += " ; ";
						table += cursorCourses.getString(i);
					}
					table += '\n';
				} while (cursorCourses.moveToNext());
			}
			cursorCourses.close();
			dumps.add(new String[]{name, table});
		}
		db.close();

		return dumps;
	}

	public ArrayList<ArrayList<String>> dumpStudentSubject() {
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		ArrayList<ArrayList<String>> students = new ArrayList<>();
		CursorFunction add = (Cursor cursorCourses) -> {
			ArrayList<String> student = new ArrayList<>();
			student.add(cursorCourses.getString(0));
			student.add(cursorCourses.getString(1));
			student.add(cursorCourses.getString(2));
			students.add(student);

		};
		SQLiteDatabase db = this.select("SELECT students.reader, students.name, students.surname FROM students;",
				null, add, true, null);

		ArrayList<String> works = new ArrayList<>();
		add = (Cursor cursorCourses) -> works.add(cursorCourses.getString(0));
		this.select("SELECT name FROM works;", null, add, true, db);

		HashMap<Integer, Integer> questions = new HashMap<>();
		for (String work : works) {
			for (ArrayList<String> student : students) {
				add = (Cursor cursorCourses) -> {
					questions.put(cursorCourses.getInt(0), 0);
				};
				this.select("SELECT id FROM questions " +
								"JOIN works ON questions.work=works.name " +
								"WHERE works.name=?;",
						new String[]{work}, add, true, db);
				for (Integer question : questions.keySet()) {
					add = (Cursor cursorCourses) -> {
						questions.put(question, cursorCourses.getInt(0));
					};
					this.select("SELECT notation.validated FROM notation " +
									"WHERE notation.student=? AND notation.question=?;",
							new String[]{student.get(0), String.valueOf(question)}, add, true, db);
				}
				ArrayList<String> currentResult = new ArrayList<>();
				currentResult.addAll(student);
				currentResult.add(work);
				for (Integer question : questions.keySet()) {
					currentResult.add(String.valueOf(questions.get(question)));
				}
				result.add(currentResult);
				questions.clear();
			}
		}
		db.close();

		return result;
	}

	public void setTeam(String reader, String newReader, String workName) {
		AtomicInteger team = new AtomicInteger();

		CursorFunction add = (Cursor cursorCourses) -> team.set(cursorCourses.getInt(0));

		SQLiteDatabase db = this.select("SELECT team FROM students WHERE reader=?;",
				new String[]{reader}, add, true, null);

		ContentValues values = new ContentValues();
		if (team.get() != 0) {
			values.put("team", team.get());
			db.update("students", values, "reader=?", new String[]{String.valueOf(newReader)});
		}
		else {
			values.put("work", workName);
			db.insert("teams", null, values);
			values.clear();

			AtomicInteger max = new AtomicInteger();

			add = (Cursor cursorCourses) -> max.set(cursorCourses.getInt(0));

			db = this.select("SELECT MAX(id) FROM teams;",
					null, add, true, db);

			values.put("team", max.get());
			db.update("students", values, "reader IN (?, ?)", new String[]{reader, newReader});
		}

		db.close();
	}

	public void setTeam(String reader, int teamId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put("team", teamId == 0 ? null : teamId);
		db.update("students", values, "reader=?", new String[]{String.valueOf(reader)});

		db.close();
	}

	public int getGroup(String reader) {
		AtomicInteger id = new AtomicInteger();

		CursorFunction add = (Cursor cursorCourses) -> id.set(cursorCourses.getInt(0));

		this.select("SELECT team FROM students WHERE reader=?;",
				new String[]{reader}, add);

		return id.get();
	}

	public HashMap<String, String[]> getStudents(int teamId) {
		HashMap<String, String[]> students = new HashMap<>();

		CursorFunction add = (Cursor cursorCourses) -> students.put(cursorCourses.getString(0), new String[]{cursorCourses.getString(1), cursorCourses.getString(2)});

		this.select("SELECT reader, name, surname FROM students WHERE team=?;",
				new String[]{String.valueOf(teamId)}, add);

		return students;
	}
}