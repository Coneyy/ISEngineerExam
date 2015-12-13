package com.pastew.isengineerexam;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pastew.isengineerexam.data.Answer;
import com.pastew.isengineerexam.data.Answers;
import com.pastew.isengineerexam.data.FileParser;
import com.pastew.isengineerexam.online.AnswerSender;
import com.pastew.isengineerexam.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TestActivity extends Activity {

    public static final String SCORE = "SCORE";
    public static final String CURRENT_QUESTION = "CURRENT_QUESTION";

    private SoundPool soundPool;
    private int correctSoundID, wrongSoundId;
    boolean loaded = false;

    private ImageView questionView, answerAView, answerBView, answerCView;
    private List<View> answersViewList;

    private int[] questionsIds;
    private int currentQuestion;
    private Answers answers;
    private int score;
    private boolean online;

    private AnswerSender answerSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSounds();
        loadAllAnswers();

        setContentView(R.layout.activity_test);
        setupUI();

        Intent intent = getIntent();
        String mode = intent.getStringExtra(MenuActivity.MODE);

        int questionsNumber = intent.getIntExtra(MenuActivity.QUESTIONS_NUMBER, 10);
        int startQuestionID = intent.getIntExtra(MenuActivity.START_QUESTION_ID, 1);
        int endQuestionID = intent.getIntExtra(MenuActivity.END_QUESTION_ID, 20);
        online = intent.getBooleanExtra(MenuActivity.ONLINE, true);

        startRangeTest(questionsNumber, startQuestionID, endQuestionID, mode);
        currentQuestion = 0;

        showQuestion(questionsIds[currentQuestion]);

        if(online)
            answerSender = new AnswerSender(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SCORE, score);
        outState.putInt(CURRENT_QUESTION, currentQuestion);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        score = savedInstanceState.getInt(SCORE);
        currentQuestion = savedInstanceState.getInt(CURRENT_QUESTION);

        updateScoreTextView();
        showQuestion(questionsIds[currentQuestion]);
    }

    private void initSounds() {
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        correctSoundID = soundPool.load(this, R.raw.correct, 1);
        wrongSoundId = soundPool.load(this, R.raw.wrong, 1);
    }

    private void playSound(int soundID) {
        if (!loaded)
            return;

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        soundPool.play(soundID, volume, volume, 1, 0, 1f);
    }

    private void startRangeTest(int questionsNumber, int startQuestionID, int endQuestionID, String mode) {
        if(mode.equals(MenuActivity.RANDOM_RANGE_TEST_MODE))
            questionsIds = Utils.getRandomArray(questionsNumber, startQuestionID, endQuestionID);
        else if(mode.equals(MenuActivity.ORDER_RANGE_TEST_MODE))
            questionsIds = Utils.getArray(questionsNumber, startQuestionID, endQuestionID);
    }

    private void loadAllAnswers() {
        try {
            answers = FileParser.readAnswers(this.getAssets().open("answers.txt"));
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.cant_load_answers), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupUI() {
        ((TextView) findViewById(R.id.questionID)).setText("id: " + Integer.toString(currentQuestion));

        questionView = (ImageView) findViewById(R.id.question);
        answerAView = (ImageView) findViewById(R.id.answer_a);
        answerBView = (ImageView) findViewById(R.id.answer_b);
        answerCView = (ImageView) findViewById(R.id.answer_c);

        answerAView.setTag(Answer.A);
        answerBView.setTag(Answer.B);
        answerCView.setTag(Answer.C);

        answersViewList = new ArrayList<>();
        answersViewList.add(answerAView);
        answersViewList.add(answerBView);
        answersViewList.add(answerCView);

        Collections.shuffle(answersViewList);

        for (View v : answersViewList)
            v.setOnClickListener(answerClickListener);

        // next question after click anywhere
        findViewById(R.id.main_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestion >= questionsIds.length)
                    endTest();
                else
                    showQuestion(questionsIds[currentQuestion]);
            }
        });

    }

    View.OnClickListener answerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView userAnswer = (ImageView) v;

            Answer answer = (Answer) v.getTag();

            // disable answers buttons
            for (View view : answersViewList)
                view.setClickable(false);

            // user answer is correct
            if (answer.equals(answers.get(questionsIds[currentQuestion]))) {
                userAnswer.setBackgroundColor(getResources().getColor(R.color.correct));
                ++score;
                updateScoreTextView();
                playSound(correctSoundID);
            }
            // correct answer is unknown
            else if (answers.get(questionsIds[currentQuestion]).equals(Answer.UNKNOWN)) {
                userAnswer.setBackgroundColor(getResources().getColor(R.color.unknown));

                //user answer is wrong
            } else {
                // show correct answer
                for (View view : answersViewList)
                    if (view.getTag().equals(answers.get(questionsIds[currentQuestion]))) {
                        view.setBackgroundColor(getResources().getColor(R.color.correct));
                    }

                // highlight users wrong answer
                userAnswer.setBackgroundColor(getResources().getColor(R.color.wrong));
                playSound(wrongSoundId);
            }


            if(online)
                answerSender.sendAnswer(questionsIds[currentQuestion], answer.getString());

            currentQuestion++;

            findViewById(R.id.main_layout).setClickable(true);

            /* // delay instead of click for next question
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showQuestion(++currentQuestion);
                }
            }, 1000);
            */
        }
    };

    private void updateScoreTextView() {
        ((TextView) findViewById(R.id.score_tv)).setText(Integer.toString(score));
    }

    private void endTest() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads images for questionNumber and shows it on screen
     */
    private void showQuestion(int questionNumber) {
        findViewById(R.id.main_layout).setClickable(false);

        // we don't know the correct answer, show dialog
        if(answers.get(questionsIds[currentQuestion]).equals(Answer.UNKNOWN))
            findViewById(R.id.unknown_answer_dialog).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.unknown_answer_dialog).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.questionID)).setText("id: " + questionNumber);
        ((TextView) findViewById(R.id.current_question_tv)).setText(currentQuestion + 1 + "/" + questionsIds.length);

        questionView.setImageResource(getDrawableId(questionNumber, 'p'));

        answerAView.setImageResource(getDrawableId(questionNumber, 'a'));
        answerBView.setImageResource(getDrawableId(questionNumber, 'b'));
        answerCView.setImageResource(getDrawableId(questionNumber, 'c'));

        LinearLayout questionsContainer = ((LinearLayout) findViewById(R.id.questions_containter));
        questionsContainer.removeAllViews();

        Collections.shuffle(answersViewList);

        for (View v : answersViewList) {
            questionsContainer.addView(v);
            v.setBackgroundColor(getResources().getColor(R.color.inactive));
            v.setClickable(true);
        }
    }

    /**
     * @param questionNumber number of the question
     * @param type           p question <br/>
     *                       a A answer <br/>
     *                       b B answer <br/>
     *                       c C answer <br/>
     * @return id of the drawable (for example R.drawable.img1_p
     */
    private int getDrawableId(int questionNumber, char type) {
        StringBuilder fileName = new StringBuilder();
        fileName.append("img");
        fileName.append(questionNumber);
        fileName.append("_");
        fileName.append(type);

        return getResources().getIdentifier(fileName.toString(), "drawable", getPackageName());
    }
}
