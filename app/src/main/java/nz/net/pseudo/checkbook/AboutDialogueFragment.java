package nz.net.pseudo.checkbook;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import nz.net.pseudo.checkbook.databinding.AboutDialogueFragmentBinding;
import nz.net.pseudo.checkbook.databinding.SettingsDialogueFragmentBinding;

/**
 * Created by i on 10/05/16.
 */
public class AboutDialogueFragment extends DialogFragment{
    AboutDialogueFragmentBinding mBinding;
    ResourceListIface resourceListImpl;
    EventsListIface eventsListImpl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_dialogue_fragment, container, false);
        mBinding = AboutDialogueFragmentBinding.bind(view);

        mBinding.aboutContentTextview.setMovementMethod(LinkMovementMethod.getInstance());

        mBinding.aboutDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

}
