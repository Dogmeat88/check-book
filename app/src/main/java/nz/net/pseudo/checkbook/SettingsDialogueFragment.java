package nz.net.pseudo.checkbook;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nz.net.pseudo.checkbook.databinding.SettingsDialogueFragmentBinding;

/**
 * Created by i on 10/05/16.
 */
public class SettingsDialogueFragment extends DialogFragment{
    SettingsDialogueFragmentBinding mBinding;
    ResourceListIface resourceListImpl;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            resourceListImpl = (ResourceListIface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ResourceListIface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_dialogue_fragment, container, false);
        mBinding = SettingsDialogueFragmentBinding.bind(view);

        mBinding.settingsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mBinding.settingsRefreshResourcesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.persistResources(getActivity().getPreferences(Context.MODE_PRIVATE), null);
                resourceListImpl.resourcesAsyncTask();
            }
        });

        return view;
    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//            super.onCreate(savedInstanceState);
//            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogCustom);
//            setCancelable(true);
//            setRetainInstance(true);
//       // mBinding = DataBindingUtil.setContentView(getActivity(), R.layout.settings_dialogue_fragment);
//    }
}
