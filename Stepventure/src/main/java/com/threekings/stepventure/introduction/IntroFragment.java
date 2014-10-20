package com.threekings.stepventure.introduction;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.threekings.stepventure.R;

public final class IntroFragment extends Fragment {
    private static final String KEY_CONTENT = "IntroFragment:Content";

    public static IntroFragment newInstance(int position, int lenght, String content, int image) {
        IntroFragment fragment = new IntroFragment();
        fragment.mContent = content;
        fragment.mImage = image;
        fragment.mPosition = position;
        fragment.mLenght = lenght;
        return fragment;
    }

    private String mContent;
    private int mImage;
    private int mPosition;
    private int mLenght;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Get view
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.intro_layout);

        // Set String
        TextView help_text = (TextView) rootView.findViewById(R.id.help_text);
        help_text.setText(mContent);
        help_text.setTextSize(20);
        help_text.setPadding(20, 20, 20, 20);

        // Set Image
        ImageView screenshot = (ImageView) rootView.findViewById(R.id.screenshot);
        screenshot.setImageResource(mImage);

        if (mPosition == 0){
            TextView hint = new TextView(getActivity());
            hint.setText(getResources().getString(R.string.swipe_hint));
            hint.setTypeface(Typeface.create("sans-serif-thin", 0));
            hint.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            hint.setTextColor(getResources().getColor(R.color.white));
            hint.setTextSize(30);
            hint.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.1f));

            layout.addView(hint);
        }
        if (mPosition == mLenght-1){
            Button exitIntroButton = new Button(getActivity());
            exitIntroButton.setText(getResources().getString(R.string.exit_introduction));
            exitIntroButton.setTypeface(Typeface.create("sans-serif", 0));
            exitIntroButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            exitIntroButton.setTextColor(getResources().getColor(R.color.white));
            exitIntroButton.setTextSize(25);
            exitIntroButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.1f));

            exitIntroButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((IntroductionActivity)getActivity()).exitIntroduction();
                }
            });

            layout.addView(exitIntroButton);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
