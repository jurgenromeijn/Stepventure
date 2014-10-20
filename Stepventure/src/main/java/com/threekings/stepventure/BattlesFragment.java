package com.threekings.stepventure;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.threekings.stepventure.adapter.TypefaceSpan;

import java.util.Random;


public class BattlesFragment extends android.app.Fragment {

    // Tag
    public static final String TAG = "com.threekings.stepventure.BattleFragment";

    // Settings
    private SharedPreferences settings;

    // Calculations
    private Random random = new Random();
    private int min = 5;
    private int max = 7;

    // View elements
    private View rootView;
    private MediaPlayer mp;

    // Game
    private Boolean battleState;
    private Boolean levelUp = false;
    private double damage;
    private int randomMonster;
    private boolean battleActive = false;


    // BUTTONS + DAMAGE & MISS CHANCE
    private Button swordButton;
    private double swordDamage = 1;
    private double swordMissChance = 0;

    private Button shieldBashButton;
    private double shieldBashDamage = 1.5;
    private double shieldBashMissChance = 0.25;

    private Button laserButton;
    private double laserDamage = 2;
    private double laserMissChance = 0.5;

    private double monsterMissChance = 0.1;

    // MONSTER STATS
    private ImageView imgMonster;
    private CharSequence nameMonster;
    private int levelMonster;

    private double attackDamageWeaponMonster;
    private double attackDamageMonster;
    private double defenseMonster;
    private double healthMonster;
    private int experienceWorth;

    // Base stats
    private double multiplierAttackDamageMonster = 0.5;
    private double multiplierDefenseMonster = 0.5;
    private double multiplierHealthMonster = 0.5;

    private int baseAttackDamageMonster = 10;
    private int baseDefenseMonster = 10;
    private int baseHealthMonster = 200;

    // PLAYER STATS
    private ImageView imgPlayer;
    private CharSequence namePlayer = "You";

    private int stepcoins;

    // Settings
    public static final String ID_TOTAL_MONSTERS_KILLED = "totalMonstersKilled";
    public static final String ID_TOTAL_TIMES_DIED = "totalTimesDied";
    public static final String ID_LEVEL_PLAYER = "levelPlayer";
    public static final String ID_HEALTH_PLAYER = "healthPlayer";
    public static final String ID_ATTACK_DAMAGE_PLAYER = "attackDamagePlayer";
    public static final String ID_DEFENSE_PLAYER = "defensePlayer";
    public static final String ID_CURRENT_EXPERIENCE = "currentExperience";
    public static final String ID_EXPERIENCE_NEEDED = "experienceNeeded";

    // Local stats
    private int totalMonstersKilled;
    private int totalTimesDied;
    private int levelPlayer;
    private double healthPlayer;
    private double attackDamageWeaponPlayer;
    private double attackDamagePlayer;
    private double defensePlayer;
    private int currentExperience;
    private int experienceNeeded;
    private int defenseShield = 0;

    // Base stats
    private double multiplierAttackDamagePlayer = 0.6;
    private double multiplierDefensePlayer = 0.6;
    private double multiplierHealthPlayer = 0.5;

    private int baseAttackDamagePlayer = 10;
    private int baseDefensePlayer = 10;
    private int baseHealthPlayer = 200;
    private int baseExperiencePlayer = 1000;

    // TEXTVIEWS
    private TextView textViewLevelPlayer;
    private TextView textViewHealthPlayer;
    private TextView textViewNameMonster;
    private TextView textViewLevelMonster;
    private TextView textViewHealthMonster;
    private TextView textViewBattleInformation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        settings = getActivity().getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Stats Player
        levelPlayer = settings.getInt(ID_LEVEL_PLAYER, 1);
        editor.putInt(ID_LEVEL_PLAYER, levelPlayer);

        stepcoins = settings.getInt(MainActivity.ID_STEPCOUNT, 0);

        long healthPlayerLong = Double.doubleToRawLongBits(baseHealthPlayer * levelPlayer * multiplierHealthPlayer);
        healthPlayer = settings.getLong(ID_HEALTH_PLAYER, healthPlayerLong);
        editor.putLong(ID_HEALTH_PLAYER, healthPlayerLong);
        healthPlayer = Double.longBitsToDouble(healthPlayerLong);

        attackDamageWeaponPlayer = levelPlayer * 3;

