package nz.net.pseudo.cb;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import nz.net.pseudo.cb.beacon.BeaconIface;
import nz.net.pseudo.cb.databinding.GeneralSettingsFragmentBinding;
import nz.net.pseudo.cb.event.EventsListIface;
import nz.net.pseudo.cb.resource.ResourceListIface;

/**
 * Created by i on 10/05/16.
 * General settings management for the app
 */
public class GeneralSettingsFragment extends DialogFragment{
    private GeneralSettingsFragmentBinding mBinding;
    private ResourceListIface resourceListImpl;
    private EventsListIface eventsListImpl;
    private BeaconIface beaconImpl;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            resourceListImpl = (ResourceListIface) context;
            eventsListImpl = (EventsListIface) context;
            beaconImpl = (BeaconIface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ResourceListIface, EventsListIface and BeaconIface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_settings_fragment, container, false);
        mBinding = GeneralSettingsFragmentBinding.bind(view);


        int quickBookDuration = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.QUICKBOOK_DURATION_KEY, getResources().getInteger(R.integer.settings_event_duration_15min));

        if(quickBookDuration==getResources().getInteger(R.integer.settings_event_duration_15min)){
            mBinding.settingsEventDuration15min.setChecked(true);
        }else{
            mBinding.settingsEventDuration30min.setChecked(true);
        }
        mBinding.settingsEventDurationContainer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.settings_event_duration_15min){
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.QUICKBOOK_DURATION_KEY, getResources().getInteger(R.integer.settings_event_duration_15min)).commit();
                    eventsListImpl.eventsAsyncTask();
                }else{
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.QUICKBOOK_DURATION_KEY, getResources().getInteger(R.integer.settings_event_duration_30min)).commit();
                    eventsListImpl.eventsAsyncTask();
                }
            }
        });

        mBinding.editEventTitle.setHint(getActivity().getPreferences(Context.MODE_PRIVATE).getString(AppUtils.QUICKBOOK_TITLE_KEY, getString(R.string.event_book_confirm_edittext_hint)));
        mBinding.editEventTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if("".equals(s.toString())){
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().remove(AppUtils.QUICKBOOK_TITLE_KEY).commit();
                }else {
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString(AppUtils.QUICKBOOK_TITLE_KEY, s.toString()).commit();
                }
            }
        });


        mBinding.editEmailContent.setHint(getActivity().getPreferences(Context.MODE_PRIVATE).getString(AppUtils.EVENT_EMAIL_CONTENT_KEY, getString(R.string.event_email_default_content_prefix)));
        mBinding.editEmailContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if("".equals(s.toString())){
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().remove(AppUtils.EVENT_EMAIL_CONTENT_KEY).commit();
                }else {
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString(AppUtils.EVENT_EMAIL_CONTENT_KEY, s.toString()).commit();
                }
            }
        });


        mBinding.settingsAutoRefreshToggleButton.setChecked(getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.AUTO_REFRESH_STATE_KEY, false));

        mBinding.settingsAutoRefreshToggleButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(AppUtils.AUTO_REFRESH_STATE_KEY, isChecked).commit();
                        eventsListImpl.autoRefreshInit();
                    }
                }
        );


        int savedAutoRefreshRate = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.AUTO_REFRESH_RATE_KEY
                , getResources().getInteger(R.integer.settings_auto_refresh_duration_default_ms))/AppUtils.MIN_IN_MS;
        mBinding.seekAutoRefreshRateValue.setText(getString(R.string.unit_symbol_minutes,savedAutoRefreshRate));
        mBinding.seekAutoRefreshRate.setProgress(savedAutoRefreshRate);
        mBinding.seekAutoRefreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBinding.seekAutoRefreshRateValue.setText(getString(R.string.unit_symbol_minutes,(progress+1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.AUTO_REFRESH_RATE_KEY, (seekBar.getProgress() + 1) * AppUtils.MIN_IN_MS).commit();
                if(getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.AUTO_REFRESH_STATE_KEY, false)) {
                    eventsListImpl.autoRefreshInit();
                }
            }
        });


        mBinding.settingsRefreshResourcesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.persistResources(getActivity().getPreferences(Context.MODE_PRIVATE), null);
                resourceListImpl.resourcesAsyncTask();
            }
        });

        mBinding.settingsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

}
