/*
 * ChMacAddroid - Android app that changes a network devices MAC address
 * Copyright (C) 2014 Matthew Finkel <Matthew.Finkel@gmail.com>
 *
 * This file is part of ChMacAddroid
 *
 * ChMacAddroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ChMacAddroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ChMacAddroid, in the COPYING file.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package un.ique.chmacaddroid;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.AlertDialog;

public class CMCAlertDialog {
    private Activity mAct;

    public CMCAlertDialog(Activity act) {
        mAct = act;
    }

    public void showSuggestRestartAlert(String tag) {
        launchAndCloseRestartAlert(R.string.notice_restart,
                                   R.string.notice_restart_title,
                                   R.string.button_close_app,
                                   R.string.button_continue,
                                   "closeapp", "do_nothing",
                                   tag);
    }

    public void showInvalidEntryAlert(int messageId, String tag) {
        launchAndCloseRestartAlert(
                                messageId,
                                R.string.manualmac_notice_invalid_title,
                                R.string.button_okay,
                                0, "do_nothing", "do_nothing",
                                tag);
    }


    public void launchAndCloseRestartAlert(int messageId, int titleId,
                                           int pbutton, int nbutton,
                                           String cbPos, String cbNeg,
                                           String tag) {
        DialogFragment dialog =
                new AlertNoticeDialogFragment(messageId, titleId,
                                              pbutton, nbutton,
                                              cbPos, cbNeg);
        dialog.show(mAct.getFragmentManager(), tag);
        /*dialog.dismiss();*/
    }


    public void callbackActions(String cb) {
        if (cb == "closeapp") {
            mAct.setResult(Main.RESULT_EXIT);
            mAct.finish();
        } else if (cb == "do_nothing") {
            return;
        } else {
            return;
        }
    }

    private class AlertNoticeDialogFragment extends DialogFragment {
        int mId, tId, mPosButton, mNegButton;
        String callbackPos, callbackNeg;

        public AlertNoticeDialogFragment(int messageId, int titleId,
                                         int pbutton, int nbutton,
                                         String cbPos, String cbNeg) {
            super();
            mId = messageId;
            tId = titleId;
            mPosButton = pbutton;
            mNegButton = nbutton;
            callbackPos = cbPos;
            callbackNeg = cbNeg;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder =
                                 new AlertDialog.Builder(mAct);
            builder.setMessage(mId).setTitle(tId);
            if (mNegButton != 0) {
                builder.setNegativeButton(mNegButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackNeg);
                    }
                });
            }
            if (mPosButton != 0) {
                builder.setPositiveButton(mPosButton,
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        callbackActions(callbackPos);
                    }
                });
            }
            return builder.create();
        }
    }
}