        long attackDamagePlayerLong = Double.doubleToRawLongBits(baseAttackDamagePlayer * (levelPlayer * multiplierAttackDamagePlayer + attackDamageWeaponPlayer));
        attackDamagePlayer = settings.getLong(ID_ATTACK_DAMAGE_PLAYER, attackDamagePlayerLong);
        editor.putLong(ID_ATTACK_DAMAGE_PLAYER, attackDamagePlayerLong);
        attackDamagePlayer = Double.longBitsToDouble(attackDamagePlayerLong);

        long defensePlayerLong = Double.doubleToRawLongBits(baseDefensePlayer * levelPlayer * multiplierDefensePlayer + defenseShield);
        defensePlayer = settings.getLong(ID_DEFENSE_PLAYER, defensePlayerLong);
        editor.putLong(ID_DEFENSE_PLAYER, defensePlayerLong);
        defensePlayer = Double.longBitsToDouble(defensePlayerLong);

        int experience = (baseExperiencePlayer * levelPlayer * 10) * levelPlayer;
        experienceNeeded = settings.getInt(ID_EXPERIENCE_NEEDED, experience);
        editor.putInt(ID_EXPERIENCE_NEEDED, experience);

        currentExperience = settings.getInt(ID_CURRENT_EXPERIENCE, 0);

        // Stats Monster
        levelMonster = levelPlayer;
        attackDamageWeaponMonster = levelMonster * 3;
        attackDamageMonster = baseAttackDamageMonster * (levelMonster * multiplierAttackDamageMonster + attackDamageWeaponMonster);
        defenseMonster = baseDefenseMonster * levelMonster * multiplierDefenseMonster;
        healthMonster = baseHealthMonster * levelMonster * multiplierHealthMonster;
        experienceWorth = levelMonster * 2500;

        totalMonstersKilled = settings.getInt(ID_TOTAL_MONSTERS_KILLED, 0);
        totalTimesDied = settings.getInt(ID_TOTAL_TIMES_DIED, 0);

        editor.commit();

        Bundle args = getArguments();
        Boolean id = args.getBoolean("index", false);

        battleState = true;

