package com.example.myspeaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SpeakerActivity extends Activity implements OnInitListener {
    private EditText inputText;
    private Button speakButton;
    public static Button speak;
    public final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech tts;
    private int MY_DATA_CHECK_CODE = 0;
    Button play, record, save, rec, clear;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private static final String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
            + "helper";
    String filepath;
    String play_text;
    Context context = this;
    boolean mStartPlaying = true;
    boolean mStartRecording = true;
    List<VoiceRecording> allrecords;
    boolean isplaying = true;
    ArrayAdapter<String> adapter;
    CustomListAdapter cadapter;
    AudioManager am;
    ListView listvew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker);
        // c adapter=new CustomListAdapter();
        speak = (Button) findViewById(R.id.speak);
        // code to access sdcard if mounted else using the internal memory
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            Toast.makeText(context, "sdcard is not mounted on your device", Toast.LENGTH_LONG).show();


        // to get External SdCard path on devices if it available.
        //by using Environment.getExternalStorageDirectory().getAbsolutePath() I can get the path to the Internal Storage.
        //So I used below class for detecting External storage.


        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        long megAvailable = bytesAvailable / 1048576;
        System.out.println("Megs :" + megAvailable);
        // crated one image store in sd card to save that image and then i need that image path and name of the image pls tell how to get the name and path of the image
        File fileDir = new File(mFileName);
        System.out.println(fileDir.getAbsolutePath());
        if (!fileDir.exists()) {
            System.out.println(fileDir.mkdirs() || fileDir.isDirectory());
        }
        //I have .3gp audio file which is stored in SD Card.I want to copy that file into another folder of sd card
        File audioFile = new File(fileDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".3gp");
        if (!audioFile.exists())
            try {
                audioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        Log.d("file name", mFileName + ".");
        filepath = audioFile.getAbsolutePath();

        inputText = (EditText) findViewById(R.id.play_text);
        showAllRecordings();
        inputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // SpeakerActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = inputText.getText().toString().toLowerCase().trim();
                cadapter.filter(text);
            }
        });
        tts = new TextToSpeech(this, this);// ,"com.googlecode.eyesfree.espeak");

        speakButton = (Button) findViewById(R.id.speak_btn);
        speakButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String input = inputText.getText().toString();
                if (input != null && input.length() > 0) {
                    Toast.makeText(SpeakerActivity.this, "saying " + input, Toast.LENGTH_LONG).show();
                    tts.speak(input, TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        Intent checkIntent = new Intent();
        // to check for the presence of the TTS resources with the corresponding intent:"
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        record = (Button) findViewById(R.id.new_btn);
        record.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (record.getText().equals("All Records")) {
                    findViewById(R.id.clear_btn).setVisibility(View.VISIBLE);
                    record.setText("Create Record");
                    Drawable left = getResources().getDrawable(R.drawable.add_icon);
                    left.setBounds(0, 0, 24, 24);
                    record.setCompoundDrawables(left, null, null, null);
                    findViewById(R.id.newrec_layout).setVisibility(View.GONE);
                    findViewById(R.id.rec_list).setVisibility(View.VISIBLE);
                    findViewById(R.id.all_title).setVisibility(View.VISIBLE);
                    showAllRecordings();
                } else {
                    findViewById(R.id.clear_btn).setVisibility(View.GONE);
                    record.setText("All Records");
                    Drawable left = getResources().getDrawable(R.drawable.back_icon);
                    left.setBounds(0, 0, 24, 24);
                    record.setCompoundDrawables(left, null, null, null);
                    findViewById(R.id.newrec_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.rec_list).setVisibility(View.GONE);
                    findViewById(R.id.all_title).setVisibility(View.GONE);
                    rec = (Button) findViewById(R.id.rec_btn);
                    rec.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            onRecord(mStartRecording);
                            if (mStartRecording) {
                                rec.setText("Stop");
                                rec.setBackgroundColor(Color.GREEN);
                            } else {
                                rec.setBackgroundColor(Color.GRAY);
                                rec.setText("Record");
                            }
                            mStartRecording = !mStartRecording;
                        }
                    });
                    Button save = (Button) findViewById(R.id.save_btn);
                    save.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mRecorder != null) {
                                mRecorder.stop();
                                mRecorder.release();
                            }
                            if (mPlayer != null) {
                                if (mPlayer.isPlaying()) {
                                    mPlayer.stop();
                                }
                            }
                            String rectxt = ((EditText) findViewById(R.id.rec_text)).getText().toString();
                            if (rectxt.length() < 1) {
                                new AlertDialog.Builder(context).setMessage("Please enter some text to save")
                                        .setTitle("error").setPositiveButton("ok", null).show();
                            } else {
                                findViewById(R.id.clear_btn).setVisibility(View.VISIBLE);
                                record.setText("Add Record");
                                VoiceRecording vr = new VoiceRecording(rectxt, filepath);
                                new SpeakerDB(context).save(vr);
                                findViewById(R.id.newrec_layout).setVisibility(View.GONE);
                                findViewById(R.id.rec_list).setVisibility(View.VISIBLE);
                                findViewById(R.id.all_title).setVisibility(View.VISIBLE);
                                showAllRecordings();
                            }
                        }
                    });
                }
            }
        });
        clear = (Button) findViewById(R.id.clear_btn);
        clear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final List<VoiceRecording> delist = cadapter.getCheckedList();

                if (delist.size() > 0) {
                    for (VoiceRecording v : delist) {
                        Log.d("item", v.toString());
                    }
                    new AlertDialog.Builder(context)
                            .setMessage("Are you sure to delete all the recordings?" + delist.size())
                            .setTitle("Confirm here").setPositiveButton("ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (VoiceRecording v : delist) {
                                new SpeakerDB(context).delete(v);
                                allrecords.remove(v);
                                showAllRecordings();
                                File speakfile = new File(v.getFilename());
                                System.out.println("deleted " + speakfile + " " + speakfile.delete());
                            }
                        }
                    }).setNegativeButton("Cancel", null).show();
                } else {
                    new AlertDialog.Builder(context).setMessage("No recordings are selected to delete..")
                            .setTitle("Confirmation").setPositiveButton("ok", null).show();
                }
            }
        });
        if (mPlayer != null)
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("mplayer", "completion");
                    mStartPlaying = true;
                }
            });
        SeekBar volume = (SeekBar) findViewById(R.id.seekbar);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
        volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });
        speak.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode1, int resultCode1, Intent data1) {
        super.onActivityResult(requestCode1, resultCode1, data1);

        switch (requestCode1) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode1 == RESULT_OK && null != data1) {

                    ArrayList<String> result = data1.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputText.setText(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (mPlayer != null) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
        }
    }

    private void showAllRecordings() {
        // TODO Auto-generated method stub
        allrecords = new SpeakerDB(context).getAllRecords();
        cadapter = new CustomListAdapter(allrecords);
        List<String> texts = new ArrayList<String>();
        for (VoiceRecording voiceRecording : allrecords) {
            texts.add(voiceRecording.getPlayText());
        }
        listvew = (ListView) findViewById(R.id.rec_list);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, texts);
        // listvew.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listvew.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, int arg2, long arg3) {

                arg0.setBackgroundColor(new Color().parseColor("#ebf4fa"));
                arg1.setBackgroundColor(Color.GREEN);

                filepath = allrecords.get(arg2).getFilename();
                isplaying = true;

                onPlay(isplaying);
                isplaying = !isplaying;
                mPlayer.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isplaying = false;
                        new Color();
                        arg1.setBackgroundColor(Color.parseColor("#ebf4fa"));
                    }
                });
            }
        });
        listvew.setAdapter(cadapter);// (adapter);
    }

    private void onRecord(boolean start) {
        if (start) {
            try {
                startRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stopRecording();
        }
    }

    private void startRecording() throws IOException {
        File fileDir = new File(mFileName);
        System.out.println(fileDir.getAbsolutePath());
        if (!fileDir.exists()) {
            System.out.println(fileDir.mkdirs() || fileDir.isDirectory());
        }
        File audioFile = new File(fileDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".3gp");
        try {
            if (!audioFile.exists()) {
                audioFile.createNewFile();
            }
            filepath = audioFile.getAbsolutePath();
            Log.d("record file", filepath + "..");

            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(audioFile.getAbsolutePath());
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("catch", "io exception");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(context).setMessage("Not possible to create the recording").setTitle("Confirmation")
                    .setPositiveButton("ok", null).show();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        Log.d("play file", filepath);
        mPlayer = new MediaPlayer();
        try {
            Log.d("myfile", filepath);
            mPlayer.setDataSource(filepath);
            mPlayer.prepare();
            mPlayer.start();
            if (mPlayer != null)
                mPlayer.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("mplayer", "completion");
                        mStartPlaying = true;
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("catch", "io exception");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        } else if (status == TextToSpeech.ERROR) {
        }
    }

    protected void onActivityResult1(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);

            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    private static int lastclicked = -1;
    private static int nowplaying = -1;

    class CustomListAdapter extends BaseAdapter {
        List<VoiceRecording> templist = null;
        List<VoiceRecording> vlist = null;
        List<VoiceRecording> checklist = new ArrayList<VoiceRecording>();
        List<Button> previous = new ArrayList<Button>();
        Button prebtn = null;

        public CustomListAdapter() {
            super();
        }

        public CustomListAdapter(List<VoiceRecording> templist) {
            super();
            this.templist = templist;
            vlist = new ArrayList<VoiceRecording>();
            vlist.addAll(templist);
        }

        public void filter(String charText) {
            charText = charText.toLowerCase();
            templist.clear();
            if (charText.length() == 0) {
                templist.addAll(vlist);
            } else {
                for (VoiceRecording v : vlist) {
                    if (v.getPlayText().toLowerCase().contains(charText)) {
                        templist.add(v);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return templist.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return templist.get(arg0);
        }

        public List<VoiceRecording> getCheckedList() {
            return checklist;
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View convertview, final ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = convertview;
            Viewholder vholder = new Viewholder();
            // if(row==null){
            row = inflater.inflate(R.layout.list_item, parent, false);
            vholder.txtvew = (TextView) row.findViewById(R.id.content);
            vholder.chkbx = (CheckBox) row.findViewById(R.id.item_check);

            row.setTag(vholder);
            // }else{
            // vholder=(Viewholder) row.getTag();
            // }
            // (checklist.contains(vholder.chkbx.getTag()));
            TextView content, detail;
            content = (TextView) row.findViewById(R.id.content);
            content.setText(templist.get(position).getPlayText());
            final CheckBox check = (CheckBox) row.findViewById(R.id.item_check);
            check.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("check button", templist.get(position).toString());
                    if (check.isChecked()) {
                        checklist.add(templist.get(position));
                        check.setTag(templist.get(position));
                    } else {
                        if (checklist.contains(templist.get(position)))
                            checklist.remove(templist.get(position));
                    }
                }
            });
            check.setChecked(checklist.contains(templist.get(position)));
            /*
             * vholder.txtvew.setText(templist.get(position).getPlayText());
             * if(checklist.contains(vholder.chkbx.getTag())){ VoiceRecording
             * t=(VoiceRecording) (vholder.chkbx.getTag());
             * if(vholder.txtvew.getText().equals(t.getPlayText()))
             * vholder.chkbx.setChecked(true) ; }
             */
            final Drawable playd = getResources().getDrawable(R.drawable.play);
            final Drawable paused = getResources().getDrawable(R.drawable.pause);
            final Button playthis = (Button) row.findViewById(R.id.item_play);
            playthis.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    System.out.println("previous size " + previous.size());
                    /*
                     * for (Button b : previous) {
                     * b.setBackgroundDrawable(playd); b.setTag("Play"); }
                     */
                    previous.clear();
                    if (prebtn != null && prebtn != playthis) {
                        prebtn.setBackgroundDrawable(playd);
                        prebtn.setTag("Play");
                    }
                    System.out.println("lastclicked " + lastclicked);
                    /*
                     * if(lastclicked!=-1 && lastclicked!=position){
                     * System.out.println("lastclicked "+lastclicked);
                     * if(mPlayer!=null){ if(mPlayer.isPlaying())
                     * mPlayer.stop(); }
                     *
                     * //parent.getRootView(); ListView dvew=(ListView)
                     * v.getParent().getParent(); LinearLayout
                     * lastview=(LinearLayout) listvew.getChildAt(lastclicked);
                     * //getChildAt(lastclicked).findViewById(R.id.item_play);
                     * System.out.println("childcount"+lastview.getChildCount())
                     * ; Button b=(Button)
                     * lastview.findViewById(R.id.item_play);
                     * b.setBackgroundDrawable(playd); }
                     */
                    if (((Button) v).getTag().equals("Pause")) {

                        try {
                            mPlayer.pause();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        ((Button) v).setBackgroundDrawable(playd);
                        ((Button) v).setTag("Play");// Text("Play");
                    } else {
                        nowplaying = position;
                        if (mPlayer != null) {
                            if (mPlayer.isPlaying())
                                mPlayer.stop();
                        }
                        previous.add(playthis);
                        ((Button) v).setBackgroundDrawable(paused);
                        ((Button) v).setTag("Pause");// Text("Pause");
                        prebtn = playthis;
                        filepath = templist.get(position).getFilename();
                        isplaying = true;

                        onPlay(isplaying);
                        isplaying = !isplaying;
                        mPlayer.setOnCompletionListener(new OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                isplaying = false;
                                ((Button) v).setBackgroundDrawable(playd);
                                ((Button) v).setTag("Play");// Text("Play");
                                lastclicked = -1;
                                nowplaying = -1;
                                previous.remove(playthis);
                                prebtn = null;
                            }
                        });
                    }
                    lastclicked = position;
                    System.out.println("lastclicked ,position" + lastclicked + ", " + position);
                }
            });
            if (position == nowplaying) {
                System.out.println("now playing" + nowplaying);
                playthis.setBackgroundDrawable(paused);
                playthis.setTag("Pause");
                previous.add(playthis);
                prebtn = playthis;
                if (mPlayer != null) {
                    System.out.println("nowplaying if");
                    mPlayer.setOnCompletionListener(new OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playthis.setBackgroundDrawable(playd);
                            playthis.setTag("Play");
                            previous.remove(playthis);
                            prebtn = null;
                        }
                    });
                } else {
                    System.out.println("nowplaying else");
                    playthis.setBackgroundDrawable(playd);
                    playthis.setTag("Play");
                    previous.remove(playthis);
                    prebtn = null;
                }
            }
            System.out.println("outside lastclicked ,position" + lastclicked + ", " + position);
            return (row);
        }

    }

    static class Viewholder {
        public TextView txtvew;
        public CheckBox chkbx;
    }
}
