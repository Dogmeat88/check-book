package nz.net.pseudo.checkbook;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nz.net.pseudo.checkbook.databinding.AboutFragmentBinding;
import nz.net.pseudo.checkbook.event.EventsListIface;
import nz.net.pseudo.checkbook.resource.ResourceListIface;

/**
 * Created by i on 10/05/16.
 * Information about CheckBook
 */
public class AboutFragment extends DialogFragment{
    private AboutFragmentBinding mBinding;
    private ResourceListIface resourceListImpl;
    private EventsListIface eventsListImpl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        mBinding = AboutFragmentBinding.bind(view);

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
