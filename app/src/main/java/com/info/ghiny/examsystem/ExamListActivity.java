package com.info.ghiny.examsystem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.info.ghiny.examsystem.manager.ExamSubjectAdapter;
import com.info.ghiny.examsystem.database.ExamSubject;
import com.info.ghiny.examsystem.model.JsonHelper;
import com.info.ghiny.examsystem.model.ProcessException;

import java.util.List;

public class ExamListActivity extends AppCompatActivity {

    private ExamSubjectAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_list);

        listAdapter = new ExamSubjectAdapter();
        ListView paperList = (ListView)findViewById(R.id.paperInfoList);
        assert paperList != null;

        String message  = getIntent().getStringExtra(JsonHelper.LIST_LIST);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try{
            List<ExamSubject> subjects  = JsonHelper.parsePaperList(message);
            listAdapter.updatePapers(subjects);
        } catch (ProcessException err) {
            finish();
        }

        paperList.setAdapter(listAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
