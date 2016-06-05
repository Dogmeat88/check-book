package nz.net.pseudo.checkbook.beacon;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import nz.net.pseudo.checkbook.AppUtils;
import nz.net.pseudo.checkbook.R;
import nz.net.pseudo.checkbook.databinding.BeaconSettingsFragmentBinding;

/**
 * Created by i on 10/05/16.
 * Settings management for Beacons
 */
public class BeaconSettingsFragment extends DialogFragment{
    private BeaconSettingsFragmentBinding mBinding;
    private BeaconIface beaconImpl;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            beaconImpl = (BeaconIface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BeaconIface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beacon_settings_fragment, container, false);
        mBinding = BeaconSettingsFragmentBinding.bind(view);

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


        int savedScanRate = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.BEACON_SCAN_RATE_KEY
                , getResources().getInteger(R.integer.settings_beacon_scan_rate_default_ms))/AppUtils.SEC_IN_MS;
        mBinding.seekBeaconScanRateValue.setText(getString(R.string.unit_symbol_seconds, savedScanRate));
        mBinding.seekBeaconScanRate.setProgress(savedScanRate);
        mBinding.seekBeaconScanRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBinding.seekBeaconScanRateValue.setText(getString(R.string.unit_symbol_seconds,(progress+1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.BEACON_SCAN_RATE_KEY, (seekBar.getProgress() + 1) * AppUtils.SEC_IN_MS).commit();
                if(getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(AppUtils.BEACON_STATE_KEY, false)) {
                    beaconImpl.setBeaconEnabled(false);
                    beaconImpl.setBeaconEnabled(true);
                }
            }
        });


        int savedOverrideDuration = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.BEACON_OVERRIDE_DURATION_KEY
                , getResources().getInteger(R.integer.settings_beacon_override_duration_default_ms))/AppUtils.SEC_IN_MS;
        mBinding.seekBeaconOverrideDurationValue.setText(getString(R.string.unit_symbol_seconds,savedOverrideDuration));
        mBinding.seekBeaconOverrideDuration.setProgress(savedOverrideDuration);
        mBinding.seekBeaconOverrideDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBinding.seekBeaconOverrideDurationValue.setText(getString(R.string.unit_symbol_seconds,(progress+1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.BEACON_OVERRIDE_DURATION_KEY, (seekBar.getProgress()+1)*AppUtils.SEC_IN_MS).commit();
            }
        });


        int savedDistance = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(AppUtils.BEACON_TRIGGER_DISTANCE_KEY, getActivity().getResources().getInteger(R.integer.settings_beacon_trigger_distance_default_m));
        mBinding.seekBeaconTriggerDistanceValue.setText(getString(R.string.unit_symbol_minutes,savedDistance));
        mBinding.seekBeaconTriggerDistance.setProgress(savedDistance);
        mBinding.seekBeaconTriggerDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBinding.seekBeaconTriggerDistanceValue.setText(getString(R.string.unit_symbol_minutes,(progress+1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt(AppUtils.BEACON_TRIGGER_DISTANCE_KEY, seekBar.getProgress()+1).commit();
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
