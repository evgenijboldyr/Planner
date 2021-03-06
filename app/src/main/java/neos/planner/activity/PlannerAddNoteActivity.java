package neos.planner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.annotation.About;
import neos.planner.entity.DbNote;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 18.03.16.
 * Project: Planner
 *
 * Активити для создания новой заметки в приложении
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class PlannerAddNoteActivity extends AppCompatActivity {

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;
    private DbNote note = new DbNote();

    //Блок пременных для работы с пользовательскими данными
    @Bind(R.id.barNoteDetails) Toolbar toolbar;
    @Bind(R.id.mSingleNoteGroup) Spinner group;
    @Bind(R.id.mSingleNoteTitle) EditText title;
    @Bind(R.id.mSingleNoteText) EditText text;
    @Bind(R.id.fabEditNote) FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        ButterKnife.bind(this);

        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        toolbar.setTitle(R.string.add_note_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String[] groups = {
            getBaseContext().getString(R.string.note_group_general),
            getBaseContext().getString(R.string.note_group_favorites)
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        group.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    note.setTitle(title.getText().toString());
                    note.setNoteText(text.getText().toString());
                    note.setGroup(group.getSelectedItem().toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    note.setCreationDate(sdf.format(new Date()));
                    note.setUpdateDate(sdf.format(new Date()));
                    notesDAO.create(note);
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (title.getText().toString().equals("")
                && text.getText().toString().equals("")) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        } else {
            callSavingDialog();
        }
    }

    /*Метод в котором происходит вызов Диалога сохранения*/
    private void callSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.save_changes_dialog_message);
        builder.setPositiveButton(
                R.string.save_changes_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            note.setTitle(title.getText().toString());
                            note.setNoteText(text.getText().toString());
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            note.setCreationDate(sdf.format(new Date()));
                            note.setUpdateDate(sdf.format(new Date()));
                            note.setGroup(group.getSelectedItem().toString());
                            notesDAO.create(note);
                            setResult(RESULT_OK, new Intent());
                            finish();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton(
                R.string.save_changes_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}
