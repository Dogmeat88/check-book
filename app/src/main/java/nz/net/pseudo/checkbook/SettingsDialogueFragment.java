package nz.net.pseudo.checkbook;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import nz.net.pseudo.checkbook.databinding.SettingsDialogueFragmentBinding;

/**
 * Created by i on 10/05/16.
 */
public class SettingsDialogueFragment extends DialogFragment{
    SettingsDialogueFragmentBinding mBinding;
    ResourceListIface resourceListImpl;
    EventsListIface eventsListImpl;
    BeaconIface beaconImpl;

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
        View view = inflater.inflate(R.layout.settings_dialogue_fragment, container, false);
        mBinding = SettingsDialogueFragmentBinding.bind(view);


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



        mBinding.settingsBeaconToggleButton.setChecked(getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.BEACON_STATE_KEY, false));

        //Disable beacon support if not available
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mBinding.settingsBeaconToggleButton.setEnabled(false);
            mBinding.settingsBeaconDisabled.setVisibility(View.VISIBLE);
        }

        mBinding.settingsBeaconToggleButton.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   getActivity().getPreferences(Context.MODE_PRIVATE).edit().putBoolean(AppUtils.BEACON_STATE_KEY, isChecked).commit();
                   beaconImpl.setBeaconEnabled(isChecked);
               }
           }
        );


        mBinding.editBeaconOverride.setHint(
                String.valueOf(getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.BEACON_OVERRIDE_DURATION_KEY
                , getResources().getInteger(R.integer.settings_beacon_override_duration_default))/1000));

        mBinding.editBeaconOverride.addTextChangedListener(new TextWatcher() {
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
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().remove(AppUtils.BEACON_OVERRIDE_DURATION_KEY).commit();
                }else {
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.BEACON_OVERRIDE_DURATION_KEY, Integer.valueOf(s.toString())*1000).commit();
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