        if(id)
        {
            Log.i(TAG, "--- BATTLE ACTIVE");
            battleActive = true;
            // Load Correct layout
            rootView = inflater.inflate(R.layout.fragment_battles, container, false);

            // Random number between all the monsters we have
            randomMonster = random.nextInt(3 - 1 + 1) + 1;

            switch (randomMonster) {
                case 1:
                    nameMonster = "Slime";
                    break;
                case 2:
                    nameMonster = "Freeze";
                    break;
                case 3:
                    nameMonster = "Mister T";
                    break;
                default:
                    break;
            }

            swordButton = (Button) rootView.findViewById(R.id.sword_attack);
            swordButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SwordAttack();
                }
            });

            shieldBashButton = (Button) rootView.findViewById(R.id.shield_bash_attack);
            shieldBashButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { ShieldBashAttack();
                }
            });

            laserButton = (Button) rootView.findViewById(R.id.laser_attack);
            laserButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    LaserAttack();
                }
            });

            // Enabling attacks while leveling
            switch (levelPlayer) {
                case 1:
                    shieldBashButton.setVisibility(View.GONE);
                    laserButton.setVisibility(View.GONE);
                    break;
                case 2:
                    laserButton.setVisibility(View.GONE);
                    break;
                case 3:
                    // All attacks enabled
                    break;
                default:
                    // All attacks enabled
                    break;
            }

            // Set image of the player
            imgPlayer = (ImageView) rootView.findViewById(R.id.player_portrait);
            imgPlayer.setImageResource(R.drawable.head_back_right);

            // Set image of the monster
            imgMonster = (ImageView) rootView.findViewById(R.id.monster_portrait);
            // Checks the value of the randomMonster and puts a random enemy on the screen
            String icon = "mob_" + randomMonster;
            int resID = getResources().getIdentifier(icon, "drawable", "com.threekings.stepventure");
            imgMonster.setImageResource(resID);

            textViewLevelPlayer = (TextView) rootView.findViewById(R.id.level_player);
            textViewHealthPlayer = (TextView) rootView.findViewById(R.id.health_player);

            textViewNameMonster = (TextView) rootView.findViewById(R.id.name_monster);
            textViewLevelMonster = (TextView) rootView.findViewById(R.id.level_monster);
            textViewHealthMonster = (TextView) rootView.findViewById(R.id.health_monster);

            textViewBattleInformation = (TextView) rootView.findViewById(R.id.battle_information);

            // Player Stats
            textViewLevelPlayer.setText(getResources().getString(R.string.battle_level, levelPlayer));
            textViewHealthPlayer.setText(getResources().getString(R.string.battle_health, (int)Math.round(healthPlayer)));

            // Monster stats
            SetMonsterSpannableString(nameMonster);
            textViewLevelMonster.setText(getResources().getString(R.string.battle_level, levelMonster));
            textViewHealthMonster.setText(getResources().getString(R.string.battle_health, (int)Math.round(healthMonster)));

            // Button text and attack info
            swordButton.setText(getResources().getString(R.string.battle_sword_button, (int)Math.round(swordDamage * 100), (int)Math.round(swordMissChance * 100)));
            shieldBashButton.setText(getResources().getString(R.string.battle_shield_button, (int)Math.round(shieldBashDamage * 100), (int)Math.round(shieldBashMissChance * 100)));
            laserButton.setText(getResources().getString(R.string.battle_laser_button, (int)Math.round(laserDamage * 100), (int)Math.round(laserMissChance * 100)));
        }
        else
        {
            battleActive = false;

            // Load Correct layout
            rootView = inflater.inflate(R.layout.fragment_no_battles, container, false);

            // Battle Stats
            totalMonstersKilled = settings.getInt(BattlesFragment.ID_TOTAL_MONSTERS_KILLED, 0);
            totalTimesDied = settings.getInt(BattlesFragment.ID_TOTAL_TIMES_DIED, 0);

            // Get view elements
            TextView won_counter = (TextView) rootView.findViewById(R.id.won_counter);
            TextView lost_counter = (TextView) rootView.findViewById(R.id.lost_counter);

            // Set counter values
            won_counter.setText(String.valueOf(totalMonstersKilled));
            lost_counter.setText(String.valueOf(totalTimesDied));

        }

        return rootView;
    }

    /** Create string with custom font */
    private void SetMonsterSpannableString(CharSequence title){
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan(getActivity().getApplicationContext(), "square.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewNameMonster.setText(s);
    }

    public void MoveMonsterImage()
    {
        final float amountToMoveDown = -20;

        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, amountToMoveDown);

        anim.setDuration(350);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) { }
        });

        imgMonster.startAnimation(anim);
    }

    public void MoveMonsterImageBack()
    {
        final float amountToMoveRight = 0;
        final float amountToMoveDown = 20;

        TranslateAnimation anim = new TranslateAnimation(0, 0, amountToMoveRight, amountToMoveDown);

        anim.setDuration(350);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) { }
        });

        imgMonster.startAnimation(anim);
    }

    public void MovePlayerImage()
    {
        final float amountToMoveRight = 0;
        final float amountToMoveDown = -20;

        TranslateAnimation anim = new TranslateAnimation(0, 0, amountToMoveRight, amountToMoveDown);

        anim.setDuration(350);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) { }
        });

        imgPlayer.startAnimation(anim);
    }

    public void MovePlayerImageBack()
    {
        final float amountToMoveRight = 0;
        final float amountToMoveDown = -20;

        TranslateAnimation anim = new TranslateAnimation(0, 0, amountToMoveRight, amountToMoveDown);

        anim.setDuration(350);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) { }
        });

        imgPlayer.startAnimation(anim);
    }

    public void MonsterAttack() {
        // creating a range from 10 numbers: 100 / 10 = 10%
        double range = 1 / monsterMissChance;
        // Calculate miss chance (random number between 1 and 10)
        int rand = (int)(Math.random() * range + (range * monsterMissChance));

        // Attack miss
        if(rand == 1.0)
        {
            // Creates the message
            CharSequence text = getResources().getString(R.string.battle_monster_missed, nameMonster);
            textViewBattleInformation.setText(text);
        }
        else
        {
            MoveMonsterImage();

            // Monster attack sound
            try {
                mp = MediaPlayer.create(this.getActivity(), R.raw.monster_attack);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer playSuccess) {
                        mp.release();
                    }
                });
                mp.start();
            }catch (NullPointerException e){
                Log.e(TAG, String.valueOf(e));
            }

            MoveMonsterImageBack();

            // Damage calculation
            int randomAttackNumber = random.nextInt(max - min + 1) + min;

            damage = (attackDamageMonster - (defensePlayer)) * randomAttackNumber / 10;
            if(damage <= 0)
            {
                damage = damage * -1;
            }

            healthPlayer = healthPlayer - damage;

            if(healthPlayer <= 0)
            {
                // Sets health of the player to zero so it won't go below zero
                healthPlayer = 0;
            }
            int healthPlayerRound = (int)Math.round(healthPlayer);
            textViewHealthPlayer.setText(getResources().getString(R.string.battle_health, healthPlayerRound));

            // Creates the message
            textViewBattleInformation.setText(getResources().getString(R.string.battle_monster_hit, nameMonster, (int)Math.round(damage)));

        }

        // Makes the buttons unclickable
        laserButton.setClickable(false);
        swordButton.setClickable(false);
        shieldBashButton.setClickable(false);

        // Makes sure the player cannot attack before the message is finished
        Handler handler = new Handler();

        // Makes the buttons clickable after 2 seconds
        handler.postDelayed(new Runnable() {
            public void run() {

                laserButton.setClickable(true);
                swordButton.setClickable(true);
                shieldBashButton.setClickable(true);

                // Set battleState to true so the player can attack again
                battleState = true;

                // Checks the status of the battle (dead or alive)
                battleStatus();
            }

        }, 2000);

    }

    public void SwordAttack() {
        MovePlayerImage();

        // Sword sound
        mp = MediaPlayer.create(this.getActivity(), R.raw.sword_sound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer playSuccess) {
                mp.release();
            }
        });
        mp.start();


        MovePlayerImageBack();

        // Damage calculation
        int randomAttackNumber = random.nextInt(max - min + 1) + min;

        damage = ((attackDamagePlayer - (defenseMonster)) * randomAttackNumber / 10) * swordDamage;
        if(damage <= 0)
        {
            damage = damage * -1;
        }

        healthMonster = healthMonster - damage;

        if(healthMonster <= 0)
        {
            // Sets health of the monster to zero so it won't go below zero
            healthMonster = 0;
        }
        int healthMonsterRound = (int)Math.round(healthMonster);
        textViewHealthMonster.setText(getResources().getString(R.string.battle_health, healthMonsterRound));

        // Creates the message
        textViewBattleInformation.setText(getResources().getString(R.string.battle_sword_hit, (int)Math.round(damage)));

        // Makes the buttons unclickable
        laserButton.setClickable(false);
        swordButton.setClickable(false);
        shieldBashButton.setClickable(false);

        // Makes sure the player cannot attack before the message is finished
        Handler handler = new Handler();

        // Makes the buttons clickable after 2 seconds
        handler.postDelayed(new Runnable() {
            public void run() {

                laserButton.setClickable(true);
                swordButton.setClickable(true);
                shieldBashButton.setClickable(true);

                // Set battleState to false so the monster can attack again
                battleState = false;

                // Checks the status of the battle (dead or alive)
                battleStatus();
            }

        }, 2000);
    }

    public void ShieldBashAttack() {
        // creating a range from 2 numbers: 100 / 2 = 50%
        double range = 1 / shieldBashMissChance;
        // Calculate miss chance (random number between 1 and 2)
        int rand = (int)(Math.random() * range + (range * shieldBashMissChance));

        // Attack miss!
        if(rand == 1.0)
        {
            // Creates the message
            textViewBattleInformation.setText(getResources().getString(R.string.battle_shield_missed));
        }
        else
        {
            MovePlayerImage();

            // Shield Bash sound
            mp = MediaPlayer.create(this.getActivity(), R.raw.shield_bash_sound);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer playSuccess) {
                    mp.release();
                }
            });
            mp.start();


            MovePlayerImageBack();

            // Damage calculation
            int randomAttackNumber = random.nextInt(max - min + 1) + min;

            damage = ((attackDamagePlayer - (defenseMonster)) * randomAttackNumber / 10) * shieldBashDamage;
            if(damage <= 0)
            {
                damage = damage * -1;
            }

            healthMonster = healthMonster - damage;

            if(healthMonster <= 0)
            {
                // Sets health of the monster to zero so it won't go below zero
                healthMonster = 0;
            }
            int healthMonsterRound = (int)Math.round(healthMonster);
            textViewHealthMonster.setText(getResources().getString(R.string.battle_health, healthMonsterRound));

            // Creates the message
            textViewBattleInformation.setText(getResources().getString(R.string.battle_shield_hit, (int)Math.round(damage)));
        }
        // Makes the buttons unclickable
        laserButton.setClickable(false);
        swordButton.setClickable(false);
        shieldBashButton.setClickable(false);

        // Makes sure the player cannot attack before the message is finished
        Handler handler = new Handler();

        // Makes the buttons clickable after 2 seconds
        handler.postDelayed(new Runnable() {
            public void run() {

                laserButton.setClickable(true);
                swordButton.setClickable(true);
                shieldBashButton.setClickable(true);

                // Set battleState to false so the monster can attack again
                battleState = false;

                // Checks the status of the battle (dead or alive)
                battleStatus();
            }

        }, 2000);
    }

    public void LaserAttack() {
        // creating a range from 4 numbers: 100 / 4 = 25%
        double range = 1 / laserMissChance;
        // Calculate miss chance (random number between 1 and 4)
        int rand = (int)(Math.random() * range + (range * laserMissChance));

        // Attack miss!
        if(rand == 1.0)
        {
            // Creates the message
            textViewBattleInformation.setText(getResources().getString(R.string.battle_laser_missed));
        }
        else
        {
            MovePlayerImage();

            // Laser sound
            mp = MediaPlayer.create(this.getActivity(), R.raw.laser_sound);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer playSuccess) {
                    mp.release();
                }
            });
            mp.start();


            MovePlayerImageBack();

            // Damage calculation
            int randomAttackNumber = random.nextInt(max - min + 1) + min;

            damage = ((attackDamagePlayer - (defenseMonster)) * randomAttackNumber / 10) * laserDamage;
            if(damage <= 0)
            {
                damage = damage * -1;
            }

            healthMonster = healthMonster - damage;

            if(healthMonster <= 0)
            {
                // Sets health of the monster to zero so it won't go below zero
                healthMonster = 0;
            }
            int healthMonsterRound = (int)Math.round(healthMonster);
            textViewHealthMonster.setText(getResources().getString(R.string.battle_health, healthMonsterRound));

            // Creates the message
            textViewBattleInformation.setText(getResources().getString(R.string.battle_laser_hit, (int)Math.round(damage)));
        }
        // Makes the buttons unclickable
        laserButton.setClickable(false);
        swordButton.setClickable(false);
        shieldBashButton.setClickable(false);

        // Makes sure the player cannot attack before the message is finished
        Handler handler = new Handler();

        // Makes the buttons clickable after 2 seconds
        handler.postDelayed(new Runnable() {
            public void run() {

                laserButton.setClickable(true);
                swordButton.setClickable(true);
                shieldBashButton.setClickable(true);

                // Set battleState to false so the monster can attack again
                battleState = false;

                // Checks the status of the battle (dead or alive)
                battleStatus();
            }

        }, 2000);
    }

    public void battleStatus() {
        // Monster and Player still alive
        if(healthMonster > 0 && healthPlayer > 0)
        {
            // Monster turn or Player turn to attack
            if(battleState == true)
            {
                // Player may attack
                laserButton.setClickable(true);
                swordButton.setClickable(true);
                shieldBashButton.setClickable(true);
            }
            else
            {
                // Player may not attack
                laserButton.setClickable(false);
                swordButton.setClickable(false);
                shieldBashButton.setClickable(false);

                // Monster must attack now
                MonsterAttack();
            }
        }
        else if(healthMonster <= 0) // Monster died
        {
            // Makes the buttons unclickable
            laserButton.setClickable(false);
            swordButton.setClickable(false);
            shieldBashButton.setClickable(false);

            SharedPreferences.Editor editor = settings.edit();

            totalMonstersKilled = totalMonstersKilled + 1;
            editor.putInt(ID_TOTAL_MONSTERS_KILLED, totalMonstersKilled);

            currentExperience = settings.getInt(ID_CURRENT_EXPERIENCE, 0) + experienceWorth;

            // LEVEL UP
            if(currentExperience >= experienceNeeded)
            {
                editor.putInt(ID_LEVEL_PLAYER, levelPlayer + 1);
                editor.commit();

                int newLevelPlayer = settings.getInt(ID_LEVEL_PLAYER, 1);

                // health update
                long healthPlayerLong = Double.doubleToRawLongBits(baseHealthPlayer * newLevelPlayer * multiplierHealthPlayer);
                editor.putLong(ID_HEALTH_PLAYER, healthPlayerLong);

                // ad update
                long attackDamagePlayerLong = Double.doubleToRawLongBits(baseAttackDamagePlayer * (newLevelPlayer * multiplierAttackDamagePlayer + attackDamageWeaponPlayer));
                editor.putLong(ID_ATTACK_DAMAGE_PLAYER, attackDamagePlayerLong);

                // defense update
                long defensePlayerLong = Double.doubleToRawLongBits(baseDefensePlayer * newLevelPlayer * multiplierDefensePlayer + defenseShield);
                editor.putLong(ID_DEFENSE_PLAYER, defensePlayerLong);

                // xp needed update
                int experience = (baseExperiencePlayer * newLevelPlayer * 10) * (newLevelPlayer / 2);
                editor.putInt(ID_EXPERIENCE_NEEDED, experience);

                editor.putInt(ID_CURRENT_EXPERIENCE, 0);

                levelUp = true;
            }
            // ADD XP
            else
            {
                editor.putInt(ID_CURRENT_EXPERIENCE, currentExperience);
            }

            editor.commit();
            // Redirects to the statistics page after 2.5 seconds (after the messages)
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                // Creates the message
                CharSequence text = getResources().getString(R.string.battle_won, nameMonster);

                public void run() {
                    // Creates the message
                    textViewBattleInformation.setText(text);
                }

            }, 500);

            if(levelUp == true)
            {
                handler.postDelayed(new Runnable() {

                    // Creates the message
                    CharSequence text = getResources().getString(R.string.battle_won_experience_level, nameMonster, experienceWorth, settings.getInt(ID_LEVEL_PLAYER, 0));

                    public void run() {
                        // Creates the message
                        textViewBattleInformation.setText(text);
                    }

                }, 1500);
            }
            else
            {
                handler.postDelayed(new Runnable() {

                    // Creates the message
                    CharSequence text = getResources().getString(R.string.battle_won_experience, nameMonster, experienceWorth);


                    public void run() {
                        // Creates the message
                        textViewBattleInformation.setText(text);
                    }

                }, 1500);
            }

            // Redirects to the statistics page after 2.5 seconds (after the messages)
            handler.postDelayed(new Runnable() {

                public void run() {
                    battleActive = false;

                    Fragment fragment = new BattlesFragment();

                    Boolean index = false;
                    Bundle args = new Bundle();
                    args.putBoolean("index", index);
                    fragment.setArguments(args);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();
                }

            }, 3250);

        }
        else if(healthPlayer <= 0) // Player died
        {
            // Makes the buttons unclickable
            laserButton.setClickable(false);
            swordButton.setClickable(false);
            shieldBashButton.setClickable(false);

            SharedPreferences.Editor editor = settings.edit();

            totalTimesDied = totalTimesDied + 1;
            editor.putInt(ID_TOTAL_TIMES_DIED, totalTimesDied);

            Log.v("STEPCOINS: ", Integer.toString(stepcoins));

            int newStepcoinsAmount = stepcoins - (int)Math.round((baseHealthPlayer * (levelPlayer * 0.08)));
            Log.v("NEW STEPCOINS: ", Integer.toString(newStepcoinsAmount));
            if(newStepcoinsAmount < 0)
            {
                newStepcoinsAmount = 0;
            }

            editor.putInt(MainActivity.ID_STEPCOUNT, newStepcoinsAmount);
            editor.commit();

            // Creates the message
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                // Creates the message
                CharSequence text = getResources().getString(R.string.battle_lost, namePlayer);

                public void run() {
                    // Creates the message
                    textViewBattleInformation.setText(text);
                }

            }, 500);

            handler.postDelayed(new Runnable() {

                // Creates the message
                CharSequence text = getResources().getString(R.string.battle_lost_coins, namePlayer, (int)Math.round((baseHealthPlayer * (levelPlayer * 0.08))));


                public void run() {
                    // Creates the message
                    textViewBattleInformation.setText(text);
                }

            }, 1500);

            // Redirects to the statistics page after 4.5 seconds (after the messages)
            handler.postDelayed(new Runnable() {

                public void run() {
                    battleActive = false;

                    Fragment fragment = new BattlesFragment();

                    Boolean index = false;
                    Bundle args = new Bundle();
                    args.putBoolean("index", index);
                    fragment.setArguments(args);

                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();
                }

            }, 3250);
        }
    }

    /** State methods */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "--- START");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "--- RESUME");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "--- PAUSE");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "--- STOP");
        if(battleActive){
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.battle_run), Toast.LENGTH_LONG).show();
            if (mp != null){
                mp.release();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "--- DESTROY");
    }


}
