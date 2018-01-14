package com.pastew.isexam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pastew.isexam.data.Answer;
import com.pastew.isexam.data.Answers;
import com.pastew.isexam.data.FileParser;
import com.pastew.isexam.favourites.FavouritesQuestions;
import com.pastew.isexam.online.AnswerSender;
import com.pastew.isexam.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestActivity extends Activity {

    private SoundPool soundPool;
    private int correctSoundID, wrongSoundId;
    boolean loaded = false;

    private ImageView questionView, answerAView, answerBView, answerCView;
    private List<View> answersViewList;

    FavouritesQuestions favouritesQuestions;
    private Switch favSwitch;

    private int currentQuestion;
    private long questionStartTime;
    private Answers answers;

    private int[] questionsIds;
    private int[] scores;

    private AnswerSender answerSender;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(FinalStrings.ONLINE_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        initSounds();
        loadAllAnswers();
        checkDarkTheme();

        favouritesQuestions = new FavouritesQuestions(sharedPreferences);

        setContentView(R.layout.activity_test);
        setupUI();

        Intent intent = getIntent();
        questionsIds = intent.getIntArrayExtra(FinalStrings.QUESTIONS_IDS);

        startTest();

        showQuestion(questionsIds[currentQuestion]);

        answerSender = new AnswerSender(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(FinalStrings.SCORES, scores);
        outState.putIntArray(FinalStrings.QUESTIONS_IDS, questionsIds);
        outState.putInt(FinalStrings.CURRENT_QUESTION, currentQuestion);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        scores = savedInstanceState.getIntArray(FinalStrings.SCORES);
        questionsIds = savedInstanceState.getIntArray(FinalStrings.QUESTIONS_IDS);
        currentQuestion = savedInstanceState.getInt(FinalStrings.CURRENT_QUESTION);

        updateScoreTextView();
        showQuestion(questionsIds[currentQuestion]);
    }

    private void checkDarkTheme() {
        boolean isDarkLayout = sharedPreferences
                .getBoolean(getString(R.string.dark_layout_pref), false);

        if (isDarkLayout) {
            setTheme(R.style.AppDarkTheme);
        }
    }

    private void initSounds() {
        boolean isSound = sharedPreferences
                .getBoolean(getString(R.string.sound_pref), true);
        if (!isSound)
            return;

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

    private void startTest() {
        currentQuestion = 0;
        scores = new int[questionsIds.length];
        for (int i = 0; i < scores.length; ++i)
            scores[i] = 0;
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
                addQuestionToFavouritesIfNeeded(questionsIds[currentQuestion]);

                if (currentQuestion + 1 >= questionsIds.length)
                    endTest();
                else
                    showQuestion(questionsIds[++currentQuestion]);
            }
        });

        favSwitch = (Switch) findViewById(R.id.favourite_switch);
    }

    @Override
    public void onBackPressed() {
        addQuestionToFavouritesIfNeeded(questionsIds[currentQuestion]);
        super.onBackPressed();
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
                scores[currentQuestion] = 1;
                updateScoreTextView();
                playSound(correctSoundID);
            }
            // correct answer is unknown
            else if (answers.get(questionsIds[currentQuestion]).equals(Answer.UNKNOWN)) {
                userAnswer.setBackgroundColor(getResources().getColor(R.color.unknown));
                scores[currentQuestion] = 1;
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

            answerSender.sendAnswer(questionsIds[currentQuestion], answer.getString(), questionStartTime);

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

    private void addQuestionToFavouritesIfNeeded(int questionId) {
        if (favSwitch.isChecked())
            favouritesQuestions.addQuestionToFavourites(questionId);
        else
            favouritesQuestions.removeQuestionFromFavourites(questionId);
    }

    private void updateScoreTextView() {
        ((TextView) findViewById(R.id.score_tv)).setText(Integer.toString(Utils.getScore(scores)));
    }

    private void endTest() {
        Intent resultsIntent = new Intent(TestActivity.this, ResultsActivity.class);
        resultsIntent.putExtra(FinalStrings.SCORES, scores);
        resultsIntent.putExtra(FinalStrings.QUESTIONS_IDS, questionsIds);
        resultsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(resultsIntent);
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

        Log.d("APP", "SHOW QUESTION " + questionNumber);

        // we don't know the correct answer, show dialog
        if (answers.get(questionsIds[currentQuestion]).equals(Answer.UNKNOWN))
            findViewById(R.id.unknown_answer_dialog).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.unknown_answer_dialog).setVisibility(View.INVISIBLE);

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

        questionStartTime = System.currentTimeMillis();

        favSwitch.setChecked(favouritesQuestions.isQuestionFavourite(questionsIds[currentQuestion]));
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
        return getResources().getIdentifier("img" + questionNumber + "_" + type, "drawable", getPackageName());
    }
}
