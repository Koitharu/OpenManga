package org.nv95.openmanga.utils.choicecontrol;

import android.view.ActionMode;

/**
 * Created by nv95 on 30.06.16.
 */

public interface ModalChoiceCallback extends ActionMode.Callback {
    void onChoiceChanged(ActionMode actionMode, ModalChoiceController controller, int count);
}
