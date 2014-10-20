package com.threekings.stepventure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StatsFragment extends android.app.Fragment {

    // Settings
    private SharedPreferences settings;

    // View elements
    private View rootView;
    private TextView stats_level;
    private TextView stats_exp_current;
    private TextView stats_exp_needed;
    private ProgressBar stats_exp_progressbar;
    private TextView stats_health;
    private TextView stats_attack;
    private TextView stats_defense;

    // Player Stats
    private int level;
    private int experienceNeeded;
    private int currentExperience;
    private long healthLong;
    private long attackDamageLong;
    private long defenseLong;

    // Doubles
    private int health;
    private int attackDamage;
    private int defense;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get settings
        settings = getActivity().getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);

        // Get player stats
        level = settings.getInt(BattlesFragment.ID_LEVEL_PLAYER, 1);
        experienceNeeded = settings.getInt(BattlesFragment.ID_EXPERIENCE_NEEDED, 10000);
        currentExperience = settings.getInt(BattlesFragment.ID_CURRENT_EXPERIENCE, 0);
        healthLong = settings.getLong(BattlesFragment.ID_HEALTH_PLAYER, 100);
        attackDamageLong = settings.getLong(BattlesFragment.ID_ATTACK_DAMAGE_PLAYER, 36);
        defenseLong = settings.getLong(BattlesFragment.ID_DEFENSE_PLAYER, 6);

        // Convert longs to doubles
        health = (int)Math.round(Double.longBitsToDouble(healthLong));
        attackDamage = (int)Math.round(Double.longBitsToDouble(attackDamageLong));
        defense = (int)Math.round(Double.longBitsToDouble(defenseLong));

        // Get view
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        // Set Level
        stats_level = (TextView) rootView.findViewById(R.id.stats_level);
        stats_level.setText("Level: " + level);

        // Set Experience
        stats_exp_current = (TextView) rootView.findViewById(R.id.stats_exp_current);
        stats_exp_current.setText(Integer.toString(currentExperience));
        stats_exp_needed = (TextView) rootView.findViewById(R.id.stats_exp_needed);
        stats_exp_needed.setText(String.valueOf(experienceNeeded));
        stats_exp_progressbar = (ProgressBar) rootView.findViewById(R.id.stats_exp_progress);
        stats_exp_progressbar.setMax(experienceNeeded);
        stats_exp_progressbar.setProgress(currentExperience);

        // Set health
        stats_health = (TextView) rootView.findViewById(R.id.stats_health);
        if(healthLong == 100.0)
        {
            stats_health.setText(String.valueOf((long)Math.round(healthLong)));
        }
        else
        {
            stats_health.setText(String.valueOf(health));
        }


        // Set Damage/defense
        stats_attack = (TextView) rootView.findViewById(R.id.stats_attack);
        if(attackDamageLong == 36.0)
        {
            stats_attack.setText(String.valueOf((long)Math.round(attackDamageLong)));
        }
        else
        {
            stats_attack.setText(String.valueOf(attackDamage));
        }
        stats_defense = (TextView) rootView.findViewById(R.id.stats_defense);
        if(defenseLong == 6.0)
        {
            stats_defense.setText(String.valueOf((long)Math.round(defenseLong)));
        }
        else
        {
            stats_defense.setText(String.valueOf(defense));
        }

        return rootView;
    }
}
